package com.restaurantemarketplace.restaurant;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.graphql.Description;

@Description("Membro vinculado ao restaurante.")
public record RestaurantMemberView(
        UUID id,
        UUID userId,
        String userSubject,
        String email,
        String displayName,
        RestaurantMemberRole role,
        Instant createdAt) {
}
