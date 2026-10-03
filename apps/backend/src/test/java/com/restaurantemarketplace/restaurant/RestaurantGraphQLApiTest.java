package com.restaurantemarketplace.restaurant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

@QuarkusTest
class RestaurantGraphQLApiTest {

    @Test
    @TestSecurity(user = "restaurant-test", roles = { "restaurant-owner", "platform-admin" })
    void shouldRegisterReviewApproveAndActivateRestaurant() {
        String restaurantId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "query": "mutation { createRestaurant(input: { tradeName: \\"Ponto do Sabor\\", legalName: \\"Ponto do Sabor LTDA\\", taxId: \\"12.345.678/0001-95\\", email: \\"contato@pontodosabor.test\\", phone: \\"71999990000\\" }) { id status tradeName taxId } }"
                        }
                        """)
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.createRestaurant.id", notNullValue())
                .body("data.createRestaurant.status", equalTo("DRAFT"))
                .body("data.createRestaurant.tradeName", equalTo("Ponto do Sabor"))
                .body("data.createRestaurant.taxId", equalTo("12345678000195"))
                .extract()
                .path("data.createRestaurant.id");

        completeRestaurantProfile(restaurantId);
        executeStatusMutation("submitRestaurantForReview", restaurantId, "UNDER_REVIEW");
        executeStatusMutation("approveRestaurant", restaurantId, "APPROVED");
        executeStatusMutation("activateRestaurant", restaurantId, "ACTIVE");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"query { restaurantStatusHistory(restaurantId: \\"%s\\") { previousStatus newStatus changedBy } }"}
                        """.formatted(restaurantId))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.restaurantStatusHistory", hasSize(4))
                .body("data.restaurantStatusHistory[0].newStatus", equalTo("DRAFT"))
                .body("data.restaurantStatusHistory[3].newStatus", equalTo("ACTIVE"))
                .body("data.restaurantStatusHistory[3].changedBy", equalTo("restaurant-test"));
    }

    @Test
    @TestSecurity(user = "incomplete-profile-owner", roles = "restaurant-owner")
    void shouldNotSubmitAnIncompleteRestaurantProfile() {
        String restaurantId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "query": "mutation { createRestaurant(input: { tradeName: \\"Cadastro Incompleto\\", legalName: \\"Cadastro Incompleto LTDA\\", taxId: \\"98.765.432/0001-10\\", email: \\"incompleto@restaurante.test\\", phone: \\"71988880000\\" }) { id } }"
                        }
                        """)
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .extract()
                .path("data.createRestaurant.id");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"mutation { submitRestaurantForReview(restaurantId: \\"%s\\") { status } }"}
                        """.formatted(restaurantId))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("errors[0].message", containsString("Pendências"))
                .body("errors[0].extensions.code", equalTo("RESTAURANT_BUSINESS_RULE"))
                .body("data.submitRestaurantForReview", equalTo(null));
    }

    @Test
    @TestSecurity(user = "restaurant-owner-only", roles = "restaurant-owner")
    void shouldRejectApprovalWithoutPlatformAdminRole() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"mutation { approveRestaurant(restaurantId: \\"00000000-0000-0000-0000-000000000000\\") { id } }"}
                        """)
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("errors[0]", notNullValue())
                .body("data.approveRestaurant", equalTo(null));
    }

    @Test
    @TestSecurity(user = "membership-owner@test.local", roles = "restaurant-owner")
    void shouldProvisionLocalUserAndCreateMemberInvitation() {
        String restaurantId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "query": "mutation { createRestaurant(input: { tradeName: \\"Equipe do Sabor\\", legalName: \\"Equipe do Sabor LTDA\\", taxId: \\"11.222.333/0001-81\\", email: \\"equipe@restaurante.test\\", phone: \\"71977770000\\" }) { id } }"
                        }
                        """)
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.createRestaurant.id", notNullValue())
                .extract()
                .path("data.createRestaurant.id");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"query { me { oidcSubject email displayName status } }"}
                        """)
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.me.oidcSubject", equalTo("membership-owner@test.local"))
                .body("data.me.email", equalTo("membership-owner@test.local"))
                .body("data.me.status", equalTo("ACTIVE"));

        String acceptanceToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"mutation { inviteRestaurantMember(restaurantId: \\"%s\\", email: \\"manager@restaurante.local\\", role: MANAGER) { id email role status acceptanceToken expiresAt } }"}
                        """.formatted(restaurantId))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.inviteRestaurantMember.email", equalTo("manager@restaurante.local"))
                .body("data.inviteRestaurantMember.role", equalTo("MANAGER"))
                .body("data.inviteRestaurantMember.status", equalTo("PENDING"))
                .body("data.inviteRestaurantMember.acceptanceToken", notNullValue())
                .extract()
                .path("data.inviteRestaurantMember.acceptanceToken");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"query { restaurantMemberInvitations(restaurantId: \\"%s\\") { email role status acceptanceToken } }"}
                        """.formatted(restaurantId))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.restaurantMemberInvitations", hasSize(1))
                .body("data.restaurantMemberInvitations[0].status", equalTo("PENDING"))
                .body("data.restaurantMemberInvitations[0].acceptanceToken", equalTo(null));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"mutation { acceptRestaurantMemberInvitation(token: \\"%s\\") { id } }"}
                        """.formatted(acceptanceToken))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("errors[0].message", containsString("verificado"))
                .body("data.acceptRestaurantMemberInvitation", equalTo(null));
    }

    private static void executeStatusMutation(String mutation, String restaurantId, String expectedStatus) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"mutation { %s(restaurantId: \\"%s\\") { status } }"}
                        """.formatted(mutation, restaurantId))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.%s.status".formatted(mutation), equalTo(expectedStatus));
    }

    private static void completeRestaurantProfile(String restaurantId) {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"mutation { updateRestaurantAddress(restaurantId: \\"%s\\", input: { postalCode: \\"40000-000\\", street: \\"Rua do Sabor\\", number: \\"100\\", neighborhood: \\"Centro\\", city: \\"Salvador\\", state: \\"BA\\" }) { address { city state postalCode } completeness { complete missingRequirements } } }"}
                        """.formatted(restaurantId))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.updateRestaurantAddress.address.city", equalTo("Salvador"))
                .body("data.updateRestaurantAddress.completeness.complete", equalTo(false));

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"query":"mutation { setRestaurantEstablishmentTypes(restaurantId: \\"%s\\", typeCodes: [\\"RESTAURANT\\", \\"SNACK_BAR\\"]) { establishmentTypes { code name } members { userSubject role } completeness { complete missingRequirements } } }"}
                        """.formatted(restaurantId))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .body("data.setRestaurantEstablishmentTypes.establishmentTypes", hasSize(2))
                .body("data.setRestaurantEstablishmentTypes.members", hasSize(1))
                .body("data.setRestaurantEstablishmentTypes.members[0].role", equalTo("OWNER"))
                .body("data.setRestaurantEstablishmentTypes.completeness.complete", equalTo(true))
                .body("data.setRestaurantEstablishmentTypes.completeness.missingRequirements", hasSize(0));
    }
}
