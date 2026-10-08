package com.fulfilment.application.monolith.fulfillment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "warehouse_fulfillment", uniqueConstraints = @UniqueConstraint(columnNames = { "storeId", "productId",
		"warehouseId" }))
public class WarehouseFulfillment {

	@Id
	@GeneratedValue
	public Long id;

	@Column(nullable = false)
	public Long storeId;

	@Column(nullable = false)
	public Long productId;

	@Column(nullable = false)
	public Long warehouseId;

	public WarehouseFulfillment() {
	}

	public WarehouseFulfillment(Long storeId, Long productId, Long warehouseId) {
		this.storeId = storeId;
		this.productId = productId;
		this.warehouseId = warehouseId;
	}
}
