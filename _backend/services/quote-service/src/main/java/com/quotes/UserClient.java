package com.quotes;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(baseUri = "http://user-service:9081")
public interface UserClient{
   
    @DELETE
    @Path("/users/accounts/delete/MyQuotes/{userId}/{quoteId}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response deleteFromMyQuotes(@PathParam("userId") String id, @PathParam("quoteId") String quoteId,@HeaderParam("Authorization") String authHeader);

    @PUT
    @Path("/users/accounts/insert/MyQuotes/{userId}/{quoteId}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response insertIntoMyQuotes(@PathParam("userId") String id, @PathParam("quoteId") String quoteId,@HeaderParam("Authorization") String authHeader);

    @GET
    @Path("/users/accounts/search/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response search(@PathParam("id") String id);

    @GET
    @Path("/bookmarks/UsedQuotesIds")
    @Produces(MediaType.APPLICATION_JSON)
    Response getUsedQuotes();
    
}