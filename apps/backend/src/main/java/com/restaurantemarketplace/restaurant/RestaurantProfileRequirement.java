package com.restaurantemarketplace.restaurant;

import org.eclipse.microprofile.graphql.Description;

@Description("Parte obrigatória do perfil para envio à análise.")
public enum RestaurantProfileRequirement {
    ADDRESS,
    ESTABLISHMENT_TYPE,
    OWNER
}
