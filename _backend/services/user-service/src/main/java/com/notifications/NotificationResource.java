package com.notifications;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import java.io.StringWriter;

@Path("/notifications")
public class NotificationResource {
    
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> notificationsCollection;
    
    static {
        mongoClient = MongoClients.create(System.getenv("CONNECTION_STRING"));
        database = mongoClient.getDatabase("Accounts");
        notificationsCollection = database.getCollection("Notifications");
    }

    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully found and returned user's notifications"),
            @APIResponse(responseCode = "400", description = "Given ID is not a valid ObjectId"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "Get all notifications for a specific user", 
              description = "Returns JSON of all notifications where the user is the recipient, enter ID of user recieving notifications")
    public Response getNotificationsForUser(@PathParam("userId") String userId) {
        try {
            if(!isValidObjectId(userId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Given ID is not a valid ObjectId")
                        .build();
            }
            ObjectId objectId = new ObjectId(userId);
            String jsonNotifications = getNotificationsByUser(objectId);
            return Response.ok(jsonNotifications).build();
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Exception Occurred: " + e)
                    .build();
        }
    }
    
    private boolean isValidObjectId(String id) {
        try {
            new ObjectId(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getNotificationsByUser(ObjectId userId) {
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
