package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import java.util.List;

import org.jboss.resteasy.reactive.ResponseStatus;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;

@Path("/warehouse")
@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

	@Inject
	private WarehouseRepository warehouseRepository;
	@Inject
	private CreateWarehouseUseCase createWarehouseUseCase;
	@Inject
	private ArchiveWarehouseUseCase archiveWarehouseUseCase;
	@Inject
	private ReplaceWarehouseUseCase replaceWarehouseUseCase;

	@Override
	public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {
		return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
	}

	@Override
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Transactional
	@ResponseStatus(201)
	public com.warehouse.api.beans.Warehouse createANewWarehouseUnit(@NotNull com.warehouse.api.beans.Warehouse data) {
		Warehouse warehouse = fromWarehouseRequest(data);
		try {
			createWarehouseUseCase.create(warehouse);
		} catch (IllegalArgumentException exception) {
			throw new WebApplicationException(exception.getMessage(), 400);
		}
		return toWarehouseResponse(warehouse);
	}

	@Override
	public com.warehouse.api.beans.Warehouse getAWarehouseUnitByID(String id) {
		Long warehouseId = parseId(id);
		Warehouse warehouse = warehouseRepository.findByDatabaseId(warehouseId);
		if (warehouse == null) {
			throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
		}
		return toWarehouseResponse(warehouse);
	}

	@Override
	@Transactional
	@ResponseStatus(204)
	public void archiveAWarehouseUnitByID(String id) {
		Long warehouseId = parseId(id);
		Warehouse warehouse = warehouseRepository.findByDatabaseId(warehouseId);
		if (warehouse == null) {
			throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
		}
		archiveWarehouseUseCase.archive(warehouse);
	}

	@Override
	@Transactional
	public com.warehouse.api.beans.Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode,
			@NotNull com.warehouse.api.beans.Warehouse data) {
		if (businessUnitCode == null || businessUnitCode.isBlank()) {
			throw new WebApplicationException("Business unit code must be provided.", 400);
		}
		Warehouse current = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
		if (current == null) {
			throw new WebApplicationException(
					"Warehouse with business unit code " + businessUnitCode + " does not exist.", 404);
		}

		Warehouse warehouse = fromWarehouseRequest(data);
		if (warehouse.businessUnitCode != null && !businessUnitCode.equals(warehouse.businessUnitCode)) {
			throw new WebApplicationException("Business unit code cannot differ from path parameter.", 400);
		}
		warehouse.businessUnitCode = businessUnitCode;
		try {
			replaceWarehouseUseCase.replace(warehouse);
		} catch (IllegalArgumentException exception) {
			throw new WebApplicationException(exception.getMessage(), 400);
		}
		return toWarehouseResponse(warehouse);
	}

	private Long parseId(String id) {
		try {
			return Long.valueOf(id);
		} catch (NumberFormatException | NullPointerException exception) {
			throw new WebApplicationException("Invalid warehouse id: " + id, 400);
		}
	}

	private Warehouse fromWarehouseRequest(com.warehouse.api.beans.Warehouse data) {
		if (data == null) {
			throw new WebApplicationException("Warehouse request must be provided.", 400);
		}
		Warehouse warehouse = new Warehouse();
		warehouse.businessUnitCode = data.getBusinessUnitCode();
		warehouse.location = data.getLocation();
		warehouse.capacity = data.getCapacity();
		warehouse.stock = data.getStock();
		return warehouse;
	}

	private com.warehouse.api.beans.Warehouse toWarehouseResponse(Warehouse warehouse) {
		var response = new com.warehouse.api.beans.Warehouse();
		if (warehouse.id != null) {
			response.setId(String.valueOf(warehouse.id));
		}
		response.setBusinessUnitCode(warehouse.businessUnitCode);
		response.setLocation(warehouse.location);
		response.setCapacity(warehouse.capacity);
		response.setStock(warehouse.stock);
		return response;
	}
}
