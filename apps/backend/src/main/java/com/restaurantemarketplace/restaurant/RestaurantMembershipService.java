package com.restaurantemarketplace.restaurant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import com.restaurantemarketplace.identity.AppUserEntity;
import com.restaurantemarketplace.identity.AppUserRepository;
import com.restaurantemarketplace.identity.CurrentUserService;

@ApplicationScoped
public class RestaurantMembershipService {

    private static final Duration INVITATION_VALIDITY = Duration.ofDays(7);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RestaurantRepository restaurants;
    private final RestaurantMemberRepository members;
    private final RestaurantMemberInvitationRepository invitations;
    private final AppUserRepository users;
    private final CurrentUserService currentUser;
    private final RestaurantAuthorization authorization;

    public RestaurantMembershipService(
            RestaurantRepository restaurants,
            RestaurantMemberRepository members,
            RestaurantMemberInvitationRepository invitations,
            AppUserRepository users,
            CurrentUserService currentUser,
            RestaurantAuthorization authorization) {
        this.restaurants = restaurants;
        this.members = members;
        this.invitations = invitations;
        this.users = users;
        this.currentUser = currentUser;
        this.authorization = authorization;
    }

    @Transactional
    public RestaurantMemberInvitationView invite(
            String restaurantId,
            String email,
            RestaurantMemberRole role) {
        UUID id = parseUuid(restaurantId, "Identificador de restaurante inválido.");
        requireRestaurant(id);
        authorization.assertCanManageMembers(id);
        requireInvitableRole(role);

        String normalizedEmail = normalizeEmail(email);
        AppUserEntity invitedUser = users.findByEmail(normalizedEmail).orElse(null);
        if (invitedUser != null && members.findByRestaurantAndUser(id, invitedUser.id()).isPresent()) {
            throw new RestaurantBusinessException("O usuário já pertence a este restaurante.");
        }

        Instant now = Instant.now();
        RestaurantMemberInvitationEntity pending = invitations.findPendingByEmail(id, normalizedEmail).orElse(null);
        if (pending != null && pending.expiresAt.isAfter(now)) {
            throw new RestaurantBusinessException("Já existe um convite pendente para este e-mail.");
        }
        if (pending != null) {
            pending.status = RestaurantMemberInvitationStatus.EXPIRED;
            pending.respondedAt = now;
            invitations.flush();
        }

        AppUserEntity actor = currentUser.getOrCreate();
        String rawToken = newInvitationToken();
        RestaurantMemberInvitationEntity invitation = new RestaurantMemberInvitationEntity();
        invitation.id = UUID.randomUUID();
        invitation.restaurantId = id;
        invitation.email = normalizedEmail;
        invitation.role = role;
        invitation.tokenHash = tokenHash(rawToken);
        invitation.status = RestaurantMemberInvitationStatus.PENDING;
        invitation.invitedByUserId = actor.id();
        invitation.expiresAt = now.plus(INVITATION_VALIDITY);
        invitation.createdAt = now;
        invitations.persist(invitation);
        return toInvitationView(invitation, rawToken, now);
    }

    @Transactional
    public List<RestaurantMemberInvitationView> list(String restaurantId) {
        UUID id = parseUuid(restaurantId, "Identificador de restaurante inválido.");
        requireRestaurant(id);
        authorization.assertCanManageMembers(id);
        Instant now = Instant.now();
        return invitations.findByRestaurant(id).stream()
                .map(invitation -> toInvitationView(invitation, null, now))
                .toList();
    }

    @Transactional(dontRollbackOn = RestaurantBusinessException.class)
    public RestaurantMemberView accept(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new RestaurantBusinessException("O token do convite é obrigatório.");
        }
        RestaurantMemberInvitationEntity invitation = invitations.findPendingByTokenHash(tokenHash(rawToken.trim()))
                .orElseThrow(() -> new RestaurantBusinessException("Convite inválido ou já utilizado."));
        Instant now = Instant.now();
        if (!invitation.expiresAt.isAfter(now)) {
            invitation.status = RestaurantMemberInvitationStatus.EXPIRED;
            invitation.respondedAt = now;
            throw new RestaurantBusinessException("O convite expirou.");
        }

        AppUserEntity user = currentUser.getOrCreate();
        if (!user.emailVerified()) {
            throw new RestaurantBusinessException(
                    "O e-mail da identidade precisa estar verificado para aceitar o convite.");
        }
        if (user.email() == null || !invitation.email.equalsIgnoreCase(user.email())) {
            throw new RestaurantBusinessException(
                    "O convite pertence a outro e-mail autenticado.");
        }
        if (members.findByRestaurantAndUser(invitation.restaurantId, user.id()).isPresent()) {
            throw new RestaurantBusinessException("O usuário já pertence a este restaurante.");
        }

        RestaurantMemberEntity member = new RestaurantMemberEntity();
        member.id = UUID.randomUUID();
        member.restaurantId = invitation.restaurantId;
        member.userId = user.id();
        member.role = invitation.role;
        member.createdAt = now;
        members.persist(member);

        invitation.status = RestaurantMemberInvitationStatus.ACCEPTED;
        invitation.acceptedByUserId = user.id();
        invitation.respondedAt = now;
        return toMemberView(member, user);
    }

    @Transactional
    public RestaurantMemberInvitationView revoke(String invitationId) {
        UUID id = parseUuid(invitationId, "Identificador de convite inválido.");
        RestaurantMemberInvitationEntity invitation = invitations.findByIdOptional(id)
                .orElseThrow(() -> new RestaurantBusinessException("Convite não encontrado."));
        authorization.assertCanManageMembers(invitation.restaurantId);
        if (invitation.status != RestaurantMemberInvitationStatus.PENDING) {
            throw new RestaurantBusinessException("Somente convites pendentes podem ser revogados.");
        }
        Instant now = Instant.now();
        invitation.status = RestaurantMemberInvitationStatus.REVOKED;
        invitation.respondedAt = now;
        return toInvitationView(invitation, null, now);
    }

    @Transactional
    public RestaurantMemberView updateRole(
            String restaurantId,
            String memberId,
            RestaurantMemberRole role) {
        UUID restaurantUuid = parseUuid(restaurantId, "Identificador de restaurante inválido.");
        UUID memberUuid = parseUuid(memberId, "Identificador de membro inválido.");
        authorization.assertCanManageMembers(restaurantUuid);
        requireInvitableRole(role);
        RestaurantMemberEntity member = members.findByRestaurantAndId(restaurantUuid, memberUuid)
                .orElseThrow(() -> new RestaurantBusinessException("Membro não encontrado."));
        if (member.role == RestaurantMemberRole.OWNER) {
            throw new RestaurantBusinessException("O papel do proprietário não pode ser alterado.");
        }
        member.role = role;
        AppUserEntity user = users.findByIdOptional(member.userId)
                .orElseThrow(() -> new RestaurantBusinessException("Usuário do membro não encontrado."));
        return toMemberView(member, user);
    }

    @Transactional
    public boolean remove(String restaurantId, String memberId) {
        UUID restaurantUuid = parseUuid(restaurantId, "Identificador de restaurante inválido.");
        UUID memberUuid = parseUuid(memberId, "Identificador de membro inválido.");
        authorization.assertCanManageMembers(restaurantUuid);
        RestaurantMemberEntity member = members.findByRestaurantAndId(restaurantUuid, memberUuid)
                .orElseThrow(() -> new RestaurantBusinessException("Membro não encontrado."));
        if (member.role == RestaurantMemberRole.OWNER) {
            throw new RestaurantBusinessException("O proprietário não pode ser removido.");
        }
        members.delete(member);
        return true;
    }

    private void requireRestaurant(UUID restaurantId) {
        if (restaurants.findByIdOptional(restaurantId).isEmpty()) {
            throw new RestaurantBusinessException("Restaurante não encontrado.");
        }
    }

    private static void requireInvitableRole(RestaurantMemberRole role) {
        if (role != RestaurantMemberRole.MANAGER && role != RestaurantMemberRole.OPERATOR) {
            throw new RestaurantBusinessException("O convite aceita apenas os papéis MANAGER ou OPERATOR.");
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new RestaurantBusinessException("Informe um e-mail válido para o convite.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static UUID parseUuid(String value, String message) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new RestaurantBusinessException(message);
        }
    }

    private static String newInvitationToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String tokenHash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível.", exception);
        }
    }

    private static RestaurantMemberInvitationView toInvitationView(
            RestaurantMemberInvitationEntity invitation,
            String acceptanceToken,
            Instant now) {
        RestaurantMemberInvitationStatus visibleStatus = invitation.status;
        if (visibleStatus == RestaurantMemberInvitationStatus.PENDING && !invitation.expiresAt.isAfter(now)) {
            visibleStatus = RestaurantMemberInvitationStatus.EXPIRED;
        }
        return new RestaurantMemberInvitationView(
                invitation.id,
                invitation.restaurantId,
                invitation.email,
                invitation.role,
                visibleStatus,
                invitation.expiresAt,
                invitation.createdAt,
                invitation.respondedAt,
                acceptanceToken);
    }

    private static RestaurantMemberView toMemberView(RestaurantMemberEntity member, AppUserEntity user) {
        return new RestaurantMemberView(
                member.id,
                user.id(),
                user.oidcSubject(),
                user.email(),
                user.displayName(),
                member.role,
                member.createdAt);
    }
}
