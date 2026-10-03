package com.restaurantemarketplace.restaurant;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(
        name = "restaurant_establishment_type",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_restaurant_establishment_type",
                columnNames = { "restaurant_id", "establishment_type_code" }))
public class RestaurantEstablishmentTypeEntity extends PanacheEntityBase {

    @Id
    UUID id;

    @Column(name = "restaurant_id", nullable = false)
    UUID restaurantId;

    @Column(name = "establishment_type_code", nullable = false, length = 50)
    String establishmentTypeCode;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}
