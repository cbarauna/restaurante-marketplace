package com.restaurantemarketplace.restaurant;

import org.eclipse.microprofile.graphql.Description;

@Description("Estado atual do cadastro e da operação do restaurante.")
public enum RestaurantStatus {
    DRAFT,
    UNDER_REVIEW,
    CHANGES_REQUESTED,
    APPROVED,
    REJECTED,
    ACTIVE,
    SUSPENDED
}
