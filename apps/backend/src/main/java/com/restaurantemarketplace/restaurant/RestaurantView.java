package com.restaurantemarketplace.restaurant;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.graphql.Description;

@Description("Visão do cadastro de um restaurante.")
public record RestaurantView(
        UUID id,
        String tradeName,
        String legalName,
        String taxId,
        String email,
        String phone,
        RestaurantStatus status,
        String reviewNote,
        Instant submittedAt,
        Instant reviewedAt,
        Instant activatedAt,
        Instant createdAt,
        Instant updatedAt,
        long version) {
}
