package com.restaurantemarketplace.restaurant;

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
@Table(name = "restaurant_member_invitation")
public class RestaurantMemberInvitationEntity extends PanacheEntityBase {

    @Id
    UUID id;

    @Column(name = "restaurant_id", nullable = false)
    UUID restaurantId;

    @Column(nullable = false, length = 254)
    String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    RestaurantMemberRole role;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    RestaurantMemberInvitationStatus status;

    @Column(name = "invited_by_user_id", nullable = false)
    UUID invitedByUserId;

    @Column(name = "accepted_by_user_id")
    UUID acceptedByUserId;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "responded_at")
    Instant respondedAt;

    @Version
    @Column(nullable = false)
    long version;
}
