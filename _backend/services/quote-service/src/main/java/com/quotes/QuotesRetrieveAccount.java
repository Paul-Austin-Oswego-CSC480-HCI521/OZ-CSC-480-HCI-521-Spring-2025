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

//    public static String retrieveGroups(HttpServletRequest request) {
//        System.out.println("cookies: " + request.getCookies());
//        Cookie jwtCookie = null;
//
//        if (request.getCookies() != null) {
//            jwtCookie = Arrays.stream(request.getCookies())
//                    .filter(c -> "jwt".equals(c.getName()))
//                    .findFirst()
//                    .orElse(null);
//        }
//
//        if (jwtCookie == null) {
//            return null;
//        }
//
//        try {
//            JwtConsumer consumer = JwtConsumer.create("defaultJwtConsumer");
//            JwtToken jwt = consumer.createJwt(jwtCookie.getValue());
//
//            ArrayList<String> groups = (ArrayList<String>) jwt.getClaims().get("groups");
//            return groups.get(0);
//        } catch (InvalidConsumerException | InvalidTokenException e) {
//            System.out.println(e);
//            return null;
//        }
//    }
//
//    public static String retrieveAccountID(HttpServletRequest request) {
//        System.out.println(request.getCookies());
//        Cookie jwtCookie = null;
//
//        if (request.getCookies() != null) {
//            jwtCookie = Arrays.stream(request.getCookies())
//                    .filter(c -> "jwt".equals(c.getName()))
//                    .findFirst()
//                    .orElse(null);
//        }
//
//        if (jwtCookie == null) {
//            return null;
//        }
//
//        try {
//            JwtConsumer consumer = JwtConsumer.create("defaultJwtConsumer");
//            JwtToken jwt = consumer.createJwt(jwtCookie.getValue());
//
//            return jwt.getClaims().getSubject();
//        } catch (InvalidConsumerException | InvalidTokenException e) {
//            System.out.println(e);
//            return null;
//        }
//    }

    public static Map<String, String> retrieveJWTData(HttpServletRequest request) {
        System.out.println(request.getCookies());
        Cookie jwtCookie = null;
   
        String jwtToken = null;
        jwtToken = request.getHeader("X-JWT-Token");


        if (request.getCookies() != null&&jwtToken ==null) {
            jwtCookie = Arrays.stream(request.getCookies())
                    .filter(c -> "jwt".equals(c.getName()))
                    .findFirst()
                    .orElse(null);
        }

        if (jwtCookie == null&&jwtToken==null) {
            return null;
        }

        try {
            JwtConsumer consumer = JwtConsumer.create("defaultJwtConsumer");
            JwtToken jwt = consumer.createJwt(jwtToken != null ? jwtToken : jwtCookie.getValue());

            ArrayList<String> groups = (ArrayList<String>) jwt.getClaims().get("groups");

            return Map.of("subject", jwt.getClaims().getSubject(),"group", groups.get(0));
        } catch (InvalidConsumerException | InvalidTokenException e) {
            System.out.println(e);
            return null;
        }
    }
}
