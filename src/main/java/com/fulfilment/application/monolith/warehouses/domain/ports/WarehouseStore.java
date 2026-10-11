package com.fulfilment.application.monolith.warehouses.domain.ports;

import java.util.List;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

public interface WarehouseStore {

	List<Warehouse> getAll();

	void create(Warehouse warehouse);

	void update(Warehouse warehouse);

	void remove(Warehouse warehouse);

	Warehouse findByBusinessUnitCode(String buCode);

	Warehouse findByDatabaseId(Long id);
}
