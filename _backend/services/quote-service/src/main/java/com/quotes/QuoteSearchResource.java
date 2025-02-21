package com.quotes;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/search")
public class QuoteSearchResource {

    @Inject
    MongoUtil mongo;

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
            String jsonQuote = mongo.getQuote(objectId);

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
    @Path("/query/{query}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully found quotes relevant to query"),
            @APIResponse(responseCode = "400", description = "Error occurred when cleaning query string"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "Fuzzy search for quotes relevant to supplied query",
    description = "Searches for quotes similar to the users input and returns json of quotes determined to be most similar." +
            " They are sorted in descending order so the first json object is the closest to users input")
    public Response advancedSearch(@Parameter(
            description = "Query string",
            required = true,
            example = "I am famous test quote",
            schema = @Schema(type = SchemaType.STRING)
    )@PathParam("query") String query) {
        try{
            query = SanitizerClass.sanitize(query); //removes special characters
            if(query == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Error cleaning string, returned null").build();
            }

            String result = mongo.searchQuote(query);
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
            " is limited to only 5 results")
    public Response getTopBookmarks() {
        try{
            String result = mongo.getTopBookmarked();
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
            " is limited to only 5 results")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSharedBookmarked() {
        try{
            String result = mongo.getTopShared();
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
    public Response getTopFlagged() {
        try {
            String result = mongo.getTopFlagged();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("Exception Occurred: "+e).build();
        }
    }
}
