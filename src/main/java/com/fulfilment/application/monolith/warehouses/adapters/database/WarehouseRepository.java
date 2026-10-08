package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

	@Override
	public List<Warehouse> getAll() {
		return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
	}

	@Override
	public void create(Warehouse warehouse) {
		var entity = new DbWarehouse();
		entity.businessUnitCode = warehouse.businessUnitCode;
		entity.location = warehouse.location;
		entity.capacity = warehouse.capacity;
		entity.stock = warehouse.stock;
		entity.createdAt = warehouse.createdAt;
		entity.archivedAt = warehouse.archivedAt;
		this.persistAndFlush(entity);
		warehouse.id = entity.id;
	}

	@Override
	public void update(Warehouse warehouse) {
		DbWarehouse entity = null;
		if (warehouse.id != null) {
			entity = find("id", warehouse.id).firstResult();
		}
		if (entity == null && warehouse.businessUnitCode != null) {
			entity = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
		}
		if (entity == null) {
			throw new IllegalArgumentException("Warehouse not found");
		}

		entity.businessUnitCode = warehouse.businessUnitCode;
		entity.location = warehouse.location;
		entity.capacity = warehouse.capacity;
		entity.stock = warehouse.stock;
		entity.createdAt = warehouse.createdAt;
		entity.archivedAt = warehouse.archivedAt;
		warehouse.id = entity.id;
	}

	@Override
	public void remove(Warehouse warehouse) {
		DbWarehouse entity = warehouse.id != null ? find("id", warehouse.id).firstResult() : null;
		if (entity == null && warehouse.businessUnitCode != null) {
			entity = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
		}
		if (entity != null) {
			this.delete(entity);
		}
	}

	@Override
	public Warehouse findByBusinessUnitCode(String buCode) {
		if (buCode == null) {
			return null;
		}
		DbWarehouse entity = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
		return entity == null ? null : entity.toWarehouse();
	}

	@Override
	public Warehouse findByDatabaseId(Long id) {
		if (id == null) {
			return null;
		}
		DbWarehouse entity = find("id", id).firstResult();
		return entity == null ? null : entity.toWarehouse();
	}
}
