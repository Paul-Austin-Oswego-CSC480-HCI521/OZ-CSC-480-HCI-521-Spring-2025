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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.HttpHeaders;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


import static com.mongodb.client.model.Filters.eq;


@Path("/auth")
public class AuthResource {


    @Inject
    AccountService accountService;

    @Inject
    SessionService sessionService;
    public static String HOME_URL = "http://localhost:9080";


    @GET
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/login")
    public Response login() throws IOException {
        String CLIENT_ID = System.getenv("CLIENT_ID");
        String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
        String REDIRECT_URI = System.getenv("REDIRECT_URI");
        Collection<String> scopes = new ArrayList<>(List.of("https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/userinfo.profile"));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(), CLIENT_ID, CLIENT_SECRET, scopes).setAccessType("offline")
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
                redirectUri).execute();
        System.out.println(tokenResponse);
        // got token where should we store next?


        // get user info
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        HttpRequest request = requestFactory
                .buildGetRequest(new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo?alt=json&access_token="
                        + tokenResponse.getAccessToken()));
        HttpResponse response = request.execute();
        GoogleIdToken.Payload payload = new Gson().fromJson(response.parseAsString(), GoogleIdToken.Payload.class);


        Gson gson = new Gson();
        String jsonObject = gson.toJson(payload);
        JsonObject convertedObject = new Gson().fromJson(jsonObject, JsonObject.class);


        Account account = new Account(payload.getEmail(), convertedObject.get("name").getAsString(), 0,
                tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresInSeconds(), Arrays.asList(tokenResponse.getScope().split(" ")),
                tokenResponse.getTokenType());


        return accountService.newUserWithCookie(account);
    }

    @GET
    @Path("/checkJWT/{jwt}")
    public Response checkJWT(@PathParam("jwt") String jwt) {
        ArrayList<String> groups;
        String userId;
        try {
            JwtConsumer consumer = JwtConsumer.create("defaultJwtConsumer");
            JwtToken jwtToken = consumer.createJwt(jwt);

            groups = (ArrayList<String>) jwtToken.getClaims().get("groups");

            userId = jwtToken.getClaims().getSubject();

        } catch (InvalidConsumerException e) {
            System.out.println(e);
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"JwtConsumer is incorrectly configured\" }")
                    .build();
        } catch (InvalidTokenException e) {
            System.out.println(e);
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid JWT\" }")
                    .build();
        }

        int admin;
        if (groups.get(0) == null) {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Invalid JWT\" }")
                    .build();
        } else if (groups.get(0).equals("admin")) {
            admin = 1;
        } else {
            admin = 0;
        }

        Date expires = new Date(System.currentTimeMillis() + 21 * 24 * 60 * 60 * 1000);

        Date lastActivity = new Date(System.currentTimeMillis());

        Session session = new Session(userId, admin, expires, lastActivity);

        session.SessionId = new ObjectId();

        String sessionId = sessionService.createSession(session);

        NewCookie cookie = new NewCookie.Builder("SessionId")
                .value(sessionId)
                .path("/")
                .maxAge(21 * 24 * 60 * 60)
                .secure(true)
                .sameSite(NewCookie.SameSite.LAX)
                .httpOnly(true)
                .build();

        return Response.status(Response.Status.FOUND)
                .cookie(cookie)
                .location(URI.create(HOME_URL))
                .build();

    }

    @POST
    @Path("/jwt")
    public Response getJWT(@CookieParam("SessionId") String sessionId,
                           @QueryParam("redirectURL") String url,
                           String jsonBody) {

        if (sessionId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Session expired. Please log in again.\" }")
                    .build();
        }

        Session session = sessionService.getSession(sessionId);

        if (session == null || session.Expires.before(new Date())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"Session expired. Please log in again.\" }")
                    .build();
        }

        //sliding expiration
        session.LastActivity = new Date(System.currentTimeMillis());
        session.Expires = new Date(session.LastActivity.getTime() + 21 * 24 * 60 * 60 * 1000);

        Boolean success = sessionService.updateSession(sessionId, session);

        if (success) {
            String jwt = JwtService.buildJwt(session.UserId).toString();

            System.out.println("jwt "+jwt);

            JsonObject requestJson;
            try {
                requestJson = JsonParser.parseString(jsonBody).getAsJsonObject();
            } catch (Exception e) {
                return Response.status(400).entity("{\"error\":\"Invalid JSON\"}").build();
            }

            String method = requestJson.get("method").getAsString();
            String body;
            try {
                JsonElement bodyElement = requestJson.get("body");
                if (bodyElement == null || bodyElement.isJsonNull()) {
                    body = null;
                } else {
                    body = bodyElement.toString();
                }
            } catch (Exception e) {
                body = null;
            }

            URI redirectUri;
            try {
                redirectUri = new URI(url);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            String path = redirectUri.getPath();
            String targetService;

            if (path.startsWith("/users/")) {
                targetService = "http://user-service:9081";
            } else if (path.startsWith("/quotes/")) {
                targetService = "http://quotes-service:9082";
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Unsupported service path\"}")
                        .build();
            }

            String internalUrl = targetService + path;
            if (redirectUri.getQuery() != null) {
                internalUrl += "?" + redirectUri.getQuery();
            }

            System.out.println("Routing to: " + internalUrl);
            System.out.println("Method: " + method);
            System.out.println("Body: " + body);

            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(internalUrl);
            Invocation.Builder requestBuilder = target.request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + jwt)
                    .cookie("SessionId", sessionId);

            Response forwardResponse;
            try {
                switch (method) {
                    case "POST":
                        if (body != null) {
                            forwardResponse = requestBuilder.post(Entity.json(body));
                        } else {
                            forwardResponse = requestBuilder.post(null);
                        }
                        break;
                    case "PUT":
                        if (body != null) {
                            forwardResponse = requestBuilder.put(Entity.json(body));
                        } else {
                            forwardResponse = requestBuilder.put(null);
                        }
                        break;
                    case "DELETE":
                        forwardResponse = requestBuilder.delete();
                        break;
                    default:
                        forwardResponse = requestBuilder.get();
                        break;
                }

                return Response.status(forwardResponse.getStatus())
                        .entity(forwardResponse.readEntity(String.class))
                        .build();
            } catch (ProcessingException e) {
                return Response.status(Response.Status.BAD_GATEWAY)
                        .entity("{\"error\": \"Failed to connect to backend service\"}")
                        .build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("SessionId") String sessionId, @Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Document userDoc = accountService.retrieveUserFromJWT(jwtString);

        if (userDoc == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to logout").toJson()).build();
        }

        boolean success = sessionService.deleteSession(sessionId);

        if (success) {
            NewCookie cookie = new NewCookie.Builder("SessionId")
                    .value("")
                    .path("/")
                    .maxAge(0)
                    .secure(true)
                    .sameSite(NewCookie.SameSite.LAX)
                    .httpOnly(true)
                    .build();


            return Response
                    .status(Response.Status.OK)
                    .cookie(cookie)
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
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
                    clientSecret).execute();
            return tokenResponse.toString();
        } catch (TokenResponseException e) {
            System.out.println("Refresh token error!");
        }
        return null;
    }
}
