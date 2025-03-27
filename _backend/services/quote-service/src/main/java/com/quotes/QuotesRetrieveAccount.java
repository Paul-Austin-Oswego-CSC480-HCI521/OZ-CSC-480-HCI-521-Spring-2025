package com.quotes;

import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Projections;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;


public class QuotesRetrieveAccount {

    public static Map<String, String> retrieveJWTData(String jwtString) {
        try {
            JwtConsumer consumer = JwtConsumer.create("defaultJwtConsumer");
            JwtToken jwt = consumer.createJwt(jwtString);

            ArrayList<String> groups = (ArrayList<String>) jwt.getClaims().get("groups");

            return Map.of("subject", jwt.getClaims().getSubject(),"group", groups.get(0));
        } catch (InvalidConsumerException | InvalidTokenException e) {
            System.out.println(e);
            return null;
        }
    }
}