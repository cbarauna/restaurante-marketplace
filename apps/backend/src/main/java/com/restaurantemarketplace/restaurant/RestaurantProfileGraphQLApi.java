package com.restaurantemarketplace.restaurant;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.quarkus.security.Authenticated;

@GraphQLApi
@ApplicationScoped
public class RestaurantProfileGraphQLApi {

    private final RestaurantProfileService service;

    public RestaurantProfileGraphQLApi(RestaurantProfileService service) {
        this.service = service;
    }

    @Query
    @Authenticated
    public RestaurantProfileView restaurantProfile(
            @Name("restaurantId") @NotBlank String restaurantId) {
        return service.find(restaurantId);
    }

    @Query
    @Authenticated
    public List<EstablishmentTypeView> establishmentTypes() {
        return service.availableEstablishmentTypes();
    }

    @Mutation("updateRestaurantAddress")
    @Authenticated
    public RestaurantProfileView updateRestaurantAddress(
            @Name("restaurantId") @NotBlank String restaurantId,
            @Valid RestaurantAddressInput input) {
        return service.updateAddress(restaurantId, input);
    }

    @Mutation("setRestaurantEstablishmentTypes")
    @Authenticated
    public RestaurantProfileView setRestaurantEstablishmentTypes(
            @Name("restaurantId") @NotBlank String restaurantId,
            @NotEmpty @Size(max = 10) List<@NotBlank String> typeCodes) {
        return service.setEstablishmentTypes(restaurantId, typeCodes);
    }
}
