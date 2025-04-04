package com.usedQuotes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import java.util.Date;
import com.accounts.*;

@Path("/useQuote")
public class UsedQuoteResource {

    public static AccountService accountService = new AccountService();
    public static UsedQuoteService usedQuoteService = new UsedQuoteService();

    @POST
    @Path("/use/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "User Successfully used quote.", content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "404", description = "That quote is not in your collection"),
    })
    @Operation(summary = "Uses a quote.")
    public Response useQuote(@PathParam("id") String id, @Context HttpHeaders headers) {

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Document userDoc = accountService.retrieveUserFromJWT(jwtString);
        
        if (userDoc == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to use quote").toJson()).build();
        }
        Account requestUser = accountService.document_to_account(userDoc);
        String accountId = accountService.getAccountIdByEmail(requestUser.Email);
        Date currentDate = new Date();
        if(!requestUser.UsedQuotes.containsKey(id)){
            UsedQuote usedQuote = new UsedQuote(1, currentDate);
            ObjectId usedQuoteId = usedQuoteService.newUsedQuote(usedQuote);
            requestUser.UsedQuotes.put(id, usedQuoteId.toString());
            Document backToDoc = accountService.account_to_document(requestUser);
            backToDoc.remove("expires_at");
            Response updateUser = accountService.updateUser(backToDoc.toJson(), accountId);
             if(updateUser.getStatus()==Response.Status.OK.getStatusCode()){
                return Response
                .status(Response.Status.OK)
                .entity(usedQuote.getUsed())
                .build();
             }
             else{
                return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new Document("error", "Failed to update user!").toJson())
                .build();
             }
        }
        else{
        Document usedQuoteDoc = usedQuoteService.retrieveUsedQuote(requestUser.UsedQuotes.get(id));
        int currentCount = usedQuoteDoc.getInteger("count", 1);
        usedQuoteDoc.put("count", currentCount + 1);
        usedQuoteDoc.put("used", currentDate);
        
        Response updateUsedQuotes =  usedQuoteService.updateUsedQuote(usedQuoteDoc.toJson(), requestUser.UsedQuotes.get(id));
        if(updateUsedQuotes.getStatus()==Response.Status.OK.getStatusCode()){
            return Response
            .status(Response.Status.OK)
            .entity(usedQuoteDoc.getDate("used"))
            .build();
         }
         else{
            return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(new Document("error", "Failed to update used quote!").toJson())
            .build();
         }
        }

    }   

    @GET
    @Path("/use/search/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully retrieved used quote.", content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "404", description = "That UsedQuote does not exist"),
    })
    @Operation(summary = "Retrieves a UsedQuote.")
    public Response searchUsedQuote(@PathParam("id") String id, @Context HttpServletRequest request) {
        Document usedQuoteDoc = usedQuoteService.retrieveUsedQuote(id);
        if(usedQuoteDoc != null){
        return Response
        .status(Response.Status.OK)
        .entity(usedQuoteDoc.toJson())
        .build();  
        }
        else{
            return Response
            .status(Response.Status.NOT_FOUND)
            .entity(new Document("error", "UsedQuote Not found!").toJson())
            .build();
        }
    }

}
