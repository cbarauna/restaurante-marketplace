package com.restaurantemarketplace.restaurant;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.restaurantemarketplace.identity.AppUserEntity;
import com.restaurantemarketplace.identity.CurrentUserService;

import io.quarkus.security.identity.SecurityIdentity;

@ApplicationScoped
public class RestaurantAuthorization {

    private final SecurityIdentity identity;
    private final CurrentUserService currentUser;
    private final RestaurantMemberRepository members;
    private final boolean authorizationEnabled;

    public RestaurantAuthorization(
            SecurityIdentity identity,
            CurrentUserService currentUser,
            RestaurantMemberRepository members,
            @ConfigProperty(name = "restaurant.security.authorization-enabled") boolean authorizationEnabled) {
        this.identity = identity;
        this.currentUser = currentUser;
        this.members = members;
        this.authorizationEnabled = authorizationEnabled;
    }

    void assertHasRole(String role) {
        if (authorizationEnabled && !identity.hasRole(role)) {
            throw new RestaurantBusinessException("O usuário não possui o papel necessário: " + role + ".");
        }
    }

    void assertAnyRole(String... roles) {
        if (!authorizationEnabled) {
            return;
        }
        for (String role : roles) {
            if (identity.hasRole(role)) {
                return;
            }
        }
        throw new RestaurantBusinessException("O usuário não possui um dos papéis necessários.");
    }

    void assertCanAccess(RestaurantEntity restaurant) {
        if (!authorizationEnabled || identity.hasRole("platform-admin")) {
            return;
        }
        AppUserEntity user = currentUser.getOrCreate();
        if (members.findByRestaurantAndUser(restaurant.id, user.id()).isEmpty()) {
            throw new RestaurantBusinessException("O usuário não pode acessar este restaurante.");
        }
    }

    void assertRestaurantRole(UUID restaurantId, RestaurantMemberRole... allowedRoles) {
        if (!authorizationEnabled || identity.hasRole("platform-admin")) {
            return;
        }
        AppUserEntity user = currentUser.getOrCreate();
        RestaurantMemberEntity member = members.findByRestaurantAndUser(restaurantId, user.id())
                .orElseThrow(() -> new RestaurantBusinessException(
                        "O usuário não pertence a este restaurante."));
        for (RestaurantMemberRole role : allowedRoles) {
            if (member.role == role) {
                return;
            }
        }
        throw new RestaurantBusinessException("O membro não possui permissão para esta operação.");
    }

    void assertCanManageMembers(UUID restaurantId) {
        assertRestaurantRole(restaurantId, RestaurantMemberRole.OWNER);
    }

    String currentActor() {
        return currentUser.currentSubject();
    }
}
