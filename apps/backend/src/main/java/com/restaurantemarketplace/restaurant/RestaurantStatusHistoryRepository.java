package com.restaurantemarketplace.restaurant;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class RestaurantStatusHistoryRepository
        implements PanacheRepositoryBase<RestaurantStatusHistoryEntity, UUID> {

    List<RestaurantStatusHistoryEntity> findByRestaurant(UUID restaurantId) {
        return find("restaurantId", Sort.ascending("changedAt"), restaurantId).list();
    }
}
