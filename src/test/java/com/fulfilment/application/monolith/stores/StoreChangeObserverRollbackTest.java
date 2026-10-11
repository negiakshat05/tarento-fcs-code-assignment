package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class StoreChangeObserverRollbackTest {

	@InjectMock
	LegacyStoreManagerGateway legacyStoreManagerGateway;

	@Inject
	RollbackEventPublisher rollbackEventPublisher;

	@Test
	void shouldNotCallLegacyGatewayWhenTransactionRollsBack() {
		assertThrows(IllegalStateException.class, rollbackEventPublisher::publishThenRollback);

		verifyNoInteractions(legacyStoreManagerGateway);
	}
}
