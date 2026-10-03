package com.restaurantemarketplace.identity;

import java.util.UUID;

import org.eclipse.microprofile.graphql.Description;

@Description("Usuário local vinculado a uma identidade OIDC.")
public record AppUserView(
        UUID id,
        String oidcSubject,
        String email,
        boolean emailVerified,
        String displayName,
        AppUserStatus status) {
}
