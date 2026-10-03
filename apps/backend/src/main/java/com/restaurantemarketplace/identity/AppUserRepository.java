package com.restaurantemarketplace.identity;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class AppUserRepository implements PanacheRepositoryBase<AppUserEntity, UUID> {

    public Optional<AppUserEntity> findBySubject(String subject) {
        return find("oidcSubject", subject).firstResultOptional();
    }

    public Optional<AppUserEntity> findByEmail(String email) {
        return find("lower(email) = ?1", email.toLowerCase(Locale.ROOT)).firstResultOptional();
    }
}
