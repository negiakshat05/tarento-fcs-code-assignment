package com.fulfilment.application.monolith.warehouses.domain.usecases;

import java.time.LocalDateTime;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

	private final WarehouseStore warehouseStore;
	private final LocationResolver locationResolver;

	public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
		this.warehouseStore = warehouseStore;
		this.locationResolver = locationResolver;
	}

	@Override
	public void replace(Warehouse newWarehouse) {
		if (newWarehouse == null || newWarehouse.businessUnitCode == null) {
			throw new IllegalArgumentException("Business unit code must be provided");
		}

		Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
		if (current == null) {
			throw new IllegalArgumentException("Warehouse not found");
		}

		if (newWarehouse.location == null || !newWarehouse.location.equals(current.location)) {
			throw new IllegalArgumentException("Replacement warehouse must use the same location");
		}

		Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
		if (location == null) {
			throw new IllegalArgumentException("Location does not exist");
		}
		if (newWarehouse.capacity == null || newWarehouse.capacity <= 0) {
			throw new IllegalArgumentException("Capacity must be greater than zero");
		}
		if (newWarehouse.stock == null || newWarehouse.stock < 0) {
			throw new IllegalArgumentException("Stock cannot be negative");
		}
		if (!newWarehouse.stock.equals(current.stock)) {
			throw new IllegalArgumentException("Replacement stock must match current warehouse stock");
		}
		if (newWarehouse.capacity < current.stock) {
			throw new IllegalArgumentException("Replacement capacity cannot be lower than current stock");
		}
		if (newWarehouse.stock > newWarehouse.capacity) {
			throw new IllegalArgumentException("Warehouse capacity cannot be lower than stock");
		}
		if (newWarehouse.capacity > location.maxCapacity) {
			throw new IllegalArgumentException("Warehouse capacity exceeds location maximum capacity");
		}

		current.archivedAt = LocalDateTime.now();
		warehouseStore.update(current);

		newWarehouse.id = null;
		newWarehouse.archivedAt = null;
		newWarehouse.createdAt = LocalDateTime.now();
		warehouseStore.create(newWarehouse);
	}
}
