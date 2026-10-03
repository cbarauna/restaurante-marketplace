package com.restaurantemarketplace.restaurant;

import java.util.List;

import org.eclipse.microprofile.graphql.Description;

@Description("Resultado da validação de completude do perfil.")
public record RestaurantProfileCompletenessView(
        boolean complete,
        List<RestaurantProfileRequirement> missingRequirements) {
}
