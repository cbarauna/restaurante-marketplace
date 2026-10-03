package com.restaurantemarketplace.restaurant;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.microprofile.graphql.Description;

@Description("Convite para ingresso na equipe de um restaurante.")
public record RestaurantMemberInvitationView(
        UUID id,
        UUID restaurantId,
        String email,
        RestaurantMemberRole role,
        RestaurantMemberInvitationStatus status,
        Instant expiresAt,
        Instant createdAt,
        Instant respondedAt,
        String acceptanceToken) {
}
