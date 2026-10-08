package com.fulfilment.application.monolith.warehouses.domain.usecases;

import java.time.LocalDateTime;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

	private final WarehouseStore warehouseStore;
	private final LocationResolver locationResolver;

	public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
		this.warehouseStore = warehouseStore;
		this.locationResolver = locationResolver;
	}

	@Override
	public void create(Warehouse warehouse) {
		validate(warehouse);

		if (warehouse.createdAt == null) {
			warehouse.createdAt = LocalDateTime.now();
		}
		warehouse.archivedAt = null;
		warehouseStore.create(warehouse);
	}

	private void validate(Warehouse warehouse) {
		if (warehouse == null) {
			throw new IllegalArgumentException("Warehouse must be provided");
		}
		if (isBlank(warehouse.businessUnitCode)) {
			throw new IllegalArgumentException("Business unit code must be provided");
		}
		if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
			throw new IllegalArgumentException("Business unit code already exists");
		}

		Location location = locationResolver.resolveByIdentifier(warehouse.location);
		if (location == null) {
			throw new IllegalArgumentException("Location does not exist");
		}
		if (warehouse.capacity == null || warehouse.capacity <= 0) {
			throw new IllegalArgumentException("Capacity must be greater than zero");
		}
		if (warehouse.stock == null || warehouse.stock < 0) {
			throw new IllegalArgumentException("Stock cannot be negative");
		}
		if (warehouse.stock > warehouse.capacity) {
			throw new IllegalArgumentException("Warehouse capacity cannot be lower than stock");
		}
		if (warehouse.capacity > location.maxCapacity) {
			throw new IllegalArgumentException("Warehouse capacity exceeds location maximum capacity");
		}

		var activeWarehouses = warehouseStore.getAll().stream().filter(w -> w.archivedAt == null)
				.filter(w -> warehouse.location.equals(w.location)).toList();

		if (activeWarehouses.size() >= location.maxNumberOfWarehouses) {
			throw new IllegalArgumentException("Maximum number of warehouses reached for location");
		}

		int totalCapacity = activeWarehouses.stream().map(Warehouse::getCapacitySafely).mapToInt(Integer::intValue)
				.sum();
		if (totalCapacity + warehouse.capacity > location.maxCapacity) {
			throw new IllegalArgumentException("Total warehouse capacity exceeds location maximum capacity");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
