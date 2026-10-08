package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class WarehouseEndpointIT {

	@Test
	void shouldListSeedWarehouses() {
		given().when().get("warehouse").then().statusCode(200).body("size()", greaterThanOrEqualTo(3))
				.body(containsString("MWH.001")).body(containsString("MWH.012")).body(containsString("MWH.023"));
	}

	@Test
	void shouldCreateGetAndArchiveWarehouse() {
		String buCode = "IT.CREATE.001";
		String id = given().contentType("application/json").body("""
				{"businessUnitCode":"%s","location":"ZWOLLE-002","capacity":30,"stock":10}
				""".formatted(buCode)).when().post("warehouse").then().statusCode(201)
				.body("businessUnitCode", equalTo(buCode)).extract().path("id");

		given().when().get("warehouse/" + id).then().statusCode(200).body("businessUnitCode", equalTo(buCode));

		given().when().delete("warehouse/" + id).then().statusCode(204);

		given().when().get("warehouse/" + id).then().statusCode(200).body("businessUnitCode", equalTo(buCode));
	}

	@Test
	void shouldReplaceWarehouseAndPreserveBusinessUnitCode() {
		String buCode = "IT.REPLACE.001";

		given().contentType("application/json").body("""
				{"businessUnitCode":"%s","location":"AMSTERDAM-001","capacity":20,"stock":5}
				""".formatted(buCode)).when().post("warehouse").then().statusCode(201);

		given().contentType("application/json").body("""
				{"businessUnitCode":"%s","location":"AMSTERDAM-001","capacity":30,"stock":5}
				""".formatted(buCode)).when().post("warehouse/" + buCode + "/replacement").then().statusCode(200)
				.body("businessUnitCode", equalTo(buCode)).body("capacity", equalTo(30)).body("stock", equalTo(5));
	}

	@Test
	void shouldRejectInvalidWarehouseCreation() {
		given().contentType("application/json").body("""
				{"businessUnitCode":"IT.INVALID.001","location":"UNKNOWN-001","capacity":20,"stock":5}
				""").when().post("warehouse").then().statusCode(400).body(containsString("Location does not exist"));
	}

	@Test
	void shouldReturnNotFoundForUnknownWarehouse() {
		given().when().get("warehouse/999999999").then().statusCode(404);
	}

	@Test
	void shouldRejectReplacementWhenStockDoesNotMatch() {
		given().contentType("application/json").body("""
				{"businessUnitCode":"MWH.012","location":"AMSTERDAM-001","capacity":60,"stock":999}
				""").when().post("warehouse/MWH.012/replacement").then().statusCode(400)
				.body(containsString("Replacement stock must match current warehouse stock"));
	}
}
