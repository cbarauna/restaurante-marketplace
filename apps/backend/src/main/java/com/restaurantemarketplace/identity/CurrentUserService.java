package com.restaurantemarketplace.identity;

import java.security.Principal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.identity.SecurityIdentity;

@ApplicationScoped
public class CurrentUserService {

    private final SecurityIdentity identity;
    private final AppUserRepository users;

    public CurrentUserService(SecurityIdentity identity, AppUserRepository users) {
        this.identity = identity;
        this.users = users;
    }

    @Transactional
    public AppUserView currentUser() {
        return toView(getOrCreate());
    }

    public AppUserEntity getOrCreate() {
        if (identity == null || identity.isAnonymous()) {
            throw new IdentityBusinessException("Uma identidade autenticada é obrigatória.");
        }

        String subject = requiredSubject();
        String email = normalizedEmail(claim("email"));
        boolean emailVerified = booleanClaim("email_verified");
        if (email == null) {
            Principal principal = identity.getPrincipal();
            String principalName = principal == null ? null : principal.getName();
            if (principalName != null && principalName.contains("@")) {
                email = normalizedEmail(principalName);
            }
        }
        String displayName = firstNonBlank(claim("name"), claim("preferred_username"), email, subject);
        Instant now = Instant.now();

        AppUserEntity user = users.findBySubject(subject).orElse(null);
        boolean newUser = false;
        if (user == null && email != null && emailVerified) {
            user = users.findByEmail(email).orElse(null);
            if (user != null) {
                user.oidcSubject = subject;
            }
        }
        if (user == null) {
            user = new AppUserEntity();
            user.id = UUID.randomUUID();
            user.oidcSubject = subject;
            user.status = AppUserStatus.ACTIVE;
            user.createdAt = now;
            newUser = true;
        }
        if (user.status == AppUserStatus.BLOCKED) {
            throw new IdentityBusinessException("O usuário está bloqueado.");
        }

        user.email = email;
        user.emailVerified = emailVerified;
        user.displayName = displayName;
        user.lastAuthenticatedAt = now;
        user.updatedAt = now;
        if (newUser) {
            users.persist(user);
        }
        return user;
    }

    public String currentSubject() {
        if (identity == null || identity.isAnonymous()) {
            return "anonymous";
        }
        return requiredSubject();
    }

    private String requiredSubject() {
        String subject = claim("sub");
        if (subject == null) {
            Principal principal = identity.getPrincipal();
            subject = principal == null ? null : principal.getName();
        }
        if (subject == null || subject.isBlank()) {
            throw new IdentityBusinessException("O token não possui um identificador de usuário.");
        }
        return subject.trim();
    }

    private String claim(String name) {
        Principal principal = identity.getPrincipal();
        if (!(principal instanceof JsonWebToken token)) {
            return null;
        }
        Object value = token.getClaim(name);
        return value == null ? null : value.toString();
    }

    private boolean booleanClaim(String name) {
        String value = claim(name);
        return value != null && Boolean.parseBoolean(value);
    }

    private static String normalizedEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        throw new IdentityBusinessException("Não foi possível determinar o nome do usuário.");
    }

    public static AppUserView toView(AppUserEntity user) {
        return new AppUserView(
                user.id,
                user.oidcSubject,
                user.email,
                user.emailVerified,
                user.displayName,
                user.status);
    }
}
