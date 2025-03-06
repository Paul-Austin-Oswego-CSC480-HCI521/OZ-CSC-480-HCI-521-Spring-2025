package com.accounts;

import jakarta.annotation.security.RolesAllowed;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Response.Status;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;

import java.security.Principal;
import java.util.Arrays;

@Path("/accounts")
public class AccountsResource {

    public static AccountService accountService = new AccountService();

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "User Successfully created and added to the database. Will return new account document.", content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "400", description = "Invalid JSON format"),
            @APIResponse(responseCode = "409", description = "Account with the same email already exists"),
    })
    @Operation(summary = "Creates a new user account. Ensure the request header is `application/json` and provide a JSON body in the specified format.")
    @RequestBody(description = "Example request body endpoint is expecting.", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, examples = @ExampleObject(name = "Example", value = "{\"email\": \"Example Email\", "
            + "\"Username\": \"Example Name\", \"admin\": 1," + "\"access_token\": \"sample_access_token\", "
            + "\"refresh_token\": \"sample_refresh_token\", " + "\"expires_at\": 1700000000, "
            + "\"scope\": [\"read\", \"write\"], " + "\"token_type\": \"Bearer\", "
            + "\"Notifications\": [\"Welcome message\"], " + "\"MyQuotes\": [\"Life is beautiful\"], "
            + "\"FavoriteQuote\": {\"Motivation\": [\"Keep going!\"]}, "
            + "\"SharedQuotes\": [\"Success is a journey\"], "
            + "\"MyTags\": [\"Inspiration\", \"Wisdom\"], " + "\"Profession\": \"NFL Head Coach\"," + "\"PersonalQuote\": \"67abf469b0d20a5237456444\"" + "}")))
    public Response create(String json) {
        return accountService.newUser(json);
    }

    @GET
    @Path("/search/{id}")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Account has been found. Will return the account as json.", content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "404", description = "Account has not been found in the database.")
    })
    @Operation(summary = "Search a user account by ID.")
    public Response search(@PathParam("id") String id) {
        return accountService.retrieveUser(id, true);
    }

    @GET
    @Path("/search/email/{email}")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Account has been found. Will return the account as json.", content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "404", description = "Account has not been found in the database.")
    })
    @Operation(summary = "Search a user account by email address.")
    public Response searchByEmail(@PathParam("email") String email) {
        return accountService.retrieveUserByEmail(email, true);
    }    

    @DELETE
    @Path("/delete/{id}")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Account has been deleted."),
            @APIResponse(responseCode = "404", description = "Account has not been found in the database.")
    })
    @Operation(summary = "Delete a user account by ID.")
    public Response delete(@PathParam("id") String id, @Context HttpServletRequest request) {

        Document userDoc = accountService.retrieveUserFromCookie(request);

        if (userDoc == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to delete account").toJson()).build();
        }

        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "Invalid object id!").toJson())
                    .build();
        }

        if (!objectId.equals(userDoc.get("_id")) && userDoc.getInteger("admin") != 1) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to delete account").toJson()).build();
        }

        return accountService.deleteUser(id);
    }

    @PUT
    @Path("/update/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "User Successfully updated in the database. Will return updated account document.", content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "400", description = "Invalid JSON format."),
            @APIResponse(responseCode = "404", description = "Account was not found or the ID was invalid."),
    })
    @Operation(summary = "Updates a user account. Ensure the request header is `application/json` and provide a JSON body in the specified format.")
    @RequestBody(description = "Example request body endpoint is expecting.", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON, examples = @ExampleObject(name = "Example", value = "{ " + "\"SharedQuotes\": [\"Success is a journey\"]" + " }")))
    public Response update(@PathParam("id") String id, String accountJson, @Context HttpServletRequest request) {
        Document userDoc = accountService.retrieveUserFromCookie(request);

        if (userDoc == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to update account").toJson()).build();
        }

        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "Invalid object id!").toJson())
                    .build();
        }

        if (!objectId.equals(userDoc.get("_id")) && userDoc.getInteger("admin") != 1) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to update account").toJson()).build();
        }

        return accountService.updateUser(accountJson, id);
    }

    @GET
    @Path("/whoami")
    @Produces(MediaType.APPLICATION_JSON)
    public Response whoAmI(@Context HttpServletRequest request) {
        System.out.println(request.getCookies());
        Cookie jwtCookie = Arrays.stream(request.getCookies())
                .filter(c -> "jwt".equals(c.getName()))
                .findFirst()
                .orElse(null);

        if (jwtCookie == null) {
            return Response
                    .status(Status.UNAUTHORIZED)
                    .entity("{\"error\": \"This endpoint requires authentication\" }")
                    .build();
        }
        try {
            JwtConsumer consumer = JwtConsumer.create("defaultJwtConsumer");
            JwtToken jwt = consumer.createJwt(jwtCookie.getValue());

            String id = jwt.getClaims().getSubject();
            return accountService.retrieveUser(id, false);
        } catch (InvalidConsumerException e) {
            System.out.println(e);
            return Response
                    .status(Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"JwtConsumer is incorrectly configured\" }")
                    .build();
        } catch (InvalidTokenException e) {
            System.out.println(e);
            return Response
                    .status(Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid JWT\" }")
                    .build();
        }
    }

}
