package com.restaurantemarketplace.restaurant;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.graphql.Description;

@Description("Dados complementares necessários para analisar o restaurante.")
public record RestaurantProfileView(
        UUID restaurantId,
        RestaurantAddressView address,
        List<EstablishmentTypeView> establishmentTypes,
        List<RestaurantMemberView> members,
        RestaurantProfileCompletenessView completeness) {
}
