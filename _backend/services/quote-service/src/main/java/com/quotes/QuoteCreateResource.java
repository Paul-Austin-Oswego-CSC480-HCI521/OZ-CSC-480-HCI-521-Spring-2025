package com.quotes;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
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
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import com.moderation.ProfanityClass;

@Path("/create")
public class QuoteCreateResource {

    @Inject
    QuoteService quoteService;

    @Inject 
    private UserClient userClient;

    private ProfanityClass profanityFilter = new ProfanityClass();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The quote was successfully added to the database. Will return new quote ID",
            content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "409", description = "Error when sanitizing quote texts, attempted to remove all special characters, returned null"),
            @APIResponse(responseCode = "400", description = "Error when adding quote to database, returned quote ID was null"),
    })
    @Operation(summary = "Adds a new quote to the mongo database and will return the id of the newly created quote")
    @RequestBody(description = "Example request body endpoint is expecting. The only fields required are \"author\", \"quote\", \"tags\", \"creator\", and \"private\"",
            required = true, content = @Content(
            mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = QuoteObject.class),
            examples = @ExampleObject(name = "Example", value = "{\"author\": \"Example Author\", " +
                    "\"quote\": \"Example quote text\"," +
                    "\"tags\": [\"example\", \"another example\"]," +
                    "\"creator\": \"Account Object ID\", \"private\": \"boolean value\"}")
    ))
    public Response createQuote(String rawJson, @Context HttpHeaders headers) {
        try{
            String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                        .build();
            }

            String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

            Map<String, String> jwtMap= QuotesRetrieveAccount.retrieveJWTData(jwtString);

            if (jwtMap == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to create quotes").toJson()).build();
            }

            // get account ID from JWT
            String accountID = jwtMap.get("subject");

            // get group from JWT
            String group = jwtMap.get("group");

            if (group == null || accountID == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to create quotes").toJson()).build();
            }

            ObjectId accountObjectId = new ObjectId(accountID);

            //map json to Java Object
            ObjectMapper objectMapper = new ObjectMapper();
            QuoteObject quote = objectMapper.readValue(rawJson, QuoteObject.class);

            quote.setCreator(accountObjectId);

            quote = SanitizerClass.sanitizeQuote(quote);
            if(quote == null) {
                return Response.status(Response.Status.CONFLICT).entity("Error when sanitizing quote, returned null").build();
            }

            if(profanityFilter.checkProfanity(quote.getText())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Quote content is inappropiate").build();
            }
            if(profanityFilter.checkProfanity(quote.getAuthor())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Author content is inappropiate").build();
            }
            

            ObjectId newQuoteId = quoteService.createQuote(quote); //add to mongo database
            Response findAccount = userClient.search(accountID);
            if (findAccount.getStatus() == Response.Status.OK.getStatusCode()) {
                String accSearchString = findAccount.readEntity(String.class);
                Document accDoc = Document.parse(accSearchString);
                List<String> MyQuotes = accDoc.getList("MyQuotes", String.class);
                MyQuotes.add(newQuoteId.toString());
                accDoc.put("MyQuotes",MyQuotes);
                Response updateUser = userClient.updateMyQuotes(accountID,accDoc.toJson());
                
            }
            
            if(newQuoteId != null) {
                JsonObject jsonResponse = Json.createObjectBuilder()
                        .add("_id", newQuoteId.toHexString())
                        .build();
                return Response.ok(jsonResponse).build();

            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Returned QuoteID null. Check json is formatted Correctly").build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Exception occured: "+e).build();
        }
    }
}
