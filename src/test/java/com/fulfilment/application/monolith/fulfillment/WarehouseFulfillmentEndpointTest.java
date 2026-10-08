package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class WarehouseFulfillmentEndpointTest {

	@Test
	void shouldCreateAndDeleteFulfillmentAssociation() {
		long storeId = createStore();
		long productId = createProduct();

		Long id = create(storeId, productId, 1L);

		given().when().get("fulfillment").then().statusCode(200).body(containsString("\"id\":" + id));
		given().when().delete("fulfillment/" + id).then().statusCode(204);
	}

	@Test
	void shouldAllowAtMostTwoWarehousesForProductPerStore() {
		long storeId = createStore();
		long productId = createProduct();

		Long first = create(storeId, productId, 1L);
		Long second = create(storeId, productId, 2L);

		given().contentType("application/json")
				.body("{" + "\"storeId\":" + storeId + ",\"productId\":" + productId + ",\"warehouseId\":3}").when()
				.post("fulfillment").then().statusCode(400).body(containsString("at most 2 warehouses per store"));

		delete(first);
		delete(second);
	}

	@Test
	void shouldAllowAtMostThreeDifferentWarehousesForStore() {
		long storeId = createStore();
		long product1 = createProduct();
		long product2 = createProduct();
		long product3 = createProduct();
		long product4 = createProduct();

		Long first = create(storeId, product1, 1L);
		Long second = create(storeId, product2, 2L);
		Long third = create(storeId, product3, 3L);

		// Reusing an existing warehouse is allowed because the limit is on different
		// warehouses.
		Long fourth = create(storeId, product4, 1L);

		long product5 = createProduct();
		long fourthWarehouse = createWarehouse();

		given().contentType("application/json")
				.body("{" + "\"storeId\":" + storeId + ",\"productId\":" + product5 + ",\"warehouseId\":"
						+ fourthWarehouse + "}")
				.when().post("fulfillment").then().statusCode(400)
				.body(containsString("at most 3 different warehouses"));

		delete(first);
		delete(second);
		delete(third);
		delete(fourth);
	}

	@Test
	void shouldAllowAtMostFiveDifferentProductsPerWarehouse() {

		long storeId = createStore();
		long warehouseId = createWarehouse();

		Long[] associations = new Long[5];
		for (int i = 0; i < 5; i++) {
			associations[i] = create(storeId, createProduct(), warehouseId);
		}

		long sixthProduct = createProduct();
		given().contentType("application/json")
				.body("{\"storeId\":" + storeId + ",\"productId\":" + sixthProduct + ",\"warehouseId\":" + warehouseId
						+ "}")
				.when().post("fulfillment").then().statusCode(400).body(containsString("at most 5 different products"));

		for (Long association : associations) {
			delete(association);
		}
	}

	@Test
	void shouldRejectUnknownEntitiesAndDuplicateAssociation() {
		long storeId = createStore();
		long productId = createProduct();

		Long association = create(storeId, productId, 1L);

		given().contentType("application/json")
				.body("{" + "\"storeId\":" + storeId + ",\"productId\":" + productId + ",\"warehouseId\":1}").when()
				.post("fulfillment").then().statusCode(400).body(containsString("association already exists"));

		given().contentType("application/json").body("""
				{"storeId":999999,"productId":1,"warehouseId":1}
				""").when().post("fulfillment").then().statusCode(400).body(containsString("Store does not exist"));

		given().contentType("application/json")
				.body("{" + "\"storeId\":" + storeId + ",\"productId\":999999,\"warehouseId\":1}").when()
				.post("fulfillment").then().statusCode(400).body(containsString("Product does not exist"));

		delete(association);
	}

	private void delete(Long id) {
		given().when().delete("fulfillment/" + id).then().statusCode(204);
	}

	private Long create(long storeId, long productId, long warehouseId) {
		Integer id = given().contentType("application/json")
				.body("{" + "\"storeId\":" + storeId + ",\"productId\":" + productId + ",\"warehouseId\":" + warehouseId
						+ "}")
				.when().post("fulfillment").then().statusCode(201).body("storeId", equalTo((int) storeId))
				.body("productId", equalTo((int) productId)).body("warehouseId", equalTo((int) warehouseId)).extract()
				.path("id");
		return id.longValue();
	}

	private long createStore() {
		String name = "BONUS-STORE-" + UUID.randomUUID().toString().substring(0, 8);
		Integer id = given().contentType("application/json")
				.body("{\"name\":\"" + name + "\",\"quantityProductsInStock\":0}").when().post("store").then()
				.statusCode(201).extract().path("id");
		return id.longValue();
	}

	private long createWarehouse() {
		String buCode = "BONUS-WH-" + UUID.randomUUID().toString().substring(0, 8);
		String id = given().contentType("application/json")
				.body("{\"businessUnitCode\":\"" + buCode
						+ "\",\"location\":\"ZWOLLE-002\",\"capacity\":10,\"stock\":1}")
				.when().post("warehouse").then().statusCode(201).extract().path("id");
		return Long.parseLong(id);
	}

	private long createProduct() {
		String name = "BONUS-PROD-" + UUID.randomUUID().toString().substring(0, 8);
		Integer id = given().contentType("application/json").body("{\"name\":\"" + name + "\",\"stock\":1}").when()
				.post("product").then().statusCode(201).extract().path("id");
		return id.longValue();
	}
}
