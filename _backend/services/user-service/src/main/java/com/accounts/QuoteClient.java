package com.accounts;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(baseUri = "http://quotes-service:9082")
public interface QuoteClient{
   
    @PUT
    @Path("/quotes/update/bookmark")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateQuote(String rawJson);

    @GET
    @Path("/quotes/search/id/{quoteID}")
    @Produces(MediaType.APPLICATION_JSON)
    Response idSearch(@PathParam("quoteID") String quoteID);
    
}