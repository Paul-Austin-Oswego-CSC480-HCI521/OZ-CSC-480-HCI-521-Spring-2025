package com.accounts;

import com.auth.AuthResource;
import com.auth.JwtService;
import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.UpdateResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

public class AccountService {

    public static MongoClient client;
    public static MongoDatabase accountDB;
    public static MongoCollection<Document> accountCollection;

    public AccountService() {
        String connectionString = System.getenv("CONNECTION_STRING");

        client = MongoClients.create(connectionString);
        accountDB = client.getDatabase("Accounts");
        accountCollection = accountDB.getCollection("Users");

    }

    public Response newUser(String accountJson) {
        Document accountDocument;
        try {
            accountDocument = Document.parse(accountJson);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Document("error", "Cannot parse JSON object!").toJson())
                    .build();
        }

        if (accountCollection.find(eq("email", accountDocument.getString("email"))).first() != null) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(new Document("error", "Email already exists!").toJson())
                    .build();
        }

        accountCollection.insertOne(accountDocument);

        return Response
                .status(Response.Status.OK)
                .entity(accountDocument.toJson())
                .build();

    }

    public Response newUserWithCookie(Account account) {
        Document accountDocument = Document.parse(account.toJson());
        Document oldAccountDocument = accountCollection.find(eq("Email", accountDocument.getString("Email"))).first();

        String id;

        if (oldAccountDocument != null) {
            Document updateFields = new Document();
            updateFields.append("access_token", account.access_token);
            updateFields.append("expires_at", account.expires_at);
            updateFields.append("scope", account.scope);
            updateFields.append("token_type", account.token_type);

            id = oldAccountDocument.getObjectId("_id").toString();
            accountCollection.updateOne(oldAccountDocument,
                    new Document("$set", updateFields));
        } else {
            id = accountCollection.insertOne(accountDocument).getInsertedId().asObjectId().getValue().toString();
        }

        System.out.println(id);

        NewCookie cookie = new NewCookie.Builder("jwt")
                .value(JwtService.buildJwt(id))
                .path("/")
                .comment("JWT Token")
                .maxAge(3600)
                .secure(true)
                .sameSite(NewCookie.SameSite.NONE)
                .build();

        return Response
                .status(Response.Status.FOUND)
                .cookie(cookie)
                .location(URI.create(AuthResource.HOME_URL))
                .build();

    }

    public Response retrieveUser(String accountID, Boolean includeOauth) {
        ArrayList<String> fieldsList = new ArrayList<String>(
                List.of("Email", "Username", "admin", "Notifications", "MyQuotes",
                        "FavoriteQuote", "SharedQuotes", "MyTags", "Profession", "PersonalQuote"));

        if (includeOauth) {
            List<String> oauthList = List.of("access_token", "refresh_token", "expires_at", "scope",
                    "token_type");
            fieldsList.addAll(oauthList);
        }

        ObjectId objectId;

        try {
            objectId = new ObjectId(accountID);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "Invalid object id!").toJson())
                    .build();
        }

        Bson projectionFields = Projections.fields(
                Projections.include(fieldsList));
        Document doc = accountCollection.find(eq("_id", objectId))
                .projection(projectionFields)
                .first();

        if (doc == null) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "Account not found!").toJson())
                    .build();
        }

        return Response
                .ok(doc.toJson())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    public Document retrieveUserFromCookie(HttpServletRequest request) {
        Cookie jwtCookie = null;

        if (request.getCookies() != null) {
            jwtCookie = Arrays.stream(request.getCookies())
                    .filter(c -> "jwt".equals(c.getName()))
                    .findFirst()
                    .orElse(null);
        }

        if (jwtCookie == null) {
            return null;
        }
        try {
            JwtConsumer consumer = JwtConsumer.create("defaultJwtConsumer");
            JwtToken jwt = consumer.createJwt(jwtCookie.getValue());

            String id = jwt.getClaims().getSubject();

            ObjectId objectId;
            try {
                objectId = new ObjectId(id);
            } catch (Exception e) {
                return null;
            }

            return accountCollection.find(eq("_id", objectId)).first();
        } catch (InvalidConsumerException | InvalidTokenException e) {
            System.out.println(e);
            return null;
        }
    }

    public Response deleteUser(String accountID) {
        ObjectId objectId;

        try {
            objectId = new ObjectId(accountID);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "Invalid object id!").toJson())
                    .build();
        }

        MongoCollection<Document> users = accountDB.getCollection("Users");
        Bson query = eq("_id", objectId);
        users.deleteOne(query);

        return Response
                .status(Response.Status.OK)
                .build();
    }

    public Response updateUser(String updatedAccountJson, String id) {
        ObjectId objectId;

        try {
            objectId = new ObjectId(id);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "Invalid object id!").toJson())
                    .build();
        }

        Document updatedAccountDocument;
        try {
            updatedAccountDocument = Document.parse(updatedAccountJson);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Document("error", "Cannot parse Json!").toJson())
                    .build();
        }

        UpdateResult updateResult = accountCollection.updateOne(
                eq("_id", objectId),
                new Document("$set", updatedAccountDocument)
        );

        if (updateResult.getModifiedCount() == 1) {
            try {
                return Response
                        .status(Response.Status.OK)
                        .entity(accountCollection.find(eq("_id", objectId)).first().toJson())
                        .build();
            } catch (NullPointerException e) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(new Document("error", "Account Not found!").toJson())
                        .build();
            }
        } else {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "Account Not found!").toJson())
                    .build();
        }
    }

    public Account document_to_account(Document document) {
        String email = document.getString("Email");
        String username = document.getString("Username");
        int admin = document.getInteger("admin", 0);
        String accessToken = document.getString("access_token");
        String refreshToken = document.getString("refresh_token");
        Long expiresAt = document.getLong("expires_at");
        List<String> scope = document.getList("scope", String.class);
        String tokenType = document.getString("token_type");
        List<String> notifications = document.getList("Notifications", String.class);
        List<String> myQuotes = document.getList("MyQuotes", String.class);
        Map<String, List<String>> favoriteQuotes = (Map<String, List<String>>) document.get("FavoriteQuote");
        List<String> sharedQuotes = document.getList("SharedQuotes", String.class);
        List<String> myTags = document.getList("MyTags", String.class);
        String profession = document.getString("Profession");
        String personalQuote = document.getString("PersonalQuote");

        Account account = new Account(email, username, admin, accessToken, refreshToken, expiresAt, scope, tokenType,
                notifications, myQuotes, favoriteQuotes, sharedQuotes, myTags, profession, personalQuote);

        return account;
    }

    public Document account_to_document(Account account) {
        Document document = new Document("Email", account.Email)
                .append("Username", account.Username)
                .append("admin", account.admin)
                .append("access_token", account.access_token)
                .append("refresh_token", account.refresh_token)
                .append("expires_at", account.expires_at)
                .append("scope", account.scope)
                .append("token_type", account.token_type)
                .append("Notifications", account.Notifications)
                .append("MyQuotes", account.MyQuotes)
                .append("FavoriteQuote", account.FavoriteQuote)
                .append("SharedQuotes", account.SharedQuotes)
                .append("MyTags", account.MyTags)
                .append("Profession", account.Profession)
                .append("PersonalQuote", account.PersonalQuote);

        return document;
    }

    public String getAccountIdByEmail(String email) {
        Document doc = accountCollection.find(eq("Email", email))
                .projection(Projections.include("_id"))
                .first();

        if (doc == null) {
            return null;
        }

        return doc.getObjectId("_id").toHexString();
    }

}
