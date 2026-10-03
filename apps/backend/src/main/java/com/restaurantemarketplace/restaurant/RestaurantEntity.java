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
@Table(name = "restaurant")
public class RestaurantEntity extends PanacheEntityBase {

    @Id
    UUID id;

    @Column(name = "owner_user_id", nullable = false)
    UUID ownerUserId;

    @Column(name = "trade_name", nullable = false, length = 150)
    String tradeName;

    @Column(name = "legal_name", nullable = false, length = 200)
    String legalName;

    @Column(name = "tax_id", nullable = false, unique = true, length = 20)
    String taxId;

    @Column(nullable = false, length = 254)
    String email;

    @Column(nullable = false, length = 32)
    String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    RestaurantStatus status;

    @Column(name = "review_note", length = 2000)
    String reviewNote;

    @Column(name = "submitted_at")
    Instant submittedAt;

    @Column(name = "reviewed_at")
    Instant reviewedAt;

    @Column(name = "activated_at")
    Instant activatedAt;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Version
    @Column(nullable = false)
    long version;
}
