package com.fulfilment.application.monolith.fulfillment.application;

import java.util.List;

import com.fulfilment.application.monolith.fulfillment.adapters.database.WarehouseFulfillmentRepository;
import com.fulfilment.application.monolith.fulfillment.domain.models.WarehouseFulfillment;
import com.fulfilment.application.monolith.fulfillment.validators.WarehouseFulfillmentValidator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class WarehouseFulfillmentService {

	@Inject
	WarehouseFulfillmentRepository fulfillmentRepository;

	@Inject
	WarehouseFulfillmentValidator validator;

	public List<WarehouseFulfillment> getAll() {
		return fulfillmentRepository.listAll();
	}

	@Transactional
	public WarehouseFulfillment create(Long storeId, Long productId, Long warehouseId) {
		validator.validateCreate(storeId, productId, warehouseId);

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
}
