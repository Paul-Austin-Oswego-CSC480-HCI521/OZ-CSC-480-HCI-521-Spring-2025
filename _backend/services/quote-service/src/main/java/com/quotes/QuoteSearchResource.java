package com.quotes;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/search")
public class QuoteSearchResource {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getQuote() {
        return "{\"quote\": \"The only limit to our realization of tomorrow is our doubts of today.\"}";
    }
}
