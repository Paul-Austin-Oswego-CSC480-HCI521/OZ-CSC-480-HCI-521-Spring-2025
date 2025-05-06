package com.quotes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.StringWriter;
import java.util.*;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
public class MongoUtil {

    @Produces
    @ApplicationScoped
    public MongoClient produceMongoClient() {
        System.out.println(">>>> Creating Mongo Connection");
        return MongoClients.create(getConnectionString());
    }

//    @Produces
//    @ApplicationScoped
//    public MongoDatabase produceMongoDatabase(MongoClient client) {
//        return client.getDatabase("Data");
//    }

    public void disposeMongoClient(@Disposes MongoClient client) {
        System.out.println(">>>> Closing Mongo Connection");
        client.close();
    }

    private static String getConnectionString() {
        return System.getenv("CONNECTION_STRING");
    }
}
