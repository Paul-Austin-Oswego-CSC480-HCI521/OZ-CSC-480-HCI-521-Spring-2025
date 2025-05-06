package com.notifications;

import com.accounts.AccountService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class NotificationServiceTest {
    static NotificationService notificationService;
    static AccountService accountService;

    @Container
    private static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:6.0");

    @BeforeAll
    public static void setUp() {

//        Dotenv dotenv = Dotenv.configure()
//                .load();

        String connectionString = mongoDBContainer.getConnectionString();

//        notificationService = new NotificationService(dotenv.get("CONNECTION_STRING"));

        notificationService = new NotificationService(connectionString);

        Document notificationDoc = new Document()
                .append("from", new ObjectId("67e4adce8ac6f033eefeb213"))
                .append("to", new ObjectId("67ec3b1f05ab413c177608a3"))
                .append("type", "Share")
                .append("quote_id", new ObjectId("67e6bb0f629c75688b150e53"))
                .append("Created_at", System.currentTimeMillis());

        notificationService.getNotificationsCollection().insertOne(notificationDoc);

        MongoClient client = MongoClients.create(connectionString);
        accountService = new AccountService(client, "Test", "Users");

    }

    // Test if ObjectId is valid
    @Test
    @Order(1)
    void isValidObjectIdTC1() {
        boolean valid = notificationService.isValidObjectId("1234");
        assertFalse(valid);
    }

    @Test
    @Order(1)
    void isValidObjectIdTC2() {
        boolean valid = notificationService.isValidObjectId("jhdkafhf");
        assertFalse(valid);
    }

    @Test
    @Order(1)
    void isValidObjectIdTC3() {
        boolean valid = notificationService.isValidObjectId("aaaaaaaaaaaaaaaaaaaaaaaa");
        assertTrue(valid);
    }

    @Test
    @Order(1)
    void isValidObjectIdTC4() {
        boolean valid = notificationService.isValidObjectId(null);
        assertFalse(valid);
    }

    @Test
    @Order(1)
    void isValidObjectIdTC5() {
        boolean valid = notificationService.isValidObjectId(new ObjectId().toString());
        assertTrue(valid);
    }

    @Test
    @Order(1)
    void isValidObjectIdTC6() {
        boolean valid = notificationService.isValidObjectId("507f1f77bcf86cd799439011");
        assertTrue(valid);
    }

    @Test
    @Order(1)
    void isValidObjectIdTC7() {
        boolean valid = notificationService.isValidObjectId("");
        assertFalse(valid);
    }

    @Test
    @Order(1)
    void isValidObjectIdTC8() {
        boolean valid = notificationService.isValidObjectId("507f1f77b cf86cd799439011");
        assertFalse(valid);
    }

    //Test get user notifications
    @Test
    @Order(1)
    void getNotificationsByUserTC1() {
        String notifications = notificationService.getNotificationsByUser(new ObjectId());
        assertEquals("[]", notifications);
    }

    @Test
    @Order(1)
    void getNotificationsByUserTC2() {
        String notifications = notificationService.getNotificationsByUser(null);
        assertEquals("[]", notifications);
    }

    @Test
    @Order(1)
    void getNotificationsByUserTC3() {
        String notifications = notificationService.getNotificationsByUser(new ObjectId("aaaaaaaaaaaaaaaaaaaaaaaa"));
        assertEquals("[]", notifications);
    }

    @Test
    @Order(1)
    void getNotificationsByUserTC4() {
        String notifications = notificationService.getNotificationsByUser(new ObjectId("782393872983470129798237"));
        assertEquals("[]", notifications);
    }

    @Test
    @Order(2)
    void getNotificationsByUserTC5() {
        String notifications = notificationService.getNotificationsByUser(new ObjectId("67ec3b1f05ab413c177608a3"));
        List<Document> docs = Document.parse("{\"array\":" + notifications + "}").getList("array", Document.class);
        Document doc = docs.get(0);
        assertEquals("67e4adce8ac6f033eefeb213", doc.getString("from"));
        assertEquals("67ec3b1f05ab413c177608a3", doc.getString("to"));
        assertEquals("Share", doc.getString("type"));
        assertEquals("67e6bb0f629c75688b150e53", doc.getString("quote_id"));
    }

    @Test
    @Order(3)
    void addNotificationForTest() {
        Document notificationDoc = new Document()
                .append("from", new ObjectId("67e4adce8ac6f033eefeb213"))
                .append("to", new ObjectId("67ec3b1f05ab413c177608a3"))
                .append("type", "Share")
                .append("quote_id", new ObjectId("67e6ba7c629c75688b150e50"))
                .append("Created_at", System.currentTimeMillis());

        notificationService.getNotificationsCollection().insertOne(notificationDoc);
    }

    @Test
    @Order(4)
    void getNotificationsByUserTC6() {
        String notifications = notificationService.getNotificationsByUser(new ObjectId("67ec3b1f05ab413c177608a3"));
        List<Document> docs = Document.parse("{\"array\":" + notifications + "}").getList("array", Document.class);
        Document doc1 = docs.get(0);
        assertEquals("67e4adce8ac6f033eefeb213", doc1.getString("from"));
        assertEquals("67ec3b1f05ab413c177608a3", doc1.getString("to"));
        assertEquals("Share", doc1.getString("type"));
        assertEquals("67e6bb0f629c75688b150e53", doc1.getString("quote_id"));

        Document doc2 = docs.get(1);
        assertEquals("67e4adce8ac6f033eefeb213", doc2.getString("from"));
        assertEquals("67ec3b1f05ab413c177608a3", doc2.getString("to"));
        assertEquals("Share", doc2.getString("type"));
        assertEquals("67e6ba7c629c75688b150e50", doc2.getString("quote_id"));
    }

    // delete test notifications at the end
    @Test
    @Order(5)
    void deleteNotifications(){
        ObjectId objectId = new ObjectId("67ec3b1f05ab413c177608a3");
        Document filter = new Document("to", objectId);
        notificationService.getNotificationsCollection().deleteMany(filter).getDeletedCount();
    }

}