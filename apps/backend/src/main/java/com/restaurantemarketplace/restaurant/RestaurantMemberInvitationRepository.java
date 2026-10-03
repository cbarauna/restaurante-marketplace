package com.restaurantemarketplace.restaurant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class RestaurantMemberInvitationRepository
        implements PanacheRepositoryBase<RestaurantMemberInvitationEntity, UUID> {

    Optional<RestaurantMemberInvitationEntity> findPendingByEmail(UUID restaurantId, String email) {
        return find(
                "restaurantId = ?1 and lower(email) = ?2 and status = ?3",
                restaurantId,
                email,
                RestaurantMemberInvitationStatus.PENDING)
                .firstResultOptional();
    }

    Optional<RestaurantMemberInvitationEntity> findPendingByTokenHash(String tokenHash) {
        return find("tokenHash = ?1 and status = ?2", tokenHash, RestaurantMemberInvitationStatus.PENDING)
                .firstResultOptional();
    }

    List<RestaurantMemberInvitationEntity> findByRestaurant(UUID restaurantId) {
        return find("restaurantId", Sort.descending("createdAt"), restaurantId).list();
    }
}
