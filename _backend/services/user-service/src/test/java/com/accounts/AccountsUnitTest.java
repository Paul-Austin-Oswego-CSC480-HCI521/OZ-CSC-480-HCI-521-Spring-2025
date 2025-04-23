package com.accounts;

import com.ibm.websphere.security.jwt.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.sharedQuotes.SharedQuote;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.spec.SecretKeySpec;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class AccountsUnitTest {
    static AccountService accountService;
    static Account account;
    static String jwt;
    static ObjectId id;

    @Container
    private static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:6.0");

    // Setup Method
    @BeforeAll
    public static void setUp() throws UnknownHostException {
//        Dotenv dotenv = Dotenv.configure()
//                .load();
//
//        MongoClient client = MongoClients.create(dotenv.get("CONNECTION_STRING"));
//

        String connectionString = mongoDBContainer.getConnectionString();

        MongoClient client = MongoClients.create(connectionString);

        accountService = new AccountService(client, "Test", "Users");

//        byte[] keyBytes = "secret".getBytes(StandardCharsets.UTF_8);
//
//        if (keyBytes.length < 32) {
//            byte[] paddedKey = new byte[32];
//            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
//            keyBytes = paddedKey;
//        }
//
//        Key signingKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
//
//        jwt = Jwts.builder()
//                .claim("token_type", "Bearer")
//                .claim(Claims.SUBJECT, "362846238")
//                .claim("groups", new String[]{"admin"})
//                .setIssuer("quotable")
//                .setExpiration(new Date(System.currentTimeMillis() + 300000))
//                .setIssuedAt(new Date())
//                .setHeaderParam("typ", "JWT")
//                .signWith(signingKey)
//                .compact();
//
//        System.out.println(jwt);
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

        List<String> bookmarkedQuotes = new ArrayList<>();
        bookmarkedQuotes.add("sample_id_1");
        bookmarkedQuotes.add("sample_id_2");

        List<SharedQuote> sharedQuotes = new ArrayList<>();
        sharedQuotes.add(new SharedQuote());

        Map<String, String> usedQuotes = new HashMap<>();
        usedQuotes.put("sample_id_1", "sample_id_2");

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
                bookmarkedQuotes,
                sharedQuotes,
                "Tester",
                "sample_quote_id",
                usedQuotes
        );
    }

//    @Test
//    void retrieveUserFromJWTTC1() {
//        Document doc = accountService.retrieveUserFromJWT(jwt);
//    }

    // TEST CASES

    // POST Create Account
    // Create account (success) code 200
    // Create account (invalid JSON) code 400
    // Create account (email already exists) 409
    @Test
    @Order(1)
    void newUserTC1() {
        try(Response response = accountService.newUser(account.toJson())) {
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    @Order(2)
    void newUserTC2() {
        String jsonAccountMissingBracket = account.toJson().substring(0, account.toJson().length() - 1);
        try(Response response = accountService.newUser(jsonAccountMissingBracket)) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    @Order(3)
    void newUserTC3() {
        try(Response response = accountService.newUser("{}")) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    @Order(4)
    void newUserTC4() {
        try(Response response = accountService.newUser(account.toJson())) {
            assertEquals(409, response.getStatus());
        }
    }

    @Test
    @Order(5)
    void newUserTC5() {
        try(Response response = accountService.newUser(null)) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    @Order(6)
    void newUserTC6() {
        Document doc = new Document("Email", "");
        try(Response response = accountService.newUser(doc.toJson())) {
            assertEquals(400, response.getStatus());
        }
    }

    // GET Search account by email
    // Search account (success) code 200
    // Search account (failure) code 404
    @Test
    @Order(7)
    void retrieveUserByEmailTC1() {
        Document d;
        try(Response response = accountService.retrieveUserByEmail("example@gmail.com", false)) {
            assertEquals(200, response.getStatus());
            d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
        }
        id = d.getObjectId("_id");
    }

    @Test
    @Order(8)
    void retrieveUserByEmailTC2() {
        try(Response response = accountService.retrieveUserByEmail("example@gmail.com", true)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
            assertEquals(account.access_token, d.getString("access_token"));
        }
    }

    @Test
    @Order(9)
    void retrieveUserByEmailTC3() {
        try(Response response = accountService.retrieveUserByEmail("example@oswego.edu", false)) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(10)
    void retrieveUserByEmailTC4() {
        try(Response response = accountService.retrieveUserByEmail("example@oswego.edu", true)) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(11)
    void retrieveUserByEmailTC5() {
        try(Response response = accountService.retrieveUserByEmail(null, true)) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(12)
    void retrieveUserByEmailTC6() {
        try(Response response = accountService.retrieveUserByEmail("", true)) {
            assertEquals(404, response.getStatus());
        }
    }

    // GET Search account by ID
    // Search account (success) code 200
    // Search account (failure) code 404
    @Test
    @Order(13)
    void retrieveUserTC1() {
        try(Response response = accountService.retrieveUser(id.toString(), false)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
        }
    }

    @Test
    @Order(14)
    void retrieveUserTC2() {
        try(Response response = accountService.retrieveUser(id.toString(), true)) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("John Doe", d.getString("Username"));
            assertEquals(account.access_token, d.getString("access_token"));
        }
    }

    @Test
    @Order(15)
    void retrieveUserTC3() {
        try(Response response = accountService.retrieveUser("512753721", false)) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(16)
    void retrieveUserTC4() {
        try(Response response = accountService.retrieveUser("aaaaaaaaaaaaaaaaaaaaaaaa", false)) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(17)
    void retrieveUserTC5() {
        try(Response response = accountService.retrieveUser(null, false)) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(18)
    void retrieveUserTC6() {
        try(Response response = accountService.retrieveUser("", false)) {
            assertEquals(404, response.getStatus());
        }
    }

    // Update account by ID and accountJson
    // Update (success) code 200
    // Update (invalid JSON) code 400
    // Update (account not found) code 404
    @Test
    @Order(19)
    void updateUserTC1() {
        try (Response response = accountService.updateUser(account.toJson(), id.toString())) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(20)
    void updateUserTC2() {
        account.Profession = "Testing Manager";
        try(Response response = accountService.updateUser(account.toJson(), id.toString())) {
            assertEquals(200, response.getStatus());
            Document d = Document.parse(response.getEntity().toString());
            assertEquals("Testing Manager", d.getString("Profession"));
        }
    }

    @Test
    @Order(21)
    void updateUserTC3() {
        try(Response response = accountService.updateUser(account.toJson().substring(0, account.toJson().length() - 1), id.toString())) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    @Order(22)
    void updateUserTC4() {
        try(Response response = accountService.updateUser(account.toJson(), "134643343")) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(23)
    void updateUserTC5() {
        try(Response response = accountService.updateUser(account.toJson(), "aaaaaaaaaaaaaaaaaaaaaaaa")) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(24)
    void updateUserTC6() {
        try(Response response = accountService.updateUser(account.toJson(), null)) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(25)
    void updateUserTC7() {
        try(Response response = accountService.updateUser(null, id.toString())) {
            assertEquals(400, response.getStatus());
        }
    }

    @Test
    @Order(25)
    void updateUserTC8() {
        try(Response response = accountService.updateUser(account.toJson(), new ObjectId().toString())) {
            assertEquals(404, response.getStatus());
        }
    }

    // DELETE Delete account by ID
    // Delete (success) code 200
    // Delete (account not found) code 404
    @Test
    @Order(26)
    void deleteUserTC1() {
        try(Response response = accountService.deleteUser("2165217521757")) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(26)
    void deleteUserTC2() {
        try(Response response = accountService.deleteUser("aaaaaaaaaaaaaaaaaaaaaaaa")) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(26)
    void deleteUserTC3() {
        try(Response response = accountService.deleteUser(new ObjectId().toString())) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(26)
    void deleteUserTC4() {
        try(Response response = accountService.deleteUser(null)) {
            assertEquals(404, response.getStatus());
        }
    }

    @Test
    @Order(29)
    void deleteUserTC5() {
        try(Response response = accountService.deleteUser(id.toString())) {
            assertEquals(200, response.getStatus());
        }
    }

    // Account to document
    @Test
    void account_to_documentTC1() {
        Document doc = accountService.account_to_document(account);
        assertEquals("example@gmail.com", doc.getString("Email"));
        assertEquals("John Doe", doc.getString("Username"));
        assertEquals(0, doc.getInteger("admin"));
        assertEquals("sample_access_token", doc.getString("access_token"));
        assertEquals("sample_refresh_token", doc.getString("refresh_token"));
        assertEquals("Tester", doc.getString("Profession"));
    }

    // Document to Account
    @Test
    void document_to_accountTC1() {
        Account account1 = accountService.document_to_account(accountService.account_to_document(account));
        assertEquals(account.toJson(), account1.toJson());
    }

}