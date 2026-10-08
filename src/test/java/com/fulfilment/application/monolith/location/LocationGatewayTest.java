package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;

public class LocationGatewayTest {

	private final LocationGateway locationGateway = new LocationGateway();

	@Test
	public void testWhenResolveExistingLocationShouldReturn() {
		Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

		assertEquals("ZWOLLE-001", location.identification);
		assertEquals(1, location.maxNumberOfWarehouses);
		assertEquals(40, location.maxCapacity);
	}

	@Test
	public void shouldReturnNullForUnknownLocation() {
		assertNull(locationGateway.resolveByIdentifier("UNKNOWN-001"));
	}

	@Test
	public void shouldReturnNullForNullIdentifier() {
		assertNull(locationGateway.resolveByIdentifier(null));
	}
}
