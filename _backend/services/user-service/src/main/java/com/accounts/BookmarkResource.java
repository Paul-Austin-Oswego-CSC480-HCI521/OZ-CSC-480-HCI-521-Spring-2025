package com.accounts;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.usedQuotes.UsedQuoteService;

import jakarta.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sharedQuotes.*;

@Path("/bookmarks")
public class BookmarkResource {
      
    @Inject
    @RestClient
    private QuoteClient quoteClient;

   
    public static AccountService accountService = new AccountService();
    public static UsedQuoteService usedQuoteService = new UsedQuoteService();


    @POST
    @Path("/add/{quoteId}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Quote successfully bookmarked"),
            @APIResponse(responseCode = "400", description = "Invalid request"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "500", description = "Internal server error"),
    })
    @Operation(summary = "Bookmark a quote for a user", description = "This endpoint allows a user to bookmark a quote.")
    public Response bookmarkQuote(
    @PathParam("quoteId") String quoteId,
    @Context HttpHeaders headers) {

        String json = null;
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Document doc = accountService.retrieveUserFromJWT(jwtString);
            doc.remove("expires_at");
            Account acc = accountService.document_to_account(doc);
            if(acc.BookmarkedQuotes.contains(quoteId)){
                return Response
            .status(Response.Status.BAD_REQUEST)
            .entity("You already bookmarked that")
            .build();
            }
            String userId = accountService.getAccountIdByEmail(acc.Email);
            acc.BookmarkedQuotes.add(quoteId);
            json = acc.toJson();
            Response quoteSearchRes;
            try{
                quoteSearchRes = quoteClient.idSearch(quoteId);
            }
            catch(WebApplicationException e){
                 quoteSearchRes = e.getResponse();
                 return Response.status(Response.Status.NOT_FOUND)
                 .entity(new Document("error", "That quote doesn't exist")
                 .toJson())
                 .build();
            }
            if(quoteSearchRes.getStatus()==Response.Status.OK.getStatusCode()){
            String quoteSearchString = quoteSearchRes.readEntity(String.class);
            Document quoteSearchDoc = Document.parse(quoteSearchString);
            
            if(quoteSearchDoc.getBoolean("private")
            &&!quoteSearchDoc.get("creator").toString().equals(userId)){
                Boolean sharedWithYou = false;
                for (SharedQuote shared : acc.SharedQuotes) {
                    if(shared.getTo().equals(userId)
                    && shared.getQuoteId().equals(quoteId)){
                        sharedWithYou = true;
                    }
                }
                if(!sharedWithYou){
                    return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "You can't boomark this it's private").toJson())
                    .build();
                }
             
            }
           
            int currentBookmarks = quoteSearchDoc.getInteger("bookmarks", 0);
            quoteSearchDoc.put("bookmarks", currentBookmarks + 1);
            quoteSearchDoc.remove("creator");
            Response quoteUpdateRes = quoteClient.updateQuote(quoteSearchDoc.toJson());
            if(quoteUpdateRes.getStatus()!=Response.Status.OK.getStatusCode()){
            return quoteUpdateRes;
         }
        }
        else{
                return quoteSearchRes;
        }
         return accountService.updateUser(json, userId);
    }

    @GET
    @Path("/filtered")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Quotes successfully retrieved"),
            @APIResponse(responseCode = "400", description = "Invalid request"),
            @APIResponse(responseCode = "500", description = "Internal server error"),
    })
    @Operation(summary = "Grab bookmarked with used quotes filtered out")
    public Response getFilteredBookmarks(@Context HttpHeaders header) {

        String authHeader = header.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Document doc = accountService.retrieveUserFromJWT(jwtString);
        if(doc != null){
            doc.remove("expires_at");
            Account acc = accountService.document_to_account(doc);
            List<JsonObject> jsonList = new ArrayList<>();
            List<String> updatedBookmarks = new ArrayList<>(acc.BookmarkedQuotes);
            for(String objectId: acc.BookmarkedQuotes){ //for all bookmarked quotes
                if(!acc.UsedQuotes.containsKey(objectId)){ //if quote id is not in used quotes map
                    Response quoteSearchRes;
                    try{
                        quoteSearchRes = quoteClient.idSearch(objectId);
                    } //get quote
                    catch(WebApplicationException e){
                        quoteSearchRes = e.getResponse();
                        updatedBookmarks.remove(objectId);
                    }
                    if(quoteSearchRes.getStatus()==Response.Status.OK.getStatusCode()){
                       

                        JsonObject quoteSearchJson = quoteSearchRes.readEntity(JsonObject.class);
                        if(quoteSearchJson.getBoolean("private")
                        &&!quoteSearchJson.get("creator").toString().equals(accountService.getAccountIdByEmail(acc.Email))){
                            Boolean sharedWithYou = false;
                            for (SharedQuote shared : acc.SharedQuotes) {
                                if(shared.getTo().equals(accountService.getAccountIdByEmail(acc.Email))
                                && shared.getQuoteId().equals(objectId)){
                                    sharedWithYou = true;
                                }
                            }
                            if(!sharedWithYou){
                                updatedBookmarks.remove(objectId);
                                continue;
                            }
                        }
                        jsonList.add(quoteSearchJson);
                    }
                }
            }
            acc.BookmarkedQuotes = updatedBookmarks;
            accountService.updateUser(acc.toJson(), accountService.getAccountIdByEmail(acc.Email));
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("Failed to retrieve account").build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Quotes successfully retrieved"),
            @APIResponse(responseCode = "400", description = "Invalid request"),
            @APIResponse(responseCode = "500", description = "Internal server error"),
    })
    @Operation(summary = "Grab bookmarked quotes for a user", description = "This endpoint allows a user to get all bookmarks for a user")
    public Response getBookmarks(@Context HttpHeaders headers) {

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Document doc = accountService.retrieveUserFromJWT(jwtString);

            if(doc != null){
            doc.remove("expires_at");
            Account acc = accountService.document_to_account(doc);
            List<JsonObject> jsonList = new ArrayList<>();
            List<String> updatedBookmarks = new ArrayList<>(acc.BookmarkedQuotes);
            for(String objectId: acc.BookmarkedQuotes){
            Response quoteSearchRes;
            try{
             quoteSearchRes = quoteClient.idSearch(objectId);
            }
            catch(WebApplicationException e){
            quoteSearchRes = e.getResponse();
            }
            if(quoteSearchRes.getStatus()==Response.Status.OK.getStatusCode()){
            JsonObject quoteSearchJson = quoteSearchRes.readEntity(JsonObject.class);
            if(quoteSearchJson.getBoolean("private")
            &&!quoteSearchJson.get("creator").toString().equals(accountService.getAccountIdByEmail(acc.Email))){
                Boolean sharedWithYou = false;
                            for (SharedQuote shared : acc.SharedQuotes) {
                                if(shared.getTo().equals(accountService.getAccountIdByEmail(acc.Email))
                                && shared.getQuoteId().equals(objectId)){
                                    sharedWithYou = true;
                                }
                            }
                            if(!sharedWithYou){
                                updatedBookmarks.remove(objectId);
                                continue;
                            }
                
            }
            jsonList.add(quoteSearchJson);
            }
            else if(quoteSearchRes.getStatus()==Response.Status.NOT_FOUND.getStatusCode()){
             updatedBookmarks.remove(objectId);
            }
            }
            acc.BookmarkedQuotes = updatedBookmarks;
            accountService.updateUser(acc.toJson(), accountService.getAccountIdByEmail(acc.Email));
            return Response
            .ok(jsonList).build();
        }
     return Response
     .status(Response.Status.BAD_REQUEST)
     .entity("Failed to retrieve account")
     .build();
    }

    @GET
    @Path("/UsedQuotes")
    @Operation(summary = "Get users used quotes.")
    public Response userUsedQuotes(@Context HttpHeaders header) {
        String authHeader = header.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");
        Document doc = accountService.retrieveUserFromJWT(jwtString);
        if(doc != null) {
            doc.remove("expires_at");
            Account account = accountService.document_to_account(doc);
            Map<String,String> updatedUsedQuotes = new HashMap<>(account.UsedQuotes);
            List<JsonObject> jsonList = new ArrayList<>();
            for(String oid: account.UsedQuotes.keySet()) {
                Response getQuote;
                try{
                 getQuote = quoteClient.idSearch(oid);
                }
                catch(WebApplicationException e){
                getQuote = e.getResponse();
                }
                if(getQuote.getStatus() == Response.Status.OK.getStatusCode()) {
                    JsonObject quoteObject = getQuote.readEntity(JsonObject.class);
                    if(quoteObject.getBoolean("private")
                    &&!quoteObject.get("creator").toString().equals(accountService.getAccountIdByEmail(account.Email))){
                        Boolean sharedWithYou = false;
                        for (SharedQuote shared : account.SharedQuotes) {
                            if(shared.getTo().equals(accountService.getAccountIdByEmail(account.Email))
                            && shared.getQuoteId().equals(oid)){
                                sharedWithYou = true;
                            }
                        }
                        if(!sharedWithYou){
                            usedQuoteService.deleteUsedQuote(account.UsedQuotes.get(oid));
                            updatedUsedQuotes.remove(oid);
                            continue;
                        }
                        
                       
                    }
                    jsonList.add(quoteObject);
                }
                else if(getQuote.getStatus() == Response.Status.NOT_FOUND.getStatusCode()){
                usedQuoteService.deleteUsedQuote(account.UsedQuotes.get(oid));
                 updatedUsedQuotes.remove(oid);
                
                }
            }
            account.UsedQuotes = updatedUsedQuotes;
            accountService.updateUser(account.toJson(), accountService.getAccountIdByEmail(account.Email));
            return Response.ok(jsonList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("Failed to retrieve account").build();
    }

    @GET
    @Path("/UsedQuotesIds")
    @Operation(summary = "Get users used quotes id's.")
    @Produces(MediaType.APPLICATION_JSON)
    public Response userUsedQuotesIds(@Context HttpHeaders header) {
        String authHeader = header.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");
        Document doc = accountService.retrieveUserFromJWT(jwtString);
        if(doc != null) {
            doc.remove("expires_at");
            Account account = accountService.document_to_account(doc);

            List<String> jsonList = new ArrayList<>(account.UsedQuotes.keySet());
            return Response.ok(jsonList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("Failed to retrieve account").build();
    }

    @DELETE
    @Path("/delete/{quoteId}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Bookmark successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid request"),
            @APIResponse(responseCode = "500", description = "Internal server error"),
    })
    @Operation(summary = "Delete a bookmark a for a user", description = "This endpoint allows a user to delete a bookmark")
    public Response deleteBookmark(
    @PathParam("quoteId") String quoteId,
    @Context HttpHeaders headers) {

        String json = null;


        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Document doc = accountService.retrieveUserFromJWT(jwtString);
            if(doc==null){
                return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Failed to retrieve account")
                .build();
                }
            doc.remove("expires_at");
            Account acc = accountService.document_to_account(doc);
            String userId = accountService.getAccountIdByEmail(acc.Email);        
            if(!acc.BookmarkedQuotes.contains(quoteId)){
                return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("You don't have this bookmarked")
                .build();
            }
            acc.BookmarkedQuotes.remove(quoteId);
            json = acc.toJson();
            Response quoteSearchRes = quoteClient.idSearch(quoteId);
            if(quoteSearchRes.getStatus()!=Response.Status.OK.getStatusCode()){
                return quoteSearchRes;
                }
            String quoteSearchString = quoteSearchRes.readEntity(String.class);
            Document quoteSearchDoc = Document.parse(quoteSearchString);
            quoteSearchDoc.remove("creator");
            int currentBookmarks = quoteSearchDoc.getInteger("bookmarks", 0);
            quoteSearchDoc.put("bookmarks", currentBookmarks - 1);
            Response quoteUpdateRes = quoteClient.updateQuote(quoteSearchDoc.toJson());
            if(quoteUpdateRes.getStatus()!=Response.Status.OK.getStatusCode()){
            return Response
            .status(Response.Status.BAD_GATEWAY)
            .entity("Failed to delete bookmark")
            .build();
            }
           

         
         return accountService.updateUser(json, userId);
    }

}
