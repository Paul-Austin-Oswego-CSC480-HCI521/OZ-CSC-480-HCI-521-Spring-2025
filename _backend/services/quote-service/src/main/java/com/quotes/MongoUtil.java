package com.quotes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.StringWriter;
import java.util.Arrays;

@ApplicationScoped
public class MongoUtil {

    private static final String DATABASE_NAME = "Data";
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    static {
        mongoClient = MongoClients.create(getConnectionString());
        database = mongoClient.getDatabase(DATABASE_NAME);
    }

    public static String getConnectionString() {
        return System.getenv("CONNECTION_STRING");
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public String getQuote(ObjectId quoteID) {
        MongoCollection<Document> collection = database.getCollection("Quotes");
        Document query = new Document("_id", quoteID);
        Document result = collection.find(query).first();

        //return (result != null) ? result.toJson() : null;
        if(result != null) {
            result.put("_id", result.getObjectId("_id").toString()); //gets rid of "$oid" field
            return result.toJson();
        }
        return null;
    }

    public String searchQuote(String searchQuery) {
        MongoCollection<Document> collection = database.getCollection("Quotes");

        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                new Document("$search", new Document("index", "QuotesAtlasSearch")
                        .append("text", new Document("query", searchQuery)
                                .append("path", Arrays.asList("quote", "author"))
                                .append("fuzzy", new Document("maxEdits", 2))
                        )
                ),
                new Document("$sort", new Document("score", -1)), //sort by relevance
                new Document("$limit", 10)
        ));

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for(Document doc : results) {
            JsonObject jsonObject = Json.createReader(new java.io.StringReader(doc.toJson())).readObject();
            jsonArrayBuilder.add(jsonObject);
        }

        StringWriter stringWriter = new StringWriter();
        try(JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            jsonWriter.writeArray(jsonArrayBuilder.build());
        }
        return stringWriter.toString();
    }

    public void searchByAuthor(String authorQuery) {
        MongoCollection<Document> collection = database.getCollection("Quotes");
        //might not need
    }

    private QuoteObject parseQuote(String jsonQuote) {
        //parses json from string form to Java Object
        System.out.println(jsonQuote);
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readValue(jsonQuote, QuoteObject.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateQuote(QuoteObject quote) {
        MongoCollection<Document> collection = database.getCollection("Quotes");

        //get current quote for reference
        String originalQuote = getQuote(quote.getId());
        QuoteObject ogQuote = parseQuote(originalQuote);

        if(ogQuote == null) {
            System.out.println("Og Quote null for some reason");
            return false;
        }

        quote.PrintQuote();
        if(quote.getId() == null) {
            System.out.print("Missing ID");
            return false;
        }
        System.out.print("Has ID");

        try {
            Document IdQuery = new Document();
            IdQuery.append("_id", quote.getId());

            //document containing data to update
            Document newData = new Document();

            //checking and appending data
            //look into automatic ways but this works well
            if(quote.getAuthor() != null && !quote.getAuthor().isEmpty()) {
                //if field is not null, or is not empty, append to document to be updated
                newData.append("author", quote.getAuthor());
            }
            if(quote.getText() != null && !quote.getText().isEmpty()) {
                //if field is not null, or is not empty, append to document to be updated
                newData.append("quote", quote.getText());
            }
            if(quote.getBookmarks() >= 0 && Math.abs(ogQuote.getBookmarks() - quote.getBookmarks()) <= 1) {
                //if bookmarks = -1, then field is null. Also check if updated value is within 1 integer delta
                //Reasoning is that the value changing by more than 1 doesn't make sense so that operation shouldn't be allowed
                newData.append("bookmarks", quote.getBookmarks());
            }
            if(quote.getShares() >= 0 && Math.abs(ogQuote.getShares() - quote.getShares()) <= 1) {
                //if bookmarks = -1, then field is null. Also check if updated value is within 1 integer delta
                //Reasoning is that the value changing by more than 1 doesn't make sense so that operation shouldn't be allowed
                newData.append("shares", quote.getShares());
            }
            if(quote.getDate() >= 0) {
                //Do we even need a way to update the date?
                newData.append("date", quote.getDate());
            }
            if(quote.getTags() != null) {
                //Don't think any other checks for tags are needed
                newData.append("tags", quote.getTags());
            }
            if(quote.getFlags() >=0 && Math.abs(ogQuote.getFlags() - quote.getFlags()) <= 1) {
                //same as above, shouldn't change more than 1 per update.
                newData.append("flags", quote.getFlags());
            }


            //create update operation
            Document updateOperation = new Document("$set", newData);

            long modifiedCount = collection.updateOne(IdQuery, updateOperation).getModifiedCount();
            return modifiedCount > 0;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteQuote(ObjectId quoteId) {
        MongoCollection<Document> collection = database.getCollection("Quotes");
        try {
            Document idQuery = new Document();
            idQuery.append("_id", quoteId);

            long deletedCount = collection.deleteOne(idQuery).getDeletedCount();
            return deletedCount > 0;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ObjectId createQuote(QuoteObject quoteData) {
        try{
            MongoCollection<Document> collection = database.getCollection("Quotes");
            //give quote a new id
            quoteData.setId(new ObjectId());

            if(quoteData.getText().isEmpty()) {
                return null;
            }

            //create document to insert
            Document quoteDoc = new Document()
                    .append("_id", quoteData.getId())
                    .append("author", quoteData.getAuthor())
                    .append("quote", quoteData.getText())
                    .append("bookmarks", 0)
                    .append("shares", 0)
                    .append("date", quoteData.getDate())
                    .append("tags", quoteData.getTags())
                    .append("flags", 0);

            collection.insertOne(quoteDoc);
            return quoteData.getId();
        } catch (Exception e) {
            System.out.println("Exception in MongoUtil/createQuote: "+e);
            return null;
        }
    }

    public String getTopBookmarked(){
        MongoCollection<Document> collection = database.getCollection("Quotes");

        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                new Document("$sort", new Document("bookmarks", -1)), new Document("$limit", 5)));

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for(Document doc : results) {
            JsonObject jsonObject = Json.createReader(new java.io.StringReader(doc.toJson())).readObject();
            jsonArrayBuilder.add(jsonObject);
        }

        StringWriter stringWriter = new StringWriter();
        try(JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            jsonWriter.writeArray(jsonArrayBuilder.build());
        }
        return stringWriter.toString();
    }

    public String getTopShared() {
        MongoCollection<Document> collection = database.getCollection("Quotes");

        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                new Document("$sort", new Document("shares", -1)), new Document("$limit", 5)));

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for(Document doc : results) {
            JsonObject jsonObject = Json.createReader(new java.io.StringReader(doc.toJson())).readObject();
            jsonArrayBuilder.add(jsonObject);
        }

        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            jsonWriter.writeArray(jsonArrayBuilder.build());
        }
        return stringWriter.toString();
    }

    public static void close() {
        if(mongoClient != null) {
            mongoClient.close();
        }
    }
}
