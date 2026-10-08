package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

	private InMemoryWarehouseStore store;
	private ReplaceWarehouseUseCase useCase;

	@BeforeEach
	void setUp() {
		store = new InMemoryWarehouseStore();
		LocationResolver resolver = identifier -> "AMSTERDAM-001".equals(identifier)
				? new Location("AMSTERDAM-001", 5, 100)
				: null;
		useCase = new ReplaceWarehouseUseCase(store, resolver);
	}

	@Test
	void shouldArchiveCurrentWarehouseAndCreateReplacement() {
		Warehouse current = warehouse("MWH.012", "AMSTERDAM-001", 50, 5);
		current.id = 12L;
		store.add(current);

		Warehouse replacement = warehouse("MWH.012", "AMSTERDAM-001", 20, 5);
		useCase.replace(replacement);

		assertNotNull(current.archivedAt);
		assertSame(current, store.updated);
		assertSame(replacement, store.created);
		assertNull(replacement.id);
		assertNull(replacement.archivedAt);
		assertNotNull(replacement.createdAt);
	}

	@Test
	void shouldRejectUnknownWarehouse() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.replace(warehouse("MWH.UNKNOWN", "AMSTERDAM-001", 20, 5)));

		assertEquals("Warehouse not found", exception.getMessage());
	}

	@Test
	void shouldRejectDifferentStock() {
		store.add(warehouse("MWH.012", "AMSTERDAM-001", 50, 5));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.replace(warehouse("MWH.012", "AMSTERDAM-001", 20, 6)));

		assertEquals("Replacement stock must match current warehouse stock", exception.getMessage());
	}

	@Test
	void shouldRejectCapacityThatCannotAccommodateCurrentStock() {
		store.add(warehouse("MWH.012", "AMSTERDAM-001", 50, 5));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.replace(warehouse("MWH.012", "AMSTERDAM-001", 4, 5)));

		assertEquals("Replacement capacity cannot be lower than current stock", exception.getMessage());
	}

	@Test
	void shouldRejectDifferentLocation() {
		store.add(warehouse("MWH.012", "AMSTERDAM-001", 50, 5));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.replace(warehouse("MWH.012", "OTHER", 20, 5)));

		assertEquals("Replacement warehouse must use the same location", exception.getMessage());
	}

	private static Warehouse warehouse(String buCode, String location, int capacity, int stock) {
		Warehouse warehouse = new Warehouse();
		warehouse.businessUnitCode = buCode;
		warehouse.location = location;
		warehouse.capacity = capacity;
		warehouse.stock = stock;
		return warehouse;
	}

	private static class InMemoryWarehouseStore implements WarehouseStore {
		private final List<Warehouse> warehouses = new ArrayList<>();
		private Warehouse updated;
		private Warehouse created;

		void add(Warehouse warehouse) {
			warehouses.add(warehouse);
		}

		@Override
		public List<Warehouse> getAll() {
			return new ArrayList<>(warehouses);
		}

		@Override
		public void create(Warehouse warehouse) {
			created = warehouse;
			warehouses.add(warehouse);
		}

		@Override
		public void update(Warehouse warehouse) {
			updated = warehouse;
		}

		@Override
		public void remove(Warehouse warehouse) {
			warehouses.remove(warehouse);
		}

		@Override
		public Warehouse findByBusinessUnitCode(String buCode) {
			return warehouses.stream().filter(w -> buCode.equals(w.businessUnitCode) && w.archivedAt == null)
					.findFirst().orElse(null);
		}

		@Override
		public Warehouse findByDatabaseId(Long id) {
			return warehouses.stream().filter(w -> id.equals(w.id)).findFirst().orElse(null);
		}
	}
}
