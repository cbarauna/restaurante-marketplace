package com.restaurantemarketplace.restaurant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class RestaurantRepository implements PanacheRepositoryBase<RestaurantEntity, UUID> {

    Optional<RestaurantEntity> findByTaxId(String taxId) {
        return find("taxId", taxId).firstResultOptional();
    }

    List<RestaurantEntity> findUnderReview() {
        return find("status", Sort.ascending("submittedAt"), RestaurantStatus.UNDER_REVIEW).list();
    }
}
