package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class WarehouseFulfillmentService {

	private static final int MAX_WAREHOUSES_PER_PRODUCT_AND_STORE = 2;
	private static final int MAX_WAREHOUSES_PER_STORE = 3;
	private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

	@Inject
	WarehouseFulfillmentRepository fulfillmentRepository;

	@Inject
	ProductRepository productRepository;

	@Inject
	WarehouseRepository warehouseRepository;

	@Transactional
	public WarehouseFulfillment create(Long storeId, Long productId, Long warehouseId) {
		validateIds(storeId, productId, warehouseId);

		if (Store.findById(storeId) == null) {
			throw new IllegalArgumentException("Store does not exist");
		}
		if (productRepository.findById(productId) == null) {
			throw new IllegalArgumentException("Product does not exist");
		}

		Warehouse warehouse = warehouseRepository.findByDatabaseId(warehouseId);
		if (warehouse == null) {
			throw new IllegalArgumentException("Warehouse does not exist");
		}
		if (warehouse.archivedAt != null) {
			throw new IllegalArgumentException("Archived warehouse cannot be used for fulfillment");
		}

		if (!fulfillmentRepository
				.find("storeId = ?1 and productId = ?2 and warehouseId = ?3", storeId, productId, warehouseId).list()
				.isEmpty()) {
			throw new IllegalArgumentException("Fulfillment association already exists");
		}

		long warehousesForProductAndStore = fulfillmentRepository
				.find("storeId = ?1 and productId = ?2", storeId, productId).list().stream()
				.map(association -> association.warehouseId).distinct().count();
		if (warehousesForProductAndStore >= MAX_WAREHOUSES_PER_PRODUCT_AND_STORE) {
			throw new IllegalArgumentException("A product can be fulfilled by at most 2 warehouses per store");
		}

		var warehousesForStore = fulfillmentRepository.find("storeId", storeId).list().stream()
				.map(association -> association.warehouseId).collect(java.util.stream.Collectors.toSet());
		if (!warehousesForStore.contains(warehouseId) && warehousesForStore.size() >= MAX_WAREHOUSES_PER_STORE) {
			throw new IllegalArgumentException("A store can be fulfilled by at most 3 different warehouses");
		}

		long productsForWarehouse = fulfillmentRepository.find("warehouseId", warehouseId).list().stream()
				.map(association -> association.productId).collect(java.util.stream.Collectors.toSet()).size();
		if (productsForWarehouse >= MAX_PRODUCTS_PER_WAREHOUSE) {
			throw new IllegalArgumentException("A warehouse can store at most 5 different products");
		}

		WarehouseFulfillment association = new WarehouseFulfillment(storeId, productId, warehouseId);
		fulfillmentRepository.persistAndFlush(association);
		return association;
	}

	@Transactional
	public void delete(Long id) {
		WarehouseFulfillment association = fulfillmentRepository.findById(id);
		if (association == null) {
			throw new IllegalArgumentException("Fulfillment association does not exist");
		}
		fulfillmentRepository.delete(association);
	}

	private void validateIds(Long storeId, Long productId, Long warehouseId) {
		if (storeId == null || productId == null || warehouseId == null) {
			throw new IllegalArgumentException("storeId, productId and warehouseId are required");
		}
	}
}
