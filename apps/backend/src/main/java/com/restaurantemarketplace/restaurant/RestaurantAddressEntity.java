package com.restaurantemarketplace.restaurant;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "restaurant_address")
public class RestaurantAddressEntity extends PanacheEntityBase {

    @Id
    @Column(name = "restaurant_id")
    UUID restaurantId;

    @Column(name = "postal_code", nullable = false, length = 9)
    String postalCode;

    @Column(nullable = false, length = 200)
    String street;

    @Column(nullable = false, length = 30)
    String number;

    @Column(length = 120)
    String complement;

    @Column(nullable = false, length = 120)
    String neighborhood;

    @Column(nullable = false, length = 120)
    String city;

    @Column(nullable = false, length = 2)
    String state;

    @Column(precision = 9, scale = 6)
    BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    BigDecimal longitude;

    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Version
    @Column(nullable = false)
    long version;
}
