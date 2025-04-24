package com.accounts;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MongoUtil {

    private static MongoClient client;

    @PostConstruct
    public void init() {
        System.out.println(">>>> Creating Mongo Connection");
        client = MongoClients.create(getConnectionString());
    }

    private static String getConnectionString() {
        return System.getenv("CONNECTION_STRING");
    }

    public MongoClient getMongoClient(){
        return client;
    }
}
