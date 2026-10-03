package com.restaurantemarketplace.restaurant;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.quarkus.security.Authenticated;

@GraphQLApi
@ApplicationScoped
public class RestaurantMembershipGraphQLApi {

    private final RestaurantMembershipService service;

    public RestaurantMembershipGraphQLApi(RestaurantMembershipService service) {
        this.service = service;
    }

    @Query
    @Authenticated
    public List<RestaurantMemberInvitationView> restaurantMemberInvitations(
            @Name("restaurantId") @NotBlank String restaurantId) {
        return service.list(restaurantId);
    }

    @Mutation
    @Authenticated
    public RestaurantMemberInvitationView inviteRestaurantMember(
            @Name("restaurantId") @NotBlank String restaurantId,
            @Email @NotBlank String email,
            @NotNull RestaurantMemberRole role) {
        return service.invite(restaurantId, email, role);
    }

    @Mutation
    @Authenticated
    public RestaurantMemberView acceptRestaurantMemberInvitation(@NotBlank String token) {
        return service.accept(token);
    }

    @Mutation
    @Authenticated
    public RestaurantMemberInvitationView revokeRestaurantMemberInvitation(
            @Name("invitationId") @NotBlank String invitationId) {
        return service.revoke(invitationId);
    }

    @Mutation
    @Authenticated
    public RestaurantMemberView updateRestaurantMemberRole(
            @Name("restaurantId") @NotBlank String restaurantId,
            @Name("memberId") @NotBlank String memberId,
            @NotNull RestaurantMemberRole role) {
        return service.updateRole(restaurantId, memberId, role);
    }

    @Mutation
    @Authenticated
    public boolean removeRestaurantMember(
            @Name("restaurantId") @NotBlank String restaurantId,
            @Name("memberId") @NotBlank String memberId) {
        return service.remove(restaurantId, memberId);
    }
}
