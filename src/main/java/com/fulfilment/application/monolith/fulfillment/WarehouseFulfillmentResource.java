package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfillment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class WarehouseFulfillmentResource {

    @Inject
    WarehouseFulfillmentRepository fulfillmentRepository;

    @Inject
	WarehouseFulfillmentService fulfillmentService;

    @GET
    public List<WarehouseFulfillment> getAll() {
        return fulfillmentRepository.listAll();
    }

    @POST
    public Response create(WarehouseFulfillmentRequest request) {
        try {
            WarehouseFulfillment association = fulfillmentService.create(
                    request == null ? null : request.storeId,
                    request == null ? null : request.productId,
                    request == null ? null : request.warehouseId);
            return Response.status(Response.Status.CREATED).entity(association).build();
        } catch (IllegalArgumentException exception) {
            throw new WebApplicationException(exception.getMessage(), 400);
        }
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        try {
            fulfillmentService.delete(id);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IllegalArgumentException exception) {
            throw new WebApplicationException(exception.getMessage(), 404);
        }
    }

    public static class WarehouseFulfillmentRequest {
        public Long storeId;
        public Long productId;
        public Long warehouseId;
    }
}
