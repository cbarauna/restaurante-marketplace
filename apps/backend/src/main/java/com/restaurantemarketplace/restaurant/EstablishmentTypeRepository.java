package com.restaurantemarketplace.restaurant;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class EstablishmentTypeRepository implements PanacheRepositoryBase<EstablishmentTypeEntity, String> {

    List<EstablishmentTypeEntity> findActive() {
        return list("active", Sort.ascending("displayName"), true);
    }
}
