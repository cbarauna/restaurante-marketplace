package com.restaurantemarketplace.restaurant;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("RESTAURANT_BUSINESS_RULE")
public class RestaurantBusinessException extends RuntimeException {

    public RestaurantBusinessException(String message) {
        super(message);
    }
}
