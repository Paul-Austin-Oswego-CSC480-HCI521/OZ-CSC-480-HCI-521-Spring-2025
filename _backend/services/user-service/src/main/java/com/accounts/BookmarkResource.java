package com.accounts;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;

@Path("/bookmarks")
public class BookmarkResource {
      
    @Inject
    @RestClient
    private QuoteClient quoteClient;

   
    public static AccountService accountService = new AccountService();

    @POST
    @Path("/{quoteId}")
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
    @Context HttpServletRequest request) {
        
        String json = null;
        String jwtCookie = null;
        Cookie cookies[] = request.getCookies();
        if(cookies!=null){
           for(Cookie cookie: cookies){
                if("jwt".equals(cookie.getName())){
                        jwtCookie = cookie.getValue();
                        break;
                }
           }     
        }
        if (jwtCookie == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("JWT Missing")
                        .build();
            }

         
        
            Document doc = accountService.retrieveUserFromCookie(request);
            doc.remove("expires_at");
            Account acc = accountService.document_to_account(doc);
            if(acc.MyQuotes.contains(quoteId)){
                return Response
            .status(Response.Status.BAD_REQUEST)
            .entity("That's your quote")
            .build();
            }
            String userId = accountService.getAccountIdByEmail(acc.Email);
            List<String> personalTags = new ArrayList<>();
            acc.BookmarkedQuotes.put(quoteId, personalTags);
            json = acc.toJson();
            Response quoteSearchRes = quoteClient.idSearch(quoteId);
            if(quoteSearchRes.getStatus()==Response.Status.OK.getStatusCode()){
            String quoteSearchString = quoteSearchRes.readEntity(String.class);
            Document quoteSearchDoc = Document.parse(quoteSearchString);
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
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Quotes successfully retrieved"),
            @APIResponse(responseCode = "400", description = "Invalid request"),
            @APIResponse(responseCode = "500", description = "Internal server error"),
    })
    @Operation(summary = "Grab bookmarked quotes for a user", description = "This endpoint allows a user to get all bookmarks for a user")
    public Response getBookmarks(@Context HttpServletRequest request) {
              
            Document doc = accountService.retrieveUserFromCookie(request);
            if(doc != null){
            doc.remove("expires_at");
            Account acc = accountService.document_to_account(doc);
            List<JsonObject> jsonList = new ArrayList<>();
            for(String objectId: acc.BookmarkedQuotes.keySet()){
            Response quoteSearchRes = quoteClient.idSearch(objectId);
            if(quoteSearchRes.getStatus()==Response.Status.OK.getStatusCode()){
            JsonObject quoteSearchJson = quoteSearchRes.readEntity(JsonObject.class);
            
            jsonList.add(quoteSearchJson);
            }
            }
            return Response
            .ok(jsonList).build();
        }
     return Response
     .status(Response.Status.BAD_REQUEST)
     .entity("Failed to retrieve account")
     .build();
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
    @Context HttpServletRequest request) {
       
        String jwtCookie = null;
        Cookie cookies[] = request.getCookies();
        if(cookies!=null){
           for(Cookie cookie: cookies){
                if("jwt".equals(cookie.getName())){
                        jwtCookie = cookie.getValue();
                        break;
                }
           }     
        }
        if (jwtCookie == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("JWT Missing")
                        .build();
            }
        String json = null;
         
        
            Document doc = accountService.retrieveUserFromCookie(request);
            if(doc==null){
                return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("Failed to retrieve account")
                .build();
                }
            doc.remove("expires_at");
            Account acc = accountService.document_to_account(doc);
            String userId = accountService.getAccountIdByEmail(acc.Email);        
            if(!acc.BookmarkedQuotes.containsKey(quoteId)){
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

    @POST
    @Path("/tag/{quoteId}/{bookmarkTag}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Bookmark tag added"),
            @APIResponse(responseCode = "400", description = "Invalid request"),
            @APIResponse(responseCode = "500", description = "Internal server error"),
    })
    @Operation(summary = "Allow users to add a custom tag to a bookmarked quote", description = "This endpoint allows to add a custom tag to a boomarked tag.")
    public Response addBookmarkTag(
        @PathParam("quoteId") String quoteId,
        @PathParam("bookmarkTag") String bookmarkTag,
        @Context HttpServletRequest request) {
       
        String json = null;
         
        String userId = null;
            Document doc = accountService.retrieveUserFromCookie(request);
            if(doc!=null){
            doc.remove("expires_at");
            Account acc = accountService.document_to_account(doc);
            if(!acc.BookmarkedQuotes.containsKey(quoteId)){
                return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("You don't have this bookmarked")
                .build();
            }
            userId = accountService.getAccountIdByEmail(acc.Email);
            acc.BookmarkedQuotes.get(quoteId).add(bookmarkTag);
            json = acc.toJson();
            }
         return accountService.updateUser(json, userId);
    }

    @DELETE
    @Path("/tag/{quoteId}/{bookmarkTag}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Bookmark tag deleted"),
            @APIResponse(responseCode = "400", description = "Invalid request"),
            @APIResponse(responseCode = "500", description = "Internal server error"),
    })
    @Operation(summary = "Allow users to delete a custom tag of a bookmarked quote", description = "This endpoint allows delete their custom tag from a quote.")
    public Response deleteBookmarkTag(
        @PathParam("quoteId") String quoteId,
        @PathParam("bookmarkTag") int tagIndex,
        @Context HttpServletRequest request) {

        String json = null;
            String userId = null;
            Document doc = accountService.retrieveUserFromCookie(request);
            doc.remove("expires_at");
            if(doc!=null){
             
            Account acc = accountService.document_to_account(doc);
            userId = accountService.getAccountIdByEmail(acc.Email);
            acc.BookmarkedQuotes.get(quoteId).remove(tagIndex);
            json = acc.toJson();
            }
         return accountService.updateUser(json, userId);
    }

}
