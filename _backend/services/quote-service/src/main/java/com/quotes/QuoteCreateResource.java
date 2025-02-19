package com.quotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.io.IOException;

@Path("/create")
public class QuoteCreateResource {

    @Inject
    MongoUtil mongo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQuote(String rawJson) {
        try{
            //map json to Java Object
            ObjectMapper objectMapper = new ObjectMapper();
            QuoteObject quote = objectMapper.readValue(rawJson, QuoteObject.class);

            quote = SanitizerClass.sanitizeQuote(quote);
            if(quote == null) {
                return Response.status(Response.Status.CONFLICT).entity("Error when sanitizing quote, returned null").build();
            }

            ObjectId newQuoteId = mongo.createQuote(quote); //add to mongo database

            if(newQuoteId != null) {
                JsonObject jsonResponse = Json.createObjectBuilder()
                        .add("_id", newQuoteId.toHexString())
                        .build();
                return Response.ok(jsonResponse.toString()).build();

            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Returned QuoteID null. Check json is formatted Correctly").build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Exception occured: "+e).build();
        }
    }
}
