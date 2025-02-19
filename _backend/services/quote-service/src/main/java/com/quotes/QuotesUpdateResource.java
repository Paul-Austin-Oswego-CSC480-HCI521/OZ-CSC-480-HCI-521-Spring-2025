package com.quotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.*;
import java.io.IOException;

@Path("/update")
public class QuotesUpdateResource {

    @Inject
    MongoUtil mongo;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateQuote(String rawJson) {
        try{

            //Map json to Java Object
            ObjectMapper objectMapper = new ObjectMapper();
            QuoteObject quote = objectMapper.readValue(rawJson, QuoteObject.class);

            quote = SanitizerClass.sanitizeQuote(quote);
            if(quote == null) {
                return Response.status(Response.Status.CONFLICT).entity("Error when sanitizing quote, returned null").build();
            }

            boolean updated = mongo.updateQuote(quote);

            if(updated) {
                return Response.ok("Quote updated successfully").build();
            } else {
                return Response.status(Response.Status.CONFLICT).entity("Error updating quote, Json could be wrong or is missing quote ID").build();
            }
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("IOException: "+e).build();
        }
    }
}
