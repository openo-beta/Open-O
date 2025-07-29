package org.oscarehr.ws.rest;

import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub implementation of SystemInfoService to prevent Spring context initialization failures
 * when the original SystemInfoService class is not available.
 * 
 * This service returns a "service unavailable" message for all endpoints.
 */
@Component("systemInfoService")
@Path("/systemInfo")
public class SystemInfoServiceStub {

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getSystemInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "unavailable");
        response.put("message", "SystemInfoService is temporarily unavailable");
        response.put("timestamp", System.currentTimeMillis());
        
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                      .entity(response)
                      .build();
    }

    @GET
    @Path("/health")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getHealthStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("health", "degraded");
        response.put("message", "SystemInfoService stub is active - original service unavailable");
        response.put("timestamp", System.currentTimeMillis());
        
        return Response.ok(response).build();
    }
}
