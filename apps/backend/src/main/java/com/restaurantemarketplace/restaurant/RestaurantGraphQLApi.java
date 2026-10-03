package com.restaurantemarketplace.restaurant;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.quarkus.security.Authenticated;

@GraphQLApi
@ApplicationScoped
public class RestaurantGraphQLApi {

    private final RestaurantService service;

    public RestaurantGraphQLApi(RestaurantService service) {
        this.service = service;
    }

    @Mutation
    @RolesAllowed("restaurant-owner")
    @Description("Cria um cadastro de restaurante em estado de rascunho.")
    public RestaurantView createRestaurant(@Valid CreateRestaurantInput input) {
        return service.create(input);
    }

    @Query("restaurant")
    @Authenticated
    public RestaurantView restaurant(@Name("id") @NotBlank String restaurantId) {
        return service.find(restaurantId);
    }

    @Query
    @Authenticated
    public List<RestaurantStatusHistoryView> restaurantStatusHistory(
            @Name("restaurantId") @NotBlank String restaurantId) {
        return service.history(restaurantId);
    }

    @Query
    @RolesAllowed("platform-admin")
    public List<RestaurantView> restaurantsUnderReview() {
        return service.underReview();
    }

    @Mutation
    @Authenticated
    public RestaurantView submitRestaurantForReview(
            @Name("restaurantId") @NotBlank String restaurantId) {
        return service.submit(restaurantId);
    }

    @Mutation
    @RolesAllowed("platform-admin")
    public RestaurantView approveRestaurant(@Name("restaurantId") @NotBlank String restaurantId) {
        return service.approve(restaurantId);
    }

    @Mutation
    @RolesAllowed("platform-admin")
    public RestaurantView rejectRestaurant(
            @Name("restaurantId") @NotBlank String restaurantId,
            @NotBlank @Size(max = 2000) String reason) {
        return service.reject(restaurantId, reason);
    }

    @Mutation
    @RolesAllowed("platform-admin")
    public RestaurantView requestRestaurantChanges(
            @Name("restaurantId") @NotBlank String restaurantId,
            @NotBlank @Size(max = 2000) String reason) {
        return service.requestChanges(restaurantId, reason);
    }

    @Mutation
    @RolesAllowed("platform-admin")
    public RestaurantView activateRestaurant(@Name("restaurantId") @NotBlank String restaurantId) {
        return service.activate(restaurantId);
    }

    @Mutation
    @RolesAllowed("platform-admin")
    public RestaurantView suspendRestaurant(
            @Name("restaurantId") @NotBlank String restaurantId,
            @NotBlank @Size(max = 2000) String reason) {
        return service.suspend(restaurantId, reason);
    }

    @Mutation
    @RolesAllowed("platform-admin")
    public RestaurantView reactivateRestaurant(@Name("restaurantId") @NotBlank String restaurantId) {
        return service.reactivate(restaurantId);
    }
}
