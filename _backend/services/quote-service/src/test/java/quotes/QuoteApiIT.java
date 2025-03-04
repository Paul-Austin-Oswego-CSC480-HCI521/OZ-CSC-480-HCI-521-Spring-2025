package quotes;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


import com.quotes.*;

public class QuoteApiIT {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:9082/quotes";
    }

    // Helper method to create a test quote and return its ID
    private String createTestQuote() {
        String quoteJson = "{\"author\": \"Test Author\", \"quote\": \"Test quote text\", \"tags\": [\"test\"]}";
        Response response = given()
                .contentType(ContentType.JSON)
                .body(quoteJson)
                .when()
                .post("/create")
                .then()
                .statusCode(200)
                .extract().response();
        return response.jsonPath().getString("_id");
    }

    // Helper method to clean up a quote by ID
    private void deleteTestQuote(String quoteId) {
        given()
                .pathParam("quoteId", quoteId)
                .when()
                .delete("/delete/{quoteId}")
                .then()
                .statusCode(200);
    }

    // Test Cases for  GET Quote by ID
    
    @Test
    public void testGetQuoteById_Success() {
        String quoteId = createTestQuote();

        given()
            .pathParam("quoteID", quoteId)
            .when()
            .get("/search/id/{quoteID}")
            .then()
            .statusCode(200)
            .body("_id", equalTo(quoteId))
            .body("author", equalTo("Test Author"))
            .body("quote", equalTo("Test quote text"));

        deleteTestQuote(quoteId);
    }

    @Test
    public void testGetQuoteById_InvalidId() {
        given()
            .pathParam("quoteID", "invalid")
            .when()
            .get("/search/id/{quoteID}")
            .then()
            .statusCode(400)
            .body(equalTo("Given ID is not valid ObjectId"));
    }

    @Test
    public void testGetQuoteById_NotFound() {
    String quoteId = createTestQuote();
    deleteTestQuote(quoteId); // Ensure it’s deleted

    given()
        .pathParam("quoteID", quoteId)
        .when()
        .get("/search/id/{quoteID}")
        .then()
        .statusCode(404)
        .body(equalTo("Returned Json was null. Check quote ID is correct"));
    }

    // Test Cases for  /POST Create Quote

    @Test
    public void testCreateQuote_Success() {
        String quoteJson = "{\"author\": \"New Author\", \"quote\": \"New quote\", \"tags\": [\"new\"]}";

        Response response = given()
            .contentType(ContentType.JSON)
            .body(quoteJson)
            .when()
            .post("/create")
            .then()
            .statusCode(200)
            .body("_id", notNullValue())
            .extract().response();

        String quoteId = response.jsonPath().getString("_id");
        deleteTestQuote(quoteId);
    }

    @Test
    public void testCreateQuote_InvalidInput() {
        String invalidJson = "{\"author\": \"Author\", \"quote\": \"\", \"tags\": [\"test\"]}";

        given()
            .contentType(ContentType.JSON)
            .body(invalidJson)
            .when()
            .post("/create")
            .then()
            .statusCode(400)
            .body(equalTo("Returned QuoteID null. Check json is formatted Correctly"));
    }

    // Test Cases for  /PUT /update Quote
    @Test
    public void testUpdateQuote_Success() {
        String quoteId = createTestQuote();

        String updateJson = String.format("{\"_id\": \"%s\", \"author\": \"Updated Author\", \"bookmarks\": 1}", quoteId);
        given()
            .contentType(ContentType.JSON)
            .body(updateJson)
            .when()
            .put("/update")
            .then()
            .statusCode(200)
            .body("success", equalTo("true"));

        // Verify update
        given()
            .pathParam("quoteID", quoteId)
            .when()
            .get("/search/id/{quoteID}")
            .then()
            .body("author", equalTo("Updated Author"))
            .body("bookmarks", equalTo(1));

        deleteTestQuote(quoteId);
        }

    // @Test
    // public void testUpdateQuote_NonExistentId() {
    // String updateJson = "{\"_id\": \"67abf3b6b0d20a5237456441\", \"author\": \"New Author\"}";

    // given()
    //     .contentType(ContentType.JSON)
    //     .body(updateJson)
    //     .when()
    //     .put("/update")
    //     .then()
    //     .statusCode(409)
    //     .body(equalTo("Error updating quote, Json could be wrong or is missing quote ID"));
    // }

    // Test cases for /DELETE quote by ID
    @Test
    public void testDeleteQuote_Success() {
        String quoteId = createTestQuote();

        given()
            .pathParam("quoteId", quoteId)
            .when()
            .delete("/delete/{quoteId}")
            .then()
            .statusCode(200)
            .body(equalTo("Quote successfully deleted"));

        // Verify deletion
        given()
            .pathParam("quoteID", quoteId)
            .when()
            .get("/search/id/{quoteID}")
            .then()
            .statusCode(404);
    }

    @Test
    public void testDeleteQuote_NonExistentId() {
    String quoteId = createTestQuote();
    deleteTestQuote(quoteId); // Delete it first

    given()
        .pathParam("quoteId", quoteId)
        .when()
        .delete("/delete/{quoteId}")
        .then()
        .statusCode(404)
        .body(equalTo("Quote not found, could not be deleted"));
    }

    // Test case for /GET /search/query
    @Test
    public void testFuzzySearch_Success() {
        createTestQuote(); // "Test quote text"
        String quoteId2 = createTestQuote(); // Another with same text

        given()
            .pathParam("query", "test quote")
            .when()
            .get("/search/query/{query}")
            .then()
            .statusCode(200);
            // .body("size()", greaterThan(0))
            // .body("[0].quote", containsString("test quote"));

        deleteTestQuote(quoteId2);
    }

    // Test case for /GET /search/topBookmarked
    @Test
    public void testGetTopBookmarked() {
        String quoteId1 = createTestQuote();
        String quoteId2 = createTestQuote();
        incrementBookmarks(quoteId2, 2); // Set to 2
        incrementBookmarks(quoteId1, 1); // Set to 1

        given()
            .when()
            .get("/search/topBookmarked")
            .then()
            .statusCode(200)
            .body("[0]._id", equalTo(quoteId2))
            .body("[1]._id", equalTo(quoteId1));

        deleteTestQuote(quoteId1);
        deleteTestQuote(quoteId2);
    }
    // helper method for testing bookmarks
    private void incrementBookmarks(String quoteId, int times) {
    for (int i = 0; i < times; i++) {
        String updateJson = String.format("{\"_id\": \"%s\", \"bookmarks\": %d}", quoteId, i + 1);
        given()
            .contentType(ContentType.JSON)
            .body(updateJson)
            .when()
            .put("/update")
            .then()
            .statusCode(200);
    }
}

}