package com.quotes;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(baseUri = "http://user-service:9081")
public interface UserClient{
   
    @PUT
    @Path("/users/accounts/update/MyQuotes/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateMyQuotes(@PathParam("id") String id, String accountJson);

    @GET
    @Path("/users/accounts/search/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response search(@PathParam("id") String id);
    
}