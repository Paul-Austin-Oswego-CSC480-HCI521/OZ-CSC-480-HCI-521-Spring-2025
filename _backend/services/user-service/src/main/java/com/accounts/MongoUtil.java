package com.accounts;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class MongoUtil {

    @Produces
    @ApplicationScoped
    public MongoClient createMongoClient() {
        System.out.println(">>>> Creating Mongo Connection");
        return MongoClients.create(getConnectionString());
    }

    public void closeMongoClient(@Disposes MongoClient client) {
        System.out.println(">>>> Closing Mongo Connection");
        client.close();
    }

    private static String getConnectionString() {
        return System.getenv("CONNECTION_STRING");
    }
}
