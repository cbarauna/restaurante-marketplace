package com.restaurantemarketplace.restaurant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class RestaurantMemberRepository implements PanacheRepositoryBase<RestaurantMemberEntity, UUID> {

    List<RestaurantMemberEntity> findByRestaurant(UUID restaurantId) {
        return find("restaurantId", Sort.ascending("createdAt"), restaurantId).list();
    }

    long countOwners(UUID restaurantId) {
        return count("restaurantId = ?1 and role = ?2", restaurantId, RestaurantMemberRole.OWNER);
    }

    Optional<RestaurantMemberEntity> findByRestaurantAndUser(UUID restaurantId, UUID userId) {
        return find("restaurantId = ?1 and userId = ?2", restaurantId, userId).firstResultOptional();
    }

    Optional<RestaurantMemberEntity> findByRestaurantAndId(UUID restaurantId, UUID memberId) {
        return find("restaurantId = ?1 and id = ?2", restaurantId, memberId).firstResultOptional();
    }
}
