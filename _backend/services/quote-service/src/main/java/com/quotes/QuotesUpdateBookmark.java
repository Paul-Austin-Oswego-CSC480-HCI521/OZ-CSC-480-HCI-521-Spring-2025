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

@Path("/bookmark")
public class QuotesUpdateBookmark {

    @Inject
    MongoUtil mongo;

    @PUT
    @Path("/increment/{quoteId}")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The quote was successfully updated. Returns json {\"success\": \"true\""),
            @APIResponse(responseCode = "409", description = "Error when sanitizing quote texts, or updating into the database"),
            @APIResponse(responseCode = "400", description = "IOException Occurred"),
    })
    @Operation(summary = "Update fields of a quote in the database", description = "Update quote within database. \"_id\" field IS REQUIRED." +
            " All other fields are optional. Currently the integer fields \"bookmarks\", \"shares\", and \"flags\" can only change by 1 at a time, " +
            "so only +1 or -1. If the current value is 5 it will only accept 4 or 6. Let Engine know if you want" +
            " more dedicated update endpoints such as unique ones for each field or any other changes")
    public Response bookmarkQuote(@PathParam("quoteId")String quoteId, @Context HttpHeaders headers) {
        ObjectId objectId = new ObjectId(quoteId);
        

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Map<String, String> jwtMap= QuotesRetrieveAccount.retrieveJWTData(jwtString);


        if (jwtMap == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to update quotes").toJson()).build();
        }

        // get account ID from JWT
        String accountID = jwtMap.get("subject");

        // get group from JWT
        String group = jwtMap.get("group");

        // check if account has not been logged in
        if (accountID == null || group == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to update quotes").toJson()).build();
        }

        
        boolean bookmarked = mongo.incrementBookmarkCount(objectId);
        if(bookmarked) {
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("Response", "200")
                    .build();
            return Response.ok(jsonResponse).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity("Error bookmarking quote").build();
        }
    }

    @DELETE
    @Path("/decrement/{quoteId}")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The quote was successfully updated. Returns json {\"success\": \"true\""),
            @APIResponse(responseCode = "409", description = "Error when sanitizing quote texts, or updating into the database"),
            @APIResponse(responseCode = "400", description = "IOException Occurred"),
    })
    @Operation(summary = "Update fields of a quote in the database", description = "Update quote within database. \"_id\" field IS REQUIRED." +
            " All other fields are optional. Currently the integer fields \"bookmarks\", \"shares\", and \"flags\" can only change by 1 at a time, " +
            "so only +1 or -1. If the current value is 5 it will only accept 4 or 6. Let Engine know if you want" +
            " more dedicated update endpoints such as unique ones for each field or any other changes")
    public Response deleteBookmark(@PathParam("quoteId")String quoteId, @Context HttpHeaders headers) {
        ObjectId objectId = new ObjectId(quoteId);
        

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Map<String, String> jwtMap= QuotesRetrieveAccount.retrieveJWTData(jwtString);


        if (jwtMap == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to update quotes").toJson()).build();
        }

        // get account ID from JWT
        String accountID = jwtMap.get("subject");

        // get group from JWT
        String group = jwtMap.get("group");

        // check if account has not been logged in
        if (accountID == null || group == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to update quotes").toJson()).build();
        }

        
        boolean bookmarked = mongo.decrementBookmarkCount(objectId);
        if(bookmarked) {
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("Response", "200")
                    .build();
            return Response.ok(jsonResponse).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity("Error bookmarking quote").build();
        }
    }
}
