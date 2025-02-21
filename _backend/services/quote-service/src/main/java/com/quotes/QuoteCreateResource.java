package com.quotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.io.IOException;

@Path("/create")
public class QuoteCreateResource {

    @Inject
    MongoUtil mongo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The quote was successfully added to the database. Will return new quote ID",
            content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "409", description = "Error when sanitizing quote texts, attempted to remove all special characters, returned null"),
            @APIResponse(responseCode = "400", description = "Error when adding quote to database, returned quote ID was null"),
    })
    @Operation(summary = "Adds a new quote to the mongo database and will return the id of the newly created quote")
    @RequestBody(description = "Example request body endpoint is expecting. \"_id\" field is NOT REQUIRED, the server will generate an id. Any supplied value will be overwritten" +
            " bookmarks, shares, and flags are also not required as they will be automatically set to 0",
            required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuoteObject.class),
            examples = @ExampleObject(name = "Example", value = "{\"_id\": \"NOT REQUIRED\", \"author\": \"Example Author\", " +
                    "\"quote\": \"Example quote text\", \"date\": 1739988322," +
                    "\"tags\": [\"example\", \"another example\"]}")
    ))
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
                return Response.ok(jsonResponse).build();

            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Returned QuoteID null. Check json is formatted Correctly").build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Exception occured: "+e).build();
        }
    }
}
