package com.quotes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonWriter;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.StringWriter;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
public class QuoteService {

    @Inject
    private MongoClient client;

    private MongoDatabase database;

    public QuoteService() {}

    @PostConstruct
    public void init() {
        database = client.getDatabase("Data");
    }

    public QuoteService(MongoClient mongoClient, String databaseName) {
        client = mongoClient;
        database = client.getDatabase(databaseName);
    }

    private Document retrieveUserFromJWT(String jwtString) {
        try {
            MongoDatabase UserDatabase = client.getDatabase("Accounts");
            MongoCollection<Document> userCollection = UserDatabase.getCollection("Users");
            JwtConsumer consumer = JwtConsumer.create("defaultJwtConsumer");
            JwtToken jwt = consumer.createJwt(jwtString);

            String id = jwt.getClaims().getSubject();

            ObjectId objectId;
            try {
                objectId = new ObjectId(id);
            } catch (Exception e) {
                return null;
            }

            return userCollection.find(eq("_id", objectId)).first();
        } catch (InvalidConsumerException | InvalidTokenException e) {
            System.out.println(e);
            return null;
        }
    }

    public String getQuote(ObjectId quoteID) {
        MongoCollection<Document> collection = database.getCollection("Quotes");
        Document query = new Document("_id", quoteID);
        Document result = collection.find(query).first();

        if(result != null) {
            result.put("_id", result.getObjectId("_id").toString()); //gets rid of "$oid" subfield
            ObjectId creator = result.getObjectId("creator");
            if(creator == null) {
                System.out.println("creator is null");
                return null;
            }
            result.put("creator", result.getObjectId("creator").toString());
            return result.toJson();
        }
        return null;
    }

    private List<String> fetchUserUsedQuoteIds(Document account) {
        try{
            //extract list from account
            Object rawObject = account.get("UsedQuotes"); //get data as raw object
            if(!(rawObject instanceof List<?> rawList)) { //check if UsedQuotes field returned as list
                //raw data is not a list, return empty list
                return Collections.emptyList();
            }

            return rawList.stream() //return list of UsedQuotes map keys
                    .filter(obj -> obj instanceof Map) //ensure object in list is a Map
                    .flatMap(obj -> ((Map<?, ?>) obj).keySet().stream()) //extract keys from Map
                    .filter(key -> key instanceof String) //ensure all keys are strings
                    .map(key -> (String) key) // cast key to string
                    .toList(); //Convert to list to return as

        } catch(ClassCastException e) {
            //return empty list
            return Collections.emptyList();
        }
    }

    private List<ObjectId> fetchUserBookmarkedQuotes(Document account) {
        try {
            List<String> list = account.getList("BookmarkedQuotes", String.class);
            return list.stream().map(ObjectId::new).toList();
        } catch (Exception e) {
            System.out.println("Failed getting users bookmarked quotes");
            return Collections.emptyList();
        }
    }

    private List<ObjectId> fetchUserUploadedQuotes(Document account) {
        try {
            List<String> list = account.getList("MyQuotes", String.class);
            return list.stream().map(ObjectId::new).toList();
        } catch (Exception e) {
            System.out.println("Failed getting users uploaded quotes");
            return Collections.emptyList();
        }
    }

    public String searchQuote(String searchQuery, boolean filterUsed, boolean filterBookmarked, boolean filterUploaded,
                              String IncludeTerms, String ExcludeTerms, String jwtString, boolean isGuest) { // fuzzy search for quote

        MongoCollection<Document> collection = database.getCollection("Quotes");

        //certain filter fields only available to user
        List<ObjectId> filterQuoteIds = new ArrayList<>(); //instantiate list of quote id's to compare to
        Document userDoc = new Document();
        String userId = null;
        if(!isGuest) {
            userDoc = retrieveUserFromJWT(jwtString); //Get User Document if not guest
            if(userDoc == null) {
                return null;
            }
            userId = userDoc.getObjectId("_id").toHexString();
        }

        if(filterUsed && !isGuest) { //if filter true, add quote ids to filter list
            List<String> temp = fetchUserUsedQuoteIds(userDoc);
            filterQuoteIds.addAll(temp.stream().map(ObjectId::new).toList()); // append list containing used quote id's
        }
        if(filterBookmarked && !isGuest) {
            filterQuoteIds.addAll(fetchUserBookmarkedQuotes(userDoc));
        }
        if(filterUploaded && !isGuest) {
            filterQuoteIds.addAll(fetchUserUploadedQuotes(userDoc));
        }

        //System.out.println(filterQuoteIds);

        //following fields will still work for guest
        List<String> TermsToInclude = new ArrayList<>();
        List<Document> MustClause = new ArrayList<>();
        if(IncludeTerms != null) {
            TermsToInclude = Arrays.stream(IncludeTerms.split(",")).toList(); //split terms into list
            //create clause specifying quote must should include specified terms
            MustClause.add(new Document("text", new Document("query", TermsToInclude).append("path", "quote")));
        }

        List<String> TermsToExclude = new ArrayList<>();
        List<Document> MustNotClause = new ArrayList<>();
        if(ExcludeTerms != null) {
            TermsToExclude = Arrays.stream(ExcludeTerms.split(",")).toList(); //split terms into list
            //create clause specifying quote must not include specified terms
            MustNotClause.add(new Document("text", new Document("query", TermsToExclude).append("path", "quote")));
        }

        //Base clause
        List<Document> ShouldClause = List.of( //search that "must" occur
                new Document("text", new Document("query", searchQuery) //set query string to user query
                        .append("path", Arrays.asList("quote", "author", "tags"))// field to search and compare to
                        .append("fuzzy", new Document("maxEdits", 2))
                ),
                new Document("text", new Document("query", searchQuery) //set query string to user query
                        .append("path", "author")// field to search and compare to
                        .append("score", new Document("boost", new Document("value", 1.5)))
                        .append("fuzzy", new Document("maxEdits", 2))
                ),
                new Document("text", new Document("query", searchQuery) //set query string to user query
                        .append("path", "tags")// field to search and compare to
                        .append("score", new Document("boost", new Document("value", 1.5)))
                        .append("fuzzy", new Document("maxEdits", 2))
                )
        );

        //build search query document
        //The should/mustNot can cause search issues if lists are empty, so they must be dynamically appended to query document
        Document CompoundDoc = new Document("should", ShouldClause); //default searching
        //append include/exclude clauses if specified
        if(!TermsToInclude.isEmpty()) {
            CompoundDoc.append("must", MustClause).append("minimumShouldMatch", 1); //Min num of elements the quote text must include
        }
        if(!TermsToExclude.isEmpty()) {
            CompoundDoc.append("mustNot", MustNotClause);
        }

        // create query document
        AggregateIterable<Document> results;
        if(isGuest) { //query if is guest
            results = collection.aggregate(Arrays.asList(
                    new Document("$search", new Document("index", "QuotesAtlasSearch") //set to search atlas index
                            .append("compound", CompoundDoc)),
                    //post search section
                    new Document("$match", new Document("private", new Document("$ne", true))), //Exclude private quotes
                    new Document("$match", new Document("_id", new Document("$nin", filterQuoteIds))), //Ignore specified quotes
                    new Document("$sort", new Document("score", -1)), //sort by relevance
                    new Document("$limit", 50) //limit to 50 results
            ));
        } else { //query if is user
            results = collection.aggregate(Arrays.asList(
                    new Document("$search", new Document("index", "QuotesAtlasSearch") //set to search atlas index
                            .append("compound", CompoundDoc)),
                    //post search section
                    new Document("$match", new Document("$or", List.of(
                            new Document("private", new Document("$ne", true)), //exclude private quotes
                            new Document("$expr", new Document("$eq", List.of("$creator", userId))) //unless is creator
                    ))),
                    new Document("$match", new Document("_id", new Document("$nin", filterQuoteIds))), //Ignore specified quotes
                    new Document("$sort", new Document("score", -1)), //sort by relevance
                    new Document("$limit", 50) //limit to 50 results
            ));
        }

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

        if(quote == null) {
            System.out.println("Quote object is null");
            return false;
        }

        //get current quote for reference
        String originalQuote = getQuote(quote.getId());
        if(originalQuote == null) {
            System.out.println("Quote id is null or invalid");
            return false;
        }
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


}
