package com.accounts;

import jakarta.json.*;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import io.github.cdimascio.dotenv.Dotenv;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountsUnitTest {
    // Initialize mock account details
    static AccountService accountService;
    static Account account;

    // Setup Method
    @BeforeAll
    public static void setUp() {
//        Dotenv dotenv = Dotenv.configure()
//                .load();

        String connectionString = "mongodb://user:password@quotes-database:27017/?authSource=admin";

        accountService = new AccountService(connectionString, "Test", "Users");
    }

    @BeforeEach
    public void mockAccountObject() {
        List<String> scope = new ArrayList<>();
        scope.add("read");
        scope.add("write");

        List<String> notifications = new ArrayList<>();
        notifications.add("sample_id_1");
        notifications.add("sample_id_2");

        List<String> myQuotes = new ArrayList<>();
        myQuotes.add("sample_id_2");
        myQuotes.add("sample_id_2");

        Map<String, List<String>> favoriteQuotes = new HashMap<>();
        favoriteQuotes.put("FavoriteQuotes", List.of("sample_id_1", "sample_id_2"));

        List<String> sharedQuotes = new ArrayList<>();
        sharedQuotes.add("sample_id_1");
        sharedQuotes.add("sample_id_2");

        List<String> myTags = new ArrayList<>();
        myTags.add("sample_id_1");
        myTags.add("sample_id_2");

        account = new Account(
                "example@gmail.com",
                "John Doe",
                0,
                "sample_access_token",
                "sample_refresh_token",
                9999999999L,
                scope,
                "Bearer",
                notifications,
                myQuotes,
                favoriteQuotes,
                sharedQuotes,
                myTags,
                "Tester",
                "sample_quote_id"
        );
    }

    // TEST CASES

    // POST Create Account
    // Create account (success) code 200
    // Create account (invalid JSON) code 400
    // Create account (email already exists) 409
    @Test
    @Order(1)
    void newUserTC200() {
        try(Response response = accountService.newUser(account.toJson())) {
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    @Order(2)
    void newUserTC400() {
        String jsonAccountMissingBracket = account.toJson().substring(0, account.toJson().length() - 1);
        try(Response response = accountService.newUser(jsonAccountMissingBracket)) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    @Order(3)
    void newUserTC409() {
        try(Response response = accountService.newUser(account.toJson())) {
            assertEquals(409, response.getStatus());
        }
    }

    // GET Search account by email
    // Search account (success) code 200
    // Search account (failure) code 404
    @Test
    @Order(4)
    void retrieveUserByEmailTC200() {
        try(Response response = accountService.retrieveUserByEmail("example@gmail.com", false)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
        }
    }

    @Test
    @Order(5)
    void retrieveUserByEmailTC404() {
        try(Response response = accountService.retrieveUserByEmail("example@oswego.edu", false)) {
            assertEquals(404, response.getStatus());
        }
    }

    // GET Search account by ID
    // Search account (success) code 200
    // Search account (failure) code 404
    @Test
    @Order(6)
    void retrieveUserByEmailTC201() { // for some reason only works when it is named this
        String accountID;
        try(Response response = accountService.retrieveUserByEmail("example@gmail.com", false)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
            accountID = d.getObjectId("_id").toString();
        }
        try(Response response = accountService.retrieveUser(accountID, false)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
        }
    }

    @Test
    @Order(7)
    void retrieveUserTC404() {
        try(Response response = accountService.retrieveUser("512753721", false)) {
            assertEquals(404, response.getStatus());
        }
    }

    // Update account by ID and accountJson
    // Update (success) code 200
    // Update (invalid JSON) code 400
    // Update (account not found) code 404
    @Test
    @Order(8)
    void retrieveUserByEmailTC202() { // for some reason only works when it is named this
        String accountID;
        try(Response response = accountService.retrieveUserByEmail("example@gmail.com", false)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
            accountID = d.getObjectId("_id").toString();
        }
        account.Profession = "Testing Manager";
        try(Response response = accountService.updateUser(account.toJson(), accountID)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("Testing Manager", d.getString("Profession"));
        }
    }

    @Test
    @Order(9)
    void retrieveUserByEmailTC400() { // for some reason only works when it is named this
        String accountID;
        try(Response response = accountService.retrieveUserByEmail("example@gmail.com", false)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
            accountID = d.getObjectId("_id").toString();
        }
        try(Response response = accountService.updateUser(account.toJson().substring(0, account.toJson().length() - 1), accountID)) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    @Order(10)
    void updateUserTC404() {
        try(Response response = accountService.updateUser(account.toJson(), "134643343")) {
            assertEquals(404, response.getStatus());
        }
    }

    // DELETE Delete account by ID
    // Delete (success) code 200
    // Delete (account not found) code 404

    @Test
    @Order(11)
    void deleteUserTC404() {
        try(Response response = accountService.deleteUser("2165217521757")) {
            assertEquals(404, response.getStatus());
        }
    }

    @AfterAll
    static void retrieveUserByEmailTC203() { // for some reason only works when it is named this
        String accountID;
        try(Response response = accountService.retrieveUserByEmail("example@gmail.com", false)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
            accountID = d.getObjectId("_id").toString();
        }
        try(Response response = accountService.deleteUser(accountID)) {
            assertEquals(200, response.getStatus());
        }
    }

    // NOT CURRENTLY NEEDED:

    // POST User Bookmark quote (quoteID)
    // Quote bookmarked (success) code 200
    // Invalid request (failure) code 400
    // Unauthorized (failure) code 401

    // GET User Bookmarked quotes
    // Quotes retrieved (success) code 200
    // Quotes invalid request (failure) code 400

    // DELETE Bookmarked quote (quoteID)
    // Delete bookmark (success) code 200
    // Delete bookmark invalid (failure) code 400

}