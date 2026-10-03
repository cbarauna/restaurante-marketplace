package com.restaurantemarketplace.identity;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("IDENTITY_BUSINESS_RULE")
public class IdentityBusinessException extends RuntimeException {

    public IdentityBusinessException(String message) {
        super(message);
    }
}
