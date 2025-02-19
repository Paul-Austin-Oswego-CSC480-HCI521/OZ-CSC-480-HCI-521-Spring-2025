package com.quotes;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

@Path("/search")
public class QuoteSearchResource {

    @Inject
    MongoUtil mongo;

    @GET
    @Path("/id/{quoteID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stringSearch(@PathParam("quoteID") String quoteID) {
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
            return Response.status(Response.Status.BAD_REQUEST).entity("Error: Invalid ObjectID format").build();
        }
    }

    @GET
    @Path("/query/{query}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response advancedSearch(@PathParam("query") String query) {
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSharedBookmarked() {
        try{
            String result = mongo.getTopShared();
            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT).entity("Exception Occurred: "+e).build();
        }
    }
}
