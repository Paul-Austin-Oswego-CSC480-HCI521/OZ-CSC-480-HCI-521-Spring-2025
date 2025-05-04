package com.accounts;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(baseUri = "http://quotes-service:9082")
public interface QuoteClient{
   
    @PUT
    @Path("/quotes/bookmark/increment/{quoteID}")
    @Produces(MediaType.APPLICATION_JSON)
    Response bookmarkQuote(@PathParam("quoteID") String quoteID, @HeaderParam("Authorization") String authHeader);

    @DELETE
    @Path("/quotes/bookmark/decrement/{quoteID}")
    @Produces(MediaType.APPLICATION_JSON)
    Response deleteBookmark(@PathParam("quoteID") String quoteID, @HeaderParam("Authorization") String authHeader);

    @GET
    @Path("/quotes/search/id/{quoteID}")
    @Produces(MediaType.APPLICATION_JSON)
    Response idSearch(@PathParam("quoteID") String quoteID);
    
}