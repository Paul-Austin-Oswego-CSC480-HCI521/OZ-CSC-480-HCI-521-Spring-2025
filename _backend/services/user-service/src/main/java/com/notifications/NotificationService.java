package com.notifications;

import com.accounts.AccountService;
import com.accounts.MongoUtil;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;

import java.io.StringWriter;

public class NotificationService {

    @Inject
    MongoUtil mongoUtil;

    private MongoClient mongoClient;
    private MongoDatabase accountDatabase;
    private MongoDatabase dataDatabase;
    private MongoDatabase moderationDatabase;
    private MongoCollection<Document> notificationsCollection;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> quotesCollection;
    private MongoCollection<Document> deleteCollection;
    private MongoCollection<Document> reportCollection;
    public AccountService accountService;

    public NotificationService() {
        mongoClient = MongoClients.create(System.getenv("CONNECTION_STRING"));

        accountDatabase = mongoClient.getDatabase("Accounts");
        notificationsCollection = accountDatabase.getCollection("Notifications");
        usersCollection = accountDatabase.getCollection("Users");

        dataDatabase = mongoClient.getDatabase("Data");
        quotesCollection = dataDatabase.getCollection("Quotes");

        moderationDatabase = mongoClient.getDatabase("Moderation");
        deleteCollection = moderationDatabase.getCollection("Deleted");
        reportCollection = moderationDatabase.getCollection("Reports");


        accountService = new AccountService();
    }

    public MongoCollection<Document> getNotificationsCollection() {return notificationsCollection;}
    public MongoCollection<Document> getUsersCollection() {return usersCollection;}
    public MongoCollection<Document> getQuotesCollection() {return quotesCollection;}
    public MongoCollection<Document> getDeleteCollection() {return deleteCollection;}
    public MongoCollection<Document> getReportCollection() {return reportCollection;}

    @PostConstruct
    public void init(){

    }

    public NotificationService(String connectionString) {
        mongoClient = MongoClients.create(connectionString);

        accountDatabase = mongoClient.getDatabase("Test");
        notificationsCollection = accountDatabase.getCollection("Notifications");
        usersCollection = accountDatabase.getCollection("Users");

        quotesCollection = accountDatabase.getCollection("Quotes");

        deleteCollection = accountDatabase.getCollection("Deleted");
        reportCollection = accountDatabase.getCollection("Reports");

        accountService = new AccountService(mongoClient, "Test", "Users");
    }


    public boolean isValidObjectId(String id) {
        try {
            new ObjectId(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getNotificationsByUser(ObjectId userId) {
        Document query = new Document("to", userId);
        FindIterable<Document> notifications = notificationsCollection.find(query);
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (Document doc : notifications) {
            if (doc.containsKey("_id")) {
                doc.put("_id", doc.getObjectId("_id").toString());
            }
            if (doc.containsKey("from")) {
                doc.put("from", doc.getObjectId("from").toString());
            }
            if (doc.containsKey("to")) {
                doc.put("to", doc.getObjectId("to").toString());
            }
            if (doc.containsKey("quote_id")) {
                doc.put("quote_id", doc.getObjectId("quote_id").toString());
            }
            JsonObject jsonObject = Json.createReader(new java.io.StringReader(doc.toJson())).readObject();
            jsonArrayBuilder.add(jsonObject);
        }
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            jsonWriter.writeArray(jsonArrayBuilder.build());
        }
        return stringWriter.toString();
    }

}
