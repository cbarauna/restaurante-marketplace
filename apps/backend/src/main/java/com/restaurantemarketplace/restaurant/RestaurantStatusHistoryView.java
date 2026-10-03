package com.restaurantemarketplace.restaurant;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.graphql.Description;

@Description("Registro auditável de uma mudança de estado do restaurante.")
public record RestaurantStatusHistoryView(
        UUID id,
        RestaurantStatus previousStatus,
        RestaurantStatus newStatus,
        String reason,
        String changedBy,
        Instant changedAt) {
}
