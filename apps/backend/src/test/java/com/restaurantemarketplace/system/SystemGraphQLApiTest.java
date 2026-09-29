package com.restaurantemarketplace.system;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class SystemGraphQLApiTest {

    @Test
    void shouldReturnSystemInformation() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"query { systemInfo { name version status } }"}
                        """)
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.systemInfo.name", equalTo("restaurante-marketplace-backend"))
                .body("data.systemInfo.version", equalTo("0.1.0-SNAPSHOT"))
                .body("data.systemInfo.status", equalTo("UP"));
    }
}
