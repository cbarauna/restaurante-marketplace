package com.restaurantemarketplace.restaurant;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

@ApplicationScoped
public class RestaurantAddressRepository implements PanacheRepositoryBase<RestaurantAddressEntity, UUID> {
}
