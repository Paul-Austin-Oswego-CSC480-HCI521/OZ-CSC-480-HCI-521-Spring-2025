package com.quotes;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.*;
import org.bson.types.ObjectId;


@Path("/delete")
public class QuoteDeleteResource {

    @Inject
    MongoUtil mongo;

    @DELETE
    @Path("/{quoteId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteQuote(@PathParam("quoteId") String quoteID) {
        try{
            //check id is in valid form
            if(!SanitizerClass.validObjectId(quoteID)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Given ID is not valid ObjectId").build();
            }

            ObjectId objectId = new ObjectId(quoteID);
            boolean result = mongo.deleteQuote(objectId);
            if(result) {
                return Response.ok("Quote successfully deleted").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Quote not found, could not be deleted").build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request: "+e).build();
        }
    }
}
