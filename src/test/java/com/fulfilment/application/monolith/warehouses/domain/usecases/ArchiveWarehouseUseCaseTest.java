package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

	@Test
	void shouldArchiveWarehouseAndPersistUpdate() {
		Warehouse warehouse = new Warehouse();
		WarehouseStoreSpy store = new WarehouseStoreSpy();

		new ArchiveWarehouseUseCase(store).archive(warehouse);

		assertNotNull(warehouse.archivedAt);
		assertSame(warehouse, store.updated);
	}

	@Test
	void shouldRejectNullWarehouse() {
		assertThrows(IllegalArgumentException.class,
				() -> new ArchiveWarehouseUseCase(new WarehouseStoreSpy()).archive(null));
	}

	@Test
	void shouldNotUpdateAlreadyArchivedWarehouse() {
		Warehouse warehouse = new Warehouse();
		warehouse.archivedAt = java.time.LocalDateTime.now().minusDays(1);
		WarehouseStoreSpy store = new WarehouseStoreSpy();

		new ArchiveWarehouseUseCase(store).archive(warehouse);

		assertEquals(null, store.updated);
	}

	private static class WarehouseStoreSpy implements WarehouseStore {
		private Warehouse updated;

		@Override
		public List<Warehouse> getAll() {
			return List.of();
		}

		@Override
		public void create(Warehouse warehouse) {
		}

		@Override
		public void update(Warehouse warehouse) {
			updated = warehouse;
		}

		@Override
		public void remove(Warehouse warehouse) {
		}

		@Override
		public Warehouse findByBusinessUnitCode(String buCode) {
			return null;
		}

		@Override
		public Warehouse findByDatabaseId(Long id) {
			return null;
		}
	}
}
