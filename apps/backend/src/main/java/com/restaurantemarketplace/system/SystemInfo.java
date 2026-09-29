package com.restaurantemarketplace.system;

import org.eclipse.microprofile.graphql.Description;

@Description("Informações básicas da aplicação.")
public record SystemInfo(String name, String version, String status) {
}
