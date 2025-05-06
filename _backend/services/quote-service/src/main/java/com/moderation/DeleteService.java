package com.moderation;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.quotes.QuoteObject;
import com.quotes.UserClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

@RequestScoped
public class DeleteService {

    @Inject
    private MongoClient client;

    private MongoDatabase moderationDB;
    private MongoCollection<Document> deletedCollection;
    private MongoCollection<Document> reportsCollection;

    public DeleteService() {}

    @PostConstruct
    public void init() {
        moderationDB = client.getDatabase("Moderation");
        deletedCollection = moderationDB.getCollection("Deleted");
        reportsCollection = moderationDB.getCollection("Reports");
    }

    public DeleteService(String connectionString) {
        client = MongoClients.create(connectionString);
        moderationDB = client.getDatabase("Test");
        deletedCollection = moderationDB.getCollection("Deleted");
        reportsCollection = moderationDB.getCollection("Reports");
    }

    public ObjectId createDeletedQuote(Document document) {
        deletedCollection.insertOne(document);
        return document.getObjectId("_id");
    }

    public void deleteReports(ObjectId quoteId) {
        Bson query = eq("quote_id", quoteId);
        reportsCollection.deleteMany(query);
    }

    public Response sendOwnerNotification(Document deleteDoc, ObjectId quoteId, String authHeader) {
        ObjectId to = deleteDoc.getObjectId("creator");
        ObjectId from = new ObjectId(deleteDoc.getString("adminID"));
        String type = "Delete";

        Document notificationDoc = new Document()
                .append("from", from.toString())
                .append("to", to.toString())
                .append("type", type)
                .append("quote_id", quoteId.toString());

        Client notifClient = ClientBuilder.newClient();
        WebTarget target = notifClient.target("http://user-service:9081/users/notifications/create");
        Invocation.Builder notifReq = target.request().header(HttpHeaders.AUTHORIZATION, authHeader);
        Response notifRes = notifReq.post(Entity.json(notificationDoc.toJson()));
        notifClient.close();

        return notifRes;
    }

}
