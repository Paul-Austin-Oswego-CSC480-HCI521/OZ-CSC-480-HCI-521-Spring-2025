package quotes;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.quotes.MongoUtil;
import com.quotes.QuoteObject;
import com.quotes.QuoteService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class QuoteServiceTesting {
    public static QuoteService quoteService;
    public static QuoteObject quoteObject;
    ObjectId id;
    public static ArrayList<ObjectId> ids;

    @Container
    private static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer("mongo:6.0");

    @BeforeAll
    public static void setUp() {
        ids = new ArrayList<>();
        String connectionString = mongoDBContainer.getConnectionString();
        MongoClient client = MongoClients.create(connectionString);

        quoteService = new QuoteService(client, "test");
    }

    @BeforeEach
    public void beforeEach() {
        ObjectId objectId = new ObjectId();
        ObjectId creatorId = new ObjectId();
        quoteObject = new QuoteObject();
        quoteObject.setId(objectId);
        quoteObject.setAuthor("author");
        quoteObject.setBookmarks(1);
        quoteObject.setShares(1);
        quoteObject.setTags(new ArrayList<>(Arrays.asList("tag1", "tag2")));
        quoteObject.setText("text");
        quoteObject.setCreator(creatorId);
        id = quoteService.createQuote(quoteObject);
        System.out.println(id);
        ids.add(id);
    }

    @AfterEach
    public void afterEach() {
        for (ObjectId id : ids) {
            quoteService.deleteQuote(id);
        }
    }

    @Test
    @Order(1)
    public void testCreateQuote() {
        assertEquals(1, ids.size());
    }


    @Test
    @Order(2)
    public void testCreateQuoteWithIdAndText(){
        quoteObject.setText("test");
        ids.add(quoteService.createQuote(quoteObject));
        assertEquals(3, ids.size());
    }


    //create Quote with empty text
    @Test
    @Order(3)
    public void testCreateQuoteWithEmptyText(){
        ObjectId objectId = new ObjectId();
        quoteObject.setId(objectId);
        quoteObject.setAuthor("author");
        quoteObject.setText("");
        quoteObject.setCreator(objectId);
        assertNull(quoteService.createQuote(quoteObject));
    }

    // just passing id and text


    //setting text null
    @Test
    @Order(4)
    public void testCreateQuoteWithTextNull(){
        quoteObject.setText(null);
        assertNull(quoteService.createQuote(quoteObject));
    }

    @Test
    @Order(5)
    public void testGetQuote(){
        assertNotNull(quoteService.getQuote(id));
    }


    //checking when the id is not present in the database
    @Test
    @Order(6)
    public void testGetQuoteswithRandomID(){
        assertNull(quoteService.getQuote(new ObjectId()));
    }

    //checking when the id is null
    @Test
    @Order(7)
    public void testGetQuoteswithNullID(){
        assertNull(quoteService.getQuote(null));
    }

    //checking when the creator is null
    //need to make sure the creator is not null
    @Test
    @Order(8)
    public void testGetQuoteswithNullCreator(){
        ObjectId objectId = new ObjectId();
        quoteObject = new QuoteObject();
        quoteObject.setId(objectId);
        quoteObject.setAuthor("author");
        quoteObject.setText("trial");
        quoteObject.setCreator(null);
        ObjectId id = quoteService.createQuote(quoteObject);
        assertNull(quoteService.getQuote(id));
    }





    //Testing parseQuote methods
    //method is private

    //test when we update quotes
    @Test
    @Order(9)
    public void testUpdateQuotes(){
        quoteObject.setText("this is a test");
        quoteObject.setAuthor("author2");
        assertTrue(quoteService.updateQuote(quoteObject));
    }


    //failed cause when getting quote object with null quoteObject
    @Test
    @Order(10)
    public void testUpdateQuotesWithNull(){
        quoteObject = null;
        assertFalse(quoteService.updateQuote(quoteObject));
    }



    //Failed because if getQuotes is null then parse method returns exception
    @Test
    @Order(11)
    public void testUpdateQuotesWithRandomId(){
        QuoteObject newQuoteObject = new QuoteObject();
        newQuoteObject.setId(new ObjectId());
        newQuoteObject.setAuthor("author3");
        assertFalse(quoteService.updateQuote(newQuoteObject));
    }



    //if id is null then getObject gives you null
    @Test
    @Order(12)
    public void testUpdateQuotesWithNullId(){
        quoteObject.setId(null);
        assertFalse(quoteService.updateQuote(quoteObject));
    }
}
