package com.quotes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        if(result != null) {
            result.put("_id", result.getObjectId("_id").toString()); //gets rid of "$oid" subfield
            result.put("creator", result.getObjectId("creator").toString());
            return result.toJson();
        }
        return null;
    }

    public String searchQuote(String searchQuery) { // fuzzy search for quote
        MongoCollection<Document> collection = database.getCollection("Quotes");

        // create query document
        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                new Document("$search", new Document("index", "QuotesAtlasSearch") //set to search atlas index
                        .append("text", new Document("query", searchQuery) //set query string to user query
                                .append("path", Arrays.asList("quote", "author", "tags")) // fields to search and compare to
                                .append("fuzzy", new Document("maxEdits", 2))
                        )
                ),
                new Document("$match", new Document("private", new Document("$ne", true))), //Exclude private quotes
                new Document("$sort", new Document("score", -1)), //sort by relevance
                new Document("$limit", 50) //limit to 50 results
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

        //quote.PrintQuote();
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
            if(quote.getisPrivate() != ogQuote.getisPrivate()) {
                // can't check for "private" field being null. So only update if value is different
                newData.append("private", quote.getisPrivate());
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

    public boolean updateAll() { //Adding new privacy and creator fields to all existing documents
        // not for actual production use
        try {
            MongoCollection<Document> collection = database.getCollection("Quotes");

            Document newFields = new Document().append("creator", null); //fields to be added
            Document updateOperation = new Document("$set", newFields);

            collection.updateMany(new Document(), updateOperation);

            return true;
        } catch (Exception e) {
            System.out.print(e);
            return false;
        }
    }

    public boolean deleteQuote(ObjectId quoteId) {
        MongoCollection<Document> collection = database.getCollection("Quotes");
        try {
            //create query document with id
            Document idQuery = new Document();
            idQuery.append("_id", quoteId);

            //delete quote, returns greater than 0 if successful
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

            if(quoteData.getText().isEmpty()) { //make sure quote has text
                return null;
            }
            //calculate unix time code
            long unixTime = System.currentTimeMillis() / 1000L;
            //create document to insert
            Document quoteDoc = new Document()
                    .append("_id", quoteData.getId())
                    .append("author", quoteData.getAuthor())
                    .append("quote", quoteData.getText())
                    .append("bookmarks", 0) // default to 0
                    .append("shares", 0) // default to 0
                    .append("date", unixTime)
                    .append("tags", quoteData.getTags())
                    .append("flags", 0) // default to 0
                    .append("private", quoteData.getisPrivate())
                    .append("creator", quoteData.getCreator());

            collection.insertOne(quoteDoc); //insert into database
            return quoteData.getId(); //return new quote id
        } catch (Exception e) {
            System.out.println("Exception in MongoUtil/createQuote: "+e);
            return null;
        }
    }

    public String getTopBookmarked(){ // get top 100 quotes with the most bookmarks
        MongoCollection<Document> collection = database.getCollection("Quotes");

        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("private", new Document("$ne", true))), // exclude private quotes
                new Document("$sort", new Document("bookmarks", -1)), // sort by bookmark field
                new Document("$limit", 100))); // limit to 100 results

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for(Document doc : results) {
            doc.put("_id", doc.getObjectId("_id").toString()); // gets rid of "$oid" subfield
            JsonObject jsonObject = Json.createReader(new java.io.StringReader(doc.toJson())).readObject();
            jsonArrayBuilder.add(jsonObject);
        }

        //put json array back into string form
        StringWriter stringWriter = new StringWriter();
        try(JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            jsonWriter.writeArray(jsonArrayBuilder.build());
        }
        return stringWriter.toString();
    }

    public String getTopShared() { // get top 100 most shared quotes
        MongoCollection<Document> collection = database.getCollection("Quotes");

        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("private", new Document("$ne", true))), // exclude private quotes
                new Document("$sort", new Document("shares", -1)), // sort by shares in descending order
                new Document("$limit", 100))); //limit to 100 results

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for(Document doc : results) {
            doc.put("_id", doc.getObjectId("_id").toString()); // gets rid of "$oid" subfield
            JsonObject jsonObject = Json.createReader(new java.io.StringReader(doc.toJson())).readObject();
            jsonArrayBuilder.add(jsonObject);
        }

        //put json array back into string form
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            jsonWriter.writeArray(jsonArrayBuilder.build());
        }
        return stringWriter.toString();
    }

    public String getMostRecent() { //gets the 100 most recently posted quotes
        MongoCollection<Document> collection = database.getCollection("Quotes");

        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("private", new Document("$ne", true))), // exclude private quotes
                new Document("$sort", new Document("date", -1)), // sort by date field in descending order
                new Document("$limit", 100))); // limit to 100

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for(Document doc : results) {
            doc.put("_id", doc.getObjectId("_id").toString());// gets rid of "$oid" subfield
            JsonObject jsonObject = Json.createReader(new java.io.StringReader(doc.toJson())).readObject();
            jsonArrayBuilder.add(jsonObject);
        }

        //put json array back into string form
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            jsonWriter.writeArray(jsonArrayBuilder.build());
        }
        return stringWriter.toString();
    }

    public String getTopFlagged() { // gets all quotes that meet a flag threshold
        MongoCollection<Document> collection = database.getCollection("Quotes");

        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                new Document("$match", new Document("private", new Document("$ne", true))), // Should it exclude private quotes?
                new Document("$match", new Document("flags", new Document("$gt", 2))), //get only quotes where flags >= 2
                new Document("$sort", new Document("flags", -1)) //sort in decending order
        ));

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for(Document doc : results) {
            doc.put("_id", doc.getObjectId("_id").toString());// gets rid of "$oid" subfield
            JsonObject jsonObject = Json.createReader(new java.io.StringReader(doc.toJson())).readObject();
            jsonArrayBuilder.add(jsonObject);
        }

        //put json array back into string form
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
            jsonWriter.writeArray(jsonArrayBuilder.build());
        }
        return stringWriter.toString();
    }

    public String getQuotesByUser(ObjectId userId) {
        MongoCollection<Document> collection = database.getCollection("Quotes");

        List<Document> quotes = collection.find(new Document("creator", userId)).into(new ArrayList<>());

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Document doc : quotes) {
            doc.put("_id", doc.getObjectId("_id").toString()); // gets rid of "$oid" subfield
            JsonObject jsonObject = Json.createReader(new java.io.StringReader(doc.toJson())).readObject();
            jsonArrayBuilder.add(jsonObject);
        }

        //put json array back into string form
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
