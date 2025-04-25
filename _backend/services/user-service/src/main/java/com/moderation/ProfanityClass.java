package com.moderation;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class ProfanityClass {

    private final HttpClient client = HttpClient.newHttpClient();
    private static final URI PROFANITY_API = URI.create("https://vector.profanity.dev");

    public boolean checkProfanity(String message) {
        try {
            // Build the JSON body
            String jsonBody = "{\"message\":\"" + message.replace("\"", "\\\"") + "\"}";

            // Fire the HTTP request
            HttpRequest request = HttpRequest.newBuilder(PROFANITY_API)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            // Parse the JSON response
            try (JsonReader jr = Json.createReader(new StringReader(body))) {
                JsonObject json = jr.readObject();
                // if the API ever changes the field name, supply a default of false:
                return json.getBoolean("isProfanity", false);
            }

        } catch (Exception e) {

            e.printStackTrace();
            // fallback if anything goes wrong:
            return false;
        }
    }
}