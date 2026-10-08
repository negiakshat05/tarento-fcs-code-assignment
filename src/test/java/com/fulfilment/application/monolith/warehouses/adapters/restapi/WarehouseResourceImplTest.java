package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;

import jakarta.ws.rs.WebApplicationException;

class WarehouseResourceImplTest {

	private WarehouseResourceImpl resource;
	private FakeWarehouseRepository repository;
	private FakeLocationResolver locationResolver;

	@BeforeEach
	void setUp() throws Exception {
		resource = new WarehouseResourceImpl();

		repository = new FakeWarehouseRepository();
		locationResolver = new FakeLocationResolver();

		setField("warehouseRepository", repository);

		setField("createWarehouseUseCase", new FakeCreateWarehouseUseCase(repository, locationResolver));

		setField("archiveWarehouseUseCase", new FakeArchiveWarehouseUseCase(repository));

		setField("replaceWarehouseUseCase", new FakeReplaceWarehouseUseCase(repository, locationResolver));
	}

	@Test
	void shouldListWarehouses() {
		Warehouse warehouse = warehouse(1L, "MWH.001", "ZWOLLE-001", 100, 10);

		repository.warehouses = List.of(warehouse);

		List<com.warehouse.api.beans.Warehouse> result = resource.listAllWarehousesUnits();

		assertEquals(1, result.size());
		assertEquals("1", result.get(0).getId());
		assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
		assertEquals("ZWOLLE-001", result.get(0).getLocation());
		assertEquals(100, result.get(0).getCapacity());
		assertEquals(10, result.get(0).getStock());
	}

	@Test
	void shouldCreateWarehouse() {
		com.warehouse.api.beans.Warehouse request = request("TEST.CREATE.001", "ZWOLLE-001", 30, 10);

		com.warehouse.api.beans.Warehouse result = resource.createANewWarehouseUnit(request);

		assertEquals("TEST.CREATE.001", result.getBusinessUnitCode());
		assertEquals("ZWOLLE-001", result.getLocation());
		assertEquals(30, result.getCapacity());
		assertEquals(10, result.getStock());
	}

	@Test
	void shouldReturnBadRequestWhenCreateFails() {
		repository.shouldFailCreate = true;

		com.warehouse.api.beans.Warehouse request = request("TEST.INVALID.001", "ZWOLLE-001", 30, 10);

		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.createANewWarehouseUnit(request));

		assertEquals(400, exception.getResponse().getStatus());
	}

	@Test
	void shouldRejectNullCreateRequest() {
		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.createANewWarehouseUnit(null));

		assertEquals(400, exception.getResponse().getStatus());
	}

	@Test
	void shouldGetWarehouseById() {
		Warehouse warehouse = warehouse(10L, "MWH.010", "AMSTERDAM-001", 50, 5);

		repository.warehouseById = warehouse;

		com.warehouse.api.beans.Warehouse result = resource.getAWarehouseUnitByID("10");

		assertEquals("10", result.getId());
		assertEquals("MWH.010", result.getBusinessUnitCode());
		assertEquals("AMSTERDAM-001", result.getLocation());
		assertEquals(50, result.getCapacity());
		assertEquals(5, result.getStock());
	}

	@Test
	void shouldReturnNotFoundWhenWarehouseDoesNotExist() {
		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.getAWarehouseUnitByID("999"));

		assertEquals(404, exception.getResponse().getStatus());
	}

	@Test
	void shouldRejectInvalidWarehouseId() {
		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.getAWarehouseUnitByID("abc"));

		assertEquals(400, exception.getResponse().getStatus());
	}

	@Test
	void shouldArchiveWarehouse() {
		Warehouse warehouse = warehouse(20L, "MWH.020", "TILBURG-001", 30, 10);

		repository.warehouseById = warehouse;

		resource.archiveAWarehouseUnitByID("20");

		assertEquals(1, repository.updateCalls);
	}

	@Test
	void shouldReturnNotFoundWhenArchivingUnknownWarehouse() {
		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.archiveAWarehouseUnitByID("999"));

		assertEquals(404, exception.getResponse().getStatus());
	}

	@Test
	void shouldRejectInvalidArchiveId() {
		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.archiveAWarehouseUnitByID("abc"));

		assertEquals(400, exception.getResponse().getStatus());
	}

	@Test
	void shouldReplaceWarehouse() {
		repository.warehouseByBusinessUnitCode = warehouse(30L, "MWH.030", "AMSTERDAM-001", 20, 5);

		com.warehouse.api.beans.Warehouse request = request("MWH.030", "AMSTERDAM-001", 30, 5);

		com.warehouse.api.beans.Warehouse result = resource.replaceTheCurrentActiveWarehouse("MWH.030", request);

		assertEquals("MWH.030", result.getBusinessUnitCode());
		assertEquals("AMSTERDAM-001", result.getLocation());
		assertEquals(30, result.getCapacity());
		assertEquals(5, result.getStock());
		assertEquals(1, repository.updateCalls);
		assertEquals(1, repository.createCalls);
	}

	@Test
	void shouldReturnNotFoundWhenReplacingUnknownWarehouse() {
		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.replaceTheCurrentActiveWarehouse("UNKNOWN", request("UNKNOWN", "AMSTERDAM-001", 30, 5)));

		assertEquals(404, exception.getResponse().getStatus());
	}

	@Test
	void shouldRejectDifferentBusinessUnitCodeInReplacementRequest() {
		repository.warehouseByBusinessUnitCode = warehouse(40L, "MWH.040", "AMSTERDAM-001", 20, 5);

		WebApplicationException exception = assertThrows(WebApplicationException.class, () -> resource
				.replaceTheCurrentActiveWarehouse("MWH.040", request("DIFFERENT", "AMSTERDAM-001", 30, 5)));

		assertEquals(400, exception.getResponse().getStatus());
	}

	@Test
	void shouldRejectReplacementWhenUseCaseFails() {
		repository.warehouseByBusinessUnitCode = warehouse(50L, "MWH.050", "AMSTERDAM-001", 20, 5);

		repository.shouldFailReplace = true;

		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.replaceTheCurrentActiveWarehouse("MWH.050", request("MWH.050", "AMSTERDAM-001", 30, 5)));

		assertEquals(400, exception.getResponse().getStatus());
	}

	@Test
	void shouldRejectBlankBusinessUnitCode() {
		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.replaceTheCurrentActiveWarehouse("   ", request("TEST", "AMSTERDAM-001", 30, 5)));

		assertEquals(400, exception.getResponse().getStatus());
	}

	@Test
	void shouldRejectNullBusinessUnitCode() {
		WebApplicationException exception = assertThrows(WebApplicationException.class,
				() -> resource.replaceTheCurrentActiveWarehouse(null, request("TEST", "AMSTERDAM-001", 30, 5)));

		assertEquals(400, exception.getResponse().getStatus());
	}

	private void setField(String fieldName, Object value) throws Exception {
		Field field = WarehouseResourceImpl.class.getDeclaredField(fieldName);

		field.setAccessible(true);
		field.set(resource, value);
	}

	private static Warehouse warehouse(Long id, String businessUnitCode, String location, int capacity, int stock) {

		Warehouse warehouse = new Warehouse();
		warehouse.id = id;
		warehouse.businessUnitCode = businessUnitCode;
		warehouse.location = location;
		warehouse.capacity = capacity;
		warehouse.stock = stock;

		return warehouse;
	}

	private static com.warehouse.api.beans.Warehouse request(String businessUnitCode, String location, int capacity,
			int stock) {

		com.warehouse.api.beans.Warehouse request = new com.warehouse.api.beans.Warehouse();

		request.setBusinessUnitCode(businessUnitCode);
		request.setLocation(location);
		request.setCapacity(capacity);
		request.setStock(stock);

		return request;
	}

	private static class FakeWarehouseRepository extends WarehouseRepository {

		private List<Warehouse> warehouses = List.of();
		private Warehouse warehouseById;
		private Warehouse warehouseByBusinessUnitCode;

		private int createCalls;
		private int updateCalls;

		private boolean shouldFailCreate;
		private boolean shouldFailReplace;

		@Override
		public List<Warehouse> getAll() {
			return warehouses;
		}

		@Override
		public Warehouse findByDatabaseId(Long id) {
			return warehouseById;
		}

		@Override
		public Warehouse findByBusinessUnitCode(String businessUnitCode) {
			return warehouseByBusinessUnitCode;
		}

		@Override
		public void create(Warehouse warehouse) {
			if (shouldFailCreate) {
				throw new IllegalArgumentException("Invalid warehouse");
			}

			createCalls++;
		}

		@Override
		public void update(Warehouse warehouse) {
			updateCalls++;

			if (shouldFailReplace) {
				throw new IllegalArgumentException("Replacement failed");
			}
		}
	}

	private static class FakeLocationResolver implements LocationResolver {

		@Override
		public Location resolveByIdentifier(String identifier) {
			if (identifier == null) {
				return null;
			}

			return new Location(identifier, 10, 1000);
		}
	}

	private static class FakeCreateWarehouseUseCase extends CreateWarehouseUseCase {

		FakeCreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {

			super(warehouseStore, locationResolver);
		}
	}

	private static class FakeArchiveWarehouseUseCase extends ArchiveWarehouseUseCase {

		FakeArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
			super(warehouseStore);
		}
	}

	private static class FakeReplaceWarehouseUseCase extends ReplaceWarehouseUseCase {

		FakeReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {

			super(warehouseStore, locationResolver);
		}
	}
}