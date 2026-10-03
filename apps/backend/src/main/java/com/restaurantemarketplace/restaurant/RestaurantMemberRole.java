package com.restaurantemarketplace.restaurant;

import org.eclipse.microprofile.graphql.Description;

@Description("Papel de um membro dentro do restaurante.")
public enum RestaurantMemberRole {
    OWNER,
    MANAGER,
    OPERATOR
}
