package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class StoreEndpointTest {

	@Test
	void shouldListStores() {
		given().when().get("store").then().statusCode(200).body("size()", greaterThanOrEqualTo(3))
				.body(containsString("TONSTAD")).body(containsString("KALLAX")).body(containsString("BESTÅ"));
	}

	@Test
	void shouldGetStoreById() {
		given().when().get("store/1").then().statusCode(200).body("id", equalTo(1)).body("name", equalTo("TONSTAD"));
	}

	@Test
	void shouldReturn404ForUnknownStore() {
		given().when().get("store/999999999").then().statusCode(404);
	}

	@Test
	void shouldCreateStore() {
		String name = "TEST-" + UUID.randomUUID().toString().substring(0, 8);

		given().contentType("application/json").body("""
				{
				  "name": "%s",
				  "quantityProductsInStock": 10
				}
				""".formatted(name)).when().post("store").then().statusCode(201).body("name", equalTo(name))
				.body("quantityProductsInStock", equalTo(10));
	}

	@Test
	void shouldRejectCreateWhenIdIsProvided() {
		given().contentType("application/json").body("""
				{
				  "id": 999,
				  "name": "INVALID-STORE",
				  "quantityProductsInStock": 10
				}
				""").when().post("store").then().statusCode(422);
	}

	@Test
	void shouldUpdateStore() {
		Integer id = createStore();

		given().contentType("application/json").body("""
				{
				  "name": "UPDATED-STORE",
				  "quantityProductsInStock": 25
				}
				""").when().put("store/" + id).then().statusCode(200).body("name", equalTo("UPDATED-STORE"))
				.body("quantityProductsInStock", equalTo(25));
	}

	@Test
	void shouldRejectUpdateWithoutName() {
		given().contentType("application/json").body("""
				{
				  "quantityProductsInStock": 25
				}
				""").when().put("store/1").then().statusCode(422);
	}

	@Test
	void shouldReturn404WhenUpdatingUnknownStore() {
		given().contentType("application/json").body("""
				{
				  "name": "UPDATED",
				  "quantityProductsInStock": 10
				}
				""").when().put("store/999999999").then().statusCode(404);
	}

	@Test
	void shouldPatchStore() {
		Integer id = createStore();

		given().contentType("application/json").body("""
				{
				  "name": "PATCHED-STORE",
				  "quantityProductsInStock": 30
				}
				""").when().patch("store/" + id).then().statusCode(200).body("name", equalTo("PATCHED-STORE"))
				.body("quantityProductsInStock", equalTo(30));
	}

	@Test
	void shouldRejectPatchWithoutName() {
		given().contentType("application/json").body("""
				{
				  "quantityProductsInStock": 10
				}
				""").when().patch("store/1").then().statusCode(422);
	}

	@Test
	void shouldReturn404WhenPatchingUnknownStore() {
		given().contentType("application/json").body("""
				{
				  "name": "PATCHED",
				  "quantityProductsInStock": 10
				}
				""").when().patch("store/999999999").then().statusCode(404);
	}

	@Test
	void shouldDeleteStore() {
		Integer id = createStore();

		given().when().delete("store/" + id).then().statusCode(204);

		given().when().get("store/" + id).then().statusCode(404);
	}

	@Test
	void shouldReturn404WhenDeletingUnknownStore() {
		given().when().delete("store/999999999").then().statusCode(404);
	}

	private Integer createStore() {
		String name = "TEST-" + UUID.randomUUID().toString().substring(0, 8);

		return given().contentType("application/json").body("""
				{
				  "name": "%s",
				  "quantityProductsInStock": 10
				}
				""".formatted(name)).when().post("store").then().statusCode(201).extract().path("id");
	}
}