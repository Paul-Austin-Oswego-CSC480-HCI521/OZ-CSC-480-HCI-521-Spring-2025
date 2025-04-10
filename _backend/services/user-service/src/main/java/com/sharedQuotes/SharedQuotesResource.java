package com.sharedQuotes;

import javax.management.Notification;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.accounts.Account;
import com.accounts.AccountService;
import com.accounts.QuoteClient;
import com.notifications.NotificationObject;
import com.notifications.NotificationResource;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/sharedQuotes")
public class SharedQuotesResource {
    
    @Inject
    @RestClient
    private QuoteClient quoteClient;

    public static AccountService accountService = new AccountService();

    @POST
    @Path("/share/{email}/{quoteId}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Quote successfully shared"),
            @APIResponse(responseCode = "400", description = "Invalid request"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "500", description = "Internal server error"),
    })
    @Operation(summary = "Share a quote with a user", description = "This endpoint allows a user to share quotes.")
    public Response shareQuote(
    @PathParam("quoteId") String quoteId,
    @PathParam("email") String email,
    @Context HttpHeaders headers) {

        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");
     
      Response searchUserTo = accountService.retrieveUserByEmail(email, false);
      if(searchUserTo.getStatus()!=Response.Status.OK.getStatusCode()){
        return Response.status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "No user found with that email").toJson())
                    .build();
      }
      String userToString = searchUserTo.readEntity(String.class);
      Document docTo = Document.parse(userToString);
      docTo.remove("expires_at");

            Document docFrom = accountService.retrieveUserFromJWT(jwtString);
            docFrom.remove("expires_at");
            Account accFrom = accountService.document_to_account(docFrom);
            String ToId = accountService.getAccountIdByEmail(email);
            String fromId = accountService.getAccountIdByEmail(accFrom.Email);
            if(fromId.equals(ToId)){
                return Response.status(Response.Status.BAD_REQUEST)
                            .entity(new Document("error", "From and to must be distinct").toJson())
                            .build();
              }
            Account accTo = accountService.document_to_account(docTo);
            Response quoteSearchRes;
            try{
                quoteSearchRes = quoteClient.idSearch(quoteId);
            }
            catch(WebApplicationException e){
              quoteSearchRes = e.getResponse();
            }
            if(quoteSearchRes.getStatus()==Response.Status.OK.getStatusCode()){
            String quoteSearchString = quoteSearchRes.readEntity(String.class);
            Document quoteSearchDoc = Document.parse(quoteSearchString);
            Boolean priv = quoteSearchDoc.getBoolean("private");
            if(priv){
                if(!quoteSearchDoc.getString("creator").equals(fromId)){
                    return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "You can't share this it's private").toJson())
                    .build();
                }
            }
        
            SharedQuote shared = new SharedQuote();
            shared.setTo(ToId);
            shared.setFrom(fromId);
            shared.setQuoteId(quoteId);
            JsonObject json = Json.createObjectBuilder()
            .add("to", shared.getTo()) 
            .add("from", shared.getFrom()) 
            .add("quote_id", shared.getQuoteId()) 
            .add("type","Share")
            .build();
            Client notifClient = ClientBuilder.newClient();
            WebTarget target = notifClient.target("http://user-service:9081/users/notifications/create");
            Invocation.Builder notifReq = target.request().header(HttpHeaders.AUTHORIZATION, authHeader);
            Response notifRes = notifReq.post(Entity.json(json.toString()));
            notifClient.close();
            if(notifRes.getStatus()!=Response.Status.CREATED.getStatusCode()){
                return notifRes;
            }
            accTo.SharedQuotes.add(shared);
            accFrom.SharedQuotes.add(shared);
            String notResString = notifRes.readEntity(String.class);
            Document notResDoc = Document.parse(notResString);
            accTo.Notifications.add(notResDoc.getString("notification_id"));
            String toJson = accTo.toJson();
            String fromJson = accFrom.toJson();
            Response updateTo = accountService.updateUser(toJson, ToId);
            Response updateFrom = accountService.updateUser(fromJson, fromId);
            if(updateTo.getStatus()!=Response.Status.OK.getStatusCode()){
                return updateTo;
            }   
            if(updateFrom.getStatus()!=Response.Status.OK.getStatusCode()){
                return updateFrom;
            }   
           
            return Response.status(Response.Status.OK)
            .entity(shared)
            .build();
           
        }
            else{
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "No quote found with that id").toJson())
                    .build();
            }
         
    }
}
