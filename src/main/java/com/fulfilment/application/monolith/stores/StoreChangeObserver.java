package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;

@ApplicationScoped
public class StoreChangeObserver {

	private final LegacyStoreManagerGateway legacyStoreManagerGateway;

	public StoreChangeObserver(LegacyStoreManagerGateway legacyStoreManagerGateway) {
		this.legacyStoreManagerGateway = legacyStoreManagerGateway;
	}

	public void onStoreChange(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreChangeEvent event) {
		Store store = event.getStore();
		if (event.getType() == StoreChangeEvent.Type.CREATED) {
			legacyStoreManagerGateway.createStoreOnLegacySystem(store);
		} else if (event.getType() == StoreChangeEvent.Type.UPDATED) {
			legacyStoreManagerGateway.updateStoreOnLegacySystem(store);
		}
	}
}
