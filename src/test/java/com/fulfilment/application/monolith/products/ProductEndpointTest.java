package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ProductEndpointTest {
	@Test
	void shouldListSeedProducts() {
		given().when().get("product").then().statusCode(200).body("size()", greaterThanOrEqualTo(3))
				.body(containsString("TONSTAD")).body(containsString("KALLAX")).body(containsString("BESTÅ"));
	}

	@Test
	void shouldCreateReadUpdateAndDeleteProduct() {
		String productName = "TEST-PRODUCT-001";

		Integer id = given().contentType("application/json").body("""
				{"name":"%s","description":"Created by integration test","price":12.50,"stock":7}
				""".formatted(productName)).when().post("product").then().statusCode(201)
				.body("name", equalTo(productName)).extract().path("id");

		given().when().get("product/" + id).then().statusCode(200).body("name", equalTo(productName));

		given().contentType("application/json").body("""
				{"name":"TEST-PRODUCT-001-UPDATED","description":"Updated","price":15.00,"stock":9}
				""").when().put("product/" + id).then().statusCode(200)
				.body("name", equalTo("TEST-PRODUCT-001-UPDATED")).body("stock", equalTo(9));

		given().when().delete("product/" + id).then().statusCode(204);

		given().when().get("product").then().statusCode(200).body(not(containsString("TEST-PRODUCT-001-UPDATED")));
	}

	@Test
	void shouldReturnNotFoundForUnknownProduct() {
		given().when().get("product/999999999").then().statusCode(404);
	}

	@Test
	void shouldRejectUpdateWithoutProductName() {
		given().contentType("application/json").body("""
				{"description":"missing_name","stock":1}
				""").when().put("product/1").then().statusCode(422);
	}
}
