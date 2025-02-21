package com.auth;
import com.accounts.Account;
import com.accounts.AccountService;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gson.Gson;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.net.URI;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@Path("/auth")
public class AuthResource{


    @GET
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/login")
    public Response login() throws IOException {
        String CLIENT_ID = System.getenv("CLIENT_ID");
        String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
        String REDIRECT_URI = System.getenv("REDIRECT_URI");
        Collection<String> scopes = new ArrayList<>(List.of("https://www.googleapis.com/auth/userinfo.email"));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), CLIENT_ID, CLIENT_SECRET, scopes).setAccessType("offline")
                .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                .build();
        String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).setScopes(scopes).build();
        return Response.status(Response.Status.FOUND).location(URI.create(authorizationUrl)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/callback")
    public Response exchangeCode(@QueryParam("code") String code) throws IOException {
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");
        String redirectUri = System.getenv("REDIRECT_URI");
        HttpTransport httpTransport = new NetHttpTransport();
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                GsonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                code,
                redirectUri
        ).execute();
        System.out.println(tokenResponse);
        //got token where should we store next?


        //get user info
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        HttpRequest request = requestFactory.buildGetRequest(new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo?alt=json&access_token=" + tokenResponse.getAccessToken()));
        HttpResponse response = request.execute();
        GoogleIdToken.Payload payload = new Gson().fromJson(response.parseAsString(), GoogleIdToken.Payload.class);

        String jwt = JwtService.buildJwt(payload.getEmail());

        NewCookie jwtCookie = new NewCookie.Builder("jwt")
                .value(jwt)
                .path("/")
                .comment("JWT Token")
                .maxAge(3600)
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
        return Response.ok().cookie(jwtCookie).entity(Map.of("payload", payload, "jwt", jwt)).build();
    }

    @GET
    @Path("/jwt")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getJwt(@HeaderParam("Session-Id") String sessionId) {
        if (sessionId == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("[\"Missing Session Id!\"]").build();
        }

        ObjectId objectId;

        try {
            objectId = new ObjectId(sessionId);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("[\"Invalid session id!\"]")
                    .build();
        }

        AccountService accountService = new AccountService();
        Document user = accountService.accountCollection.find(eq("_id", objectId)).first();

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("[\"Invalid Session Id!\"]").build();
        }

        String email = user.getString("email");

        String jwt = JwtService.buildJwt(email);

        return Response.ok(jwt).build();
    }


    public String RefreshAccessToken(String refreshToken) throws IOException {
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");
        String tokenUrl = System.getenv("REDIRECT_URI");
        GoogleTokenResponse tokenResponse = null;
        try {
            tokenResponse = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    refreshToken,
                    clientId,
                    clientSecret
            ).execute();
            return tokenResponse.toString();
        }catch (TokenResponseException e){
            System.out.println("Refresh token error!");
        }
        return null;
    }
}
