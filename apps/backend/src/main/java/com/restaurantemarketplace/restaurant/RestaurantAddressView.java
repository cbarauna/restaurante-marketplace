package com.restaurantemarketplace.restaurant;

import java.math.BigDecimal;

import org.eclipse.microprofile.graphql.Description;

@Description("Endereço operacional do restaurante.")
public record RestaurantAddressView(
        String postalCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        BigDecimal latitude,
        BigDecimal longitude) {
}
