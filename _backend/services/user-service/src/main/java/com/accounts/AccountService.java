package com.accounts;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.UpdateResult;
import jakarta.ws.rs.core.MediaType;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
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
                    .entity("[\"Cannot parse JSON object!\"]")
                    .build();
        }

        if (accountCollection.find(eq("email", accountDocument.getString("email"))).first() != null) {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("[\"Email already exists!\"]")
                    .build();
        }

        accountCollection.insertOne(accountDocument);

        return Response
                .status(Response.Status.OK)
                .entity(accountDocument.toJson())
                .build();

    }

    public Response retrieveUser(String accountID, Boolean includeOauth) {
        ArrayList<String> fieldsList = new ArrayList<String>(
                List.of("email", "username", "admin", "notifications", "myQuotes",
                        "favoriteQuote", "sharedQuotes", "myTags", "description"));

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
                    .entity("[\"Invalid object id!\"]")
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
                    .entity("[\"Account not found!\"]")
                    .build();
        }

        return Response
                .ok(doc.toJson())
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    public Response deleteUser(String accountID) {
        ObjectId objectId;

        try {
            objectId = new ObjectId(accountID);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("[\"Invalid object id!\"]")
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
                    .status(Response.Status.BAD_REQUEST)
                    .entity("[\"Invalid object id!\"]")
                    .build();
        }

        Document updatedAccountDocument;
        try {
            updatedAccountDocument = Document.parse(updatedAccountJson);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("[\"Cannot parse Json!\"]")
                    .build();
        }

        UpdateResult updateResult = accountCollection.replaceOne(
                eq("_id", objectId), updatedAccountDocument);

        if (updateResult.getModifiedCount() == 1) {
            return Response
                    .status(Response.Status.OK)
                    .entity(updatedAccountDocument.toJson())
                    .build();
        } else {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("[\"Account Not found!\"]")
                    .build();
        }
    }

    public Account document_to_account(Document document) {
        String email = document.getString("email");
        String username = document.getString("username");
        int admin = document.getInteger("admin", 0);
        String accessToken = document.getString("access_token");
        String refreshToken = document.getString("refresh_token");
        Long expiresAt = document.getLong("expires_at");
        List<String> scope = document.getList("scope", String.class);
        String tokenType = document.getString("token_type");
        List<String> notifications = document.getList("notifications", String.class);
        List<String> myQuotes = document.getList("myQuotes", String.class);
        Map<String, List<String>> favoriteQuotes = (Map<String, List<String>>) document.get("favoriteQuote");
        List<String> sharedQuotes = document.getList("sharedQuotes", String.class);
        List<String> myTags = document.getList("myTags", String.class);
        String description = document.getString("description");

        Account account = new Account(email, username, admin, accessToken, refreshToken, expiresAt, scope, tokenType,
                notifications, myQuotes, favoriteQuotes, sharedQuotes, myTags, description);

        return account;
    }

    public Document account_to_document(Account account) {
        Document document = new Document("email", account.Email)
                .append("username", account.Username)
                .append("admin", account.admin)
                .append("access_token", account.access_token)
                .append("refresh_token", account.refresh_token)
                .append("expires_at", account.expires_at)
                .append("scope", account.scope)
                .append("token_type", account.token_type)
                .append("notifications", account.Notifications)
                .append("myQuotes", account.MyQuotes)
                .append("favoriteQuote", account.FavoriteQuote)
                .append("sharedQuotes", account.SharedQuotes)
                .append("myTags", account.MyTags)
                .append("description", account.Description);

        return document;
    }

    public String getAccountIdByEmail(String email) {
        Document doc = accountCollection.find(eq("email", email))
                .projection(Projections.include("_id"))
                .first();

        if (doc == null) {
            return null;
        }

        return doc.getObjectId("_id").toHexString();
    }



}

