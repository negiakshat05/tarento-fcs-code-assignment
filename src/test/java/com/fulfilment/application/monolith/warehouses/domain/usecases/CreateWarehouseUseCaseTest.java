package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

	private InMemoryWarehouseStore store;
	private LocationResolver locationResolver;
	private CreateWarehouseUseCase useCase;

	@BeforeEach
	void setUp() {
		store = new InMemoryWarehouseStore();
		locationResolver = identifier -> switch (identifier) {
		case "ZWOLLE-002" -> new Location("ZWOLLE-002", 2, 50);
		case "AMSTERDAM-001" -> new Location("AMSTERDAM-001", 5, 100);
		default -> null;
		};
		useCase = new CreateWarehouseUseCase(store, locationResolver);
	}

	@Test
	void shouldCreateValidWarehouse() {
		Warehouse warehouse = warehouse("MWH.NEW", "ZWOLLE-002", 30, 10);

		useCase.create(warehouse);

		assertEquals(1, store.created.size());
		assertNotNull(warehouse.createdAt);
		assertNull(warehouse.archivedAt);
		assertEquals("MWH.NEW", store.created.get(0).businessUnitCode);
	}

	@Test
	void shouldRejectDuplicateBusinessUnitCode() {
		store.add(warehouse("MWH.001", "ZWOLLE-002", 20, 5));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.create(warehouse("MWH.001", "AMSTERDAM-001", 20, 5)));

		assertEquals("Business unit code already exists", exception.getMessage());
	}

	@Test
	void shouldRejectUnknownLocation() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.create(warehouse("MWH.NEW", "UNKNOWN", 20, 5)));

		assertEquals("Location does not exist", exception.getMessage());
	}

	@Test
	void shouldRejectCapacityAboveLocationMaximum() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.create(warehouse("MWH.NEW", "ZWOLLE-002", 51, 5)));

		assertEquals("Warehouse capacity exceeds location maximum capacity", exception.getMessage());
	}

	@Test
	void shouldRejectStockAboveCapacity() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.create(warehouse("MWH.NEW", "ZWOLLE-002", 10, 11)));

		assertEquals("Warehouse capacity cannot be lower than stock", exception.getMessage());
	}

	@Test
	void shouldRejectWhenMaximumNumberOfWarehousesIsReached() {
		store.add(warehouse("MWH.EXISTING.1", "ZWOLLE-002", 20, 5));
		store.add(warehouse("MWH.EXISTING.2", "ZWOLLE-002", 20, 5));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.create(warehouse("MWH.NEW", "ZWOLLE-002", 10, 5)));

		assertEquals("Maximum number of warehouses reached for location", exception.getMessage());
	}

	@Test
	void shouldRejectWhenTotalLocationCapacityWouldBeExceeded() {
		store.add(warehouse("MWH.EXISTING", "ZWOLLE-002", 30, 5));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> useCase.create(warehouse("MWH.NEW", "ZWOLLE-002", 25, 5)));

		assertEquals("Total warehouse capacity exceeds location maximum capacity", exception.getMessage());
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
		private final List<Warehouse> created = new ArrayList<>();

		void add(Warehouse warehouse) {
			warehouses.add(warehouse);
		}

		@Override
		public List<Warehouse> getAll() {
			return new ArrayList<>(warehouses);
		}

		@Override
		public void create(Warehouse warehouse) {
			created.add(warehouse);
			warehouses.add(warehouse);
		}

		@Override
		public void update(Warehouse warehouse) {
			// Not needed by these tests.
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
