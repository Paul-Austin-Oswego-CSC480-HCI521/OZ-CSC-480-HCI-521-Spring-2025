package com.quotes;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/search")
public class QuoteSearchResource {

    @Inject
    QuoteService quoteService;

    @GET
    @Path("/id/{quoteID}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Quote was successfully retrieved from the database"),
            @APIResponse(responseCode = "400", description = "Given ID is not a valid ObjectId"),
            @APIResponse(responseCode = "404", description = "Quote ID was not found in the database"),
            @APIResponse(responseCode = "409", description = "IllegalArgumentException occurred during operation")
    })
    @Operation(summary = "Retrieves a quote from the database", description = "Returns json of quote if quote was found")
    public Response idSearch(@Parameter(
            description = "ID of quote you want to get",
            required = true,
            example = "67b61f18daa68e25fbd151e9",
            schema = @Schema(type = SchemaType.STRING)
    )@PathParam("quoteID") String quoteID) {
        try {
            //check if id is valid form
            if(!SanitizerClass.validObjectId(quoteID)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Given ID is not valid ObjectId").build();
            }

            ObjectId objectId = new ObjectId(quoteID);
            String jsonQuote = quoteService.getQuote(objectId);

            if(jsonQuote != null) {
                return Response.ok(jsonQuote).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Returned Json was null. Check quote ID is correct").build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity("IllegalArgumentException"+e).build();
        }
    }

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully found quotes relevant to query"),
            @APIResponse(responseCode = "400", description = "Error occurred when cleaning query string"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "Fuzzy search for quotes relevant to supplied query",
    description = "Searches for quotes similar to the users input and returns json of quotes determined to be most similar." +
            " They are sorted in descending order so the first json object is the closest to users input.")
    public Response advancedSearch(@QueryParam("filterUsed") @Parameter(description = "Should filter out Used Quotes. Defaults to false if left blank", required = false)
                                       boolean filterUsed,
                                   @QueryParam("filterBookmarked") @Parameter(description = "Should filter out Bookmarked Quotes. Defaults to false if left blank", required = false)
                                   boolean filterBookmarked,
                                   @QueryParam("filterUploaded") @Parameter(description = "Should filter out users Uploaded Quotes. Defaults to false if left blank", required = false)
                                       boolean filterUploaded,
                                   @QueryParam("include") @Parameter(description = "String of terms that must be included in quote. Leave as comma separated string. null if left blank",
                                           required = false, example = "one,two,three")
                                       String Included,
                                   @QueryParam("exclude") @Parameter(description = "String of terms that must be excluded in quote. Leave as comma separated string. null if left blank",
                                           required = false, example = "one,two,three")
                                       String Excluded,
                                   @QueryParam("query") @Parameter(description = "Query string user entered", required = true)
                                       String query,
                                   @Context HttpHeaders header)
    {
        try{
            //get user jwt from header
            boolean isGuest;
            String authHeader = header.getHeaderString(HttpHeaders.AUTHORIZATION);
            String jwtString = null;

            if (authHeader == null) {
                //no jwt, treat as guest
                isGuest = true;
            } else if(!authHeader.toLowerCase().startsWith("bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                        .build();
            } else {
                //valid jwt, treat as user
                isGuest = false;
                jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");
            }

            //handle query string
            if(query == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Query string is null").build();
            }
            query = SanitizerClass.sanitize(query); //removes special characters
            //search database using Atlas Search
            String result = quoteService.searchQuote(query, filterUsed, filterBookmarked, filterUploaded, Included, Excluded, jwtString, isGuest);
            if(result == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No quotes matched the search criteria").build();
            }
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("Exception Occured: "+e).build();
        }
    }

    @GET
    @Path("/topBookmarked")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully found and returned quotes"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "get quotes with the most bookmarks", description = "No input required. Searches for quotes" +
            " with the most bookmarks and returns json of all the quotes. It is sorted in descending order. Currently it" +
            " is limited to 100 results")
    public Response getTopBookmarks() {
        try{
            String result = quoteService.getTopBookmarked();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("Exception Occurred: "+e).build();
        }
    }

    @GET
    @Path("/topShared")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully found and returned quotes"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "get quotes with the most shares", description = "No input required. Searches for quotes" +
            " with the most shares and returns json of all the quotes. It is sorted in descending order. Currently it" +
            " is limited to 100 results")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSharedBookmarked() {
        try{
            String result = quoteService.getTopShared();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("Exception Occurred: "+e).build();
        }
    }

    @GET
    @Path("/topFlagged")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully found and returned quotes"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "get quotes that are over flag threshold", description = "No input required. Searches for quotes" +
            " where the \"flag\" value is over a threshold, currently 2. It is sorted in descending order.")
    public Response getTopFlagged(@Context HttpHeaders header) {
        String authHeader = header.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        try {
            String result = quoteService.getTopFlagged();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("Exception Occurred: "+e).build();
        }
    }

    @GET
    @Path("/mostRecent")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully found and returned quotes"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "get quotes with the most bookmarks", description = "No input required. Returns quotes posted most recently" +
            ". It is sorted in descending order. Currently it is limited to 100 results")
    public Response getMostRecent() {
        try{
            String result = quoteService.getMostRecent();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("Exception Occurred: "+e).build();
        }
    }

    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully found and returned user's quotes"),
            @APIResponse(responseCode = "400", description = "Given ID is not a valid ObjectId"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "Get all quotes uploaded by a specific user", description = "Returns JSON of all quotes uploaded by the user")
    public Response getQuotesByUser(@Parameter(
            description = "ID of the user whose quotes you want to get",
            required = true,
            example = "67b61f18daa68e25fbd151e9",
            schema = @Schema(type = SchemaType.STRING)
    ) @PathParam("userId") String userId, @Context HttpHeaders header) {
        String authHeader = header.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        try {
            // Check if ID is valid form
            if (!SanitizerClass.validObjectId(userId)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Given ID is not valid ObjectId").build();
            }

            ObjectId objectId = new ObjectId(userId);
            String jsonQuotes = quoteService.getQuotesByUser(objectId);

            return Response.ok(jsonQuotes).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("Exception Occurred: " + e).build();
        }
    }
}


