package com.restaurantemarketplace.identity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "app_user")
public class AppUserEntity extends PanacheEntityBase {

    @Id
    UUID id;

    @Column(name = "oidc_subject", nullable = false, unique = true, length = 255)
    String oidcSubject;

    @Column(unique = true, length = 254)
    String email;

    @Column(name = "email_verified", nullable = false)
    boolean emailVerified;

    @Column(name = "display_name", nullable = false, length = 150)
    String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    AppUserStatus status;

    @Column(name = "last_authenticated_at", nullable = false)
    Instant lastAuthenticatedAt;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Version
    @Column(nullable = false)
    long version;

    public UUID id() {
        return id;
    }

    public String oidcSubject() {
        return oidcSubject;
    }

    public String email() {
        return email;
    }

    public String displayName() {
        return displayName;
    }

    public boolean emailVerified() {
        return emailVerified;
    }

    public AppUserStatus status() {
        return status;
    }
}
