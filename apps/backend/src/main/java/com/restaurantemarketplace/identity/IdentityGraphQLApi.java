package com.restaurantemarketplace.identity;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.quarkus.security.Authenticated;

@GraphQLApi
@ApplicationScoped
public class IdentityGraphQLApi {

    private final CurrentUserService currentUser;

    public IdentityGraphQLApi(CurrentUserService currentUser) {
        this.currentUser = currentUser;
    }

    @Query("me")
    @Authenticated
    public AppUserView me() {
        return currentUser.currentUser();
    }
}
