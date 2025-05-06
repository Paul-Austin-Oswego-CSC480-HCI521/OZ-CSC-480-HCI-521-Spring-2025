package com.quotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Path("/update/bookmark")
public class QuotesUpdateBookmark {

    @Inject
    QuoteService quoteService;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The quote was successfully updated. Returns json {\"success\": \"true\""),
            @APIResponse(responseCode = "409", description = "Error when sanitizing quote texts, or updating into the database"),
            @APIResponse(responseCode = "400", description = "IOException Occurred"),
    })
    @Operation(summary = "Update fields of a quote in the database", description = "Update quote within database. \"_id\" field IS REQUIRED." +
            " All other fields are optional. Currently the integer fields \"bookmarks\", \"shares\", and \"flags\" can only change by 1 at a time, " +
            "so only +1 or -1. If the current value is 5 it will only accept 4 or 6. Let Engine know if you want" +
            " more dedicated update endpoints such as unique ones for each field or any other changes")
    @RequestBody(description = "Example request body endpoint is expecting. \"_id\" field IS REQUIRED. All other fields are optional",
            required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuoteObject.class),
            examples = {@ExampleObject(name = "Example: update author and quote text", value = "{\"_id\": \"67abf3b6b0d20a5237456441\", \"author\": \"New Value\", " +
                    "\"quote\": \"New quote text\"}"),
            @ExampleObject(name = "Example: update bookmarks", value = "{\"_id\": \"67abf3b6b0d20a5237456441\", \"bookmarks\": 6}")
            }
    ))
    public Response updateQuote(String rawJson) {
        try{
            //Map json to Java Object
            ObjectMapper objectMapper = new ObjectMapper();
            QuoteObject quote = objectMapper.readValue(rawJson, QuoteObject.class);

            ObjectId objectId = new ObjectId(quote.getId().toString());
            String jsonQuote = quoteService.getQuote(objectId);
            QuoteObject oldQuote = objectMapper.readValue(jsonQuote, QuoteObject.class);

            quote = SanitizerClass.sanitizeQuote(quote);
            if(quote == null) {
                return Response.status(Response.Status.CONFLICT).entity("Error when sanitizing quote, returned null").build();
            }

            boolean updated = quoteService.updateQuote(quote);

            if(updated) {
                JsonObject jsonResponse = Json.createObjectBuilder()
                        .add("Response", "200")
                        .build();
                return Response.ok(jsonResponse).build();
            } else {
                return Response.status(Response.Status.CONFLICT).entity("Error updating quote, Json could be wrong or is missing quote ID").build();
            }
        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("IOException: "+e).build();
        }
    }
}
