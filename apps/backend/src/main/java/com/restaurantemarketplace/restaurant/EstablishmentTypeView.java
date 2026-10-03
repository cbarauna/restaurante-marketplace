package com.restaurantemarketplace.restaurant;

import org.eclipse.microprofile.graphql.Description;

@Description("Tipo pesquisável de estabelecimento.")
public record EstablishmentTypeView(String code, String name) {
}
