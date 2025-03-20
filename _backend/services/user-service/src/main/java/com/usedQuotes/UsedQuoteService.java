package com.usedQuotes;

import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class UsedQuoteService {

    public static MongoClient client;
    public static MongoDatabase accountDB;
    public static MongoCollection<Document> usedQuoteCollection;

    public UsedQuoteService() {
        String connectionString = System.getenv("CONNECTION_STRING");

        client = MongoClients.create(connectionString);
        accountDB = client.getDatabase("Accounts");
        usedQuoteCollection = accountDB.getCollection("UsedQuotes");
    }

    public UsedQuoteService(String connectionString, String dbName, String collectionName) {
        client = MongoClients.create(connectionString);
        accountDB = client.getDatabase(dbName);
        usedQuoteCollection = accountDB.getCollection(collectionName);
    }

    public ObjectId newUsedQuote(UsedQuote usedQuote) {
        try{
            usedQuote.setId(new ObjectId());
            Document usedQuoteDoc = new Document()
                    .append("_id", usedQuote.getId())
                    .append("used", usedQuote.getUsed())
                    .append("count", usedQuote.getCount());
            usedQuoteCollection.insertOne(usedQuoteDoc);
            return usedQuote.getId(); 
        } catch (Exception e) {
            System.out.println("Exception in MongoUtil/createUsedQuote: "+e);
            return null;
        }

    }

   

    public Document retrieveUsedQuote(String usedQuoteID) {
        ArrayList<String> fieldsList = new ArrayList<String>(
                List.of("count", "used"));

        ObjectId objectId;
        try {
            objectId = new ObjectId(usedQuoteID);
        } catch (Exception e) {
            return null;
        }
        Bson projectionFields = Projections.fields(
                Projections.include(fieldsList));
        Document usedQuoteDoc = usedQuoteCollection.find(eq("_id", objectId))
                .projection(projectionFields)
                .first();

        if (usedQuoteDoc == null) {
        return null;
        }

        return usedQuoteDoc;
    }

    public Response updateUsedQuote(String updatedUsedQuoteJson, String id) {
        ObjectId objectId;

        try {
            objectId = new ObjectId(id);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "Invalid object id!").toJson())
                    .build();
        }

        Document updatedUsedQuoteDoc;
        try {
            updatedUsedQuoteDoc = Document.parse(updatedUsedQuoteJson);
        } catch (Exception e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new Document("error", "Cannot parse Json!").toJson())
                    .build();
        }

        UpdateResult updateResult = usedQuoteCollection.updateOne(
                eq("_id", objectId),
                new Document("$set", updatedUsedQuoteDoc)
        );

        if (updateResult.getModifiedCount() == 1) {
            try {
                return Response
                        .status(Response.Status.OK)
                        .entity(usedQuoteCollection.find(eq("_id", objectId)).first().toJson())
                        .build();
            } catch (NullPointerException e) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(new Document("error", "Used Quote Not found!").toJson())
                        .build();
            }
        } else {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(new Document("error", "Used Quote Not found!").toJson())
                    .build();
        }
    }


}
