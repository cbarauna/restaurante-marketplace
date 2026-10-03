package com.restaurantemarketplace.restaurant;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "restaurant_status_history")
public class RestaurantStatusHistoryEntity extends PanacheEntityBase {

    @Id
    UUID id;

    @Column(name = "restaurant_id", nullable = false)
    UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 32)
    RestaurantStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 32)
    RestaurantStatus newStatus;

    @Column(length = 2000)
    String reason;

    @Column(name = "changed_by", nullable = false, length = 255)
    String changedBy;

    @Column(name = "changed_at", nullable = false)
    Instant changedAt;
}
