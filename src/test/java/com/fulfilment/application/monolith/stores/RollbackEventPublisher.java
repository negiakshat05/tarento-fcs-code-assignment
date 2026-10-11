package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RollbackEventPublisher {

	@Inject
	Event<StoreChangeEvent> storeChangeEvents;

	@Transactional
	public void publishThenRollback() {
		Store store = new Store();
		store.id = 987654L;
		store.name = "ROLLBACK-TEST";
		store.quantityProductsInStock = 1;

		storeChangeEvents.fire(StoreChangeEvent.created(store));
		throw new IllegalStateException("Deliberate rollback for observer test");
	}
}
