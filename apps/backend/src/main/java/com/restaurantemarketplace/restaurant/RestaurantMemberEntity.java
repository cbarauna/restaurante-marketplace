package com.restaurantemarketplace.restaurant;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(
        name = "restaurant_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_restaurant_member",
                columnNames = { "restaurant_id", "user_id" }))
public class RestaurantMemberEntity extends PanacheEntityBase {

    @Id
    UUID id;

    @Column(name = "restaurant_id", nullable = false)
    UUID restaurantId;

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    RestaurantMemberRole role;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}
