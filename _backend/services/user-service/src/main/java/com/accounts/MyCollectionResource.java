package com.accounts;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.Document;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;


@Path("MyCollection")
public class MyCollectionResource {

    @Inject
    @RestClient
    private QuoteClient quoteClient;

    public static AccountService accountService = new AccountService();

    public enum SortOptions {
        NONE, USED_NEWEST, USED_OLDEST, CREATED_NEWEST, CREATED_OLDEST;

        public static SortOptions fromString(String input) { //selects value from string
            try {
                return SortOptions.valueOf(input.toUpperCase());
            } catch (Exception e) { //if string does not match any case it defaults to NONE
                return NONE;
            }
        }
    }

    @GET
    @Path("/MyQuotes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSavedPublicQuotes(@Context HttpHeaders header,
                                         @QueryParam("visibility") List<String> visibility, // public, private, shared
                                         @QueryParam("usage") List<String> usage, // used, unused
                                         @QueryParam("tags") List<String> tags, // tags quote must have
                                         @QueryParam("sort") String sortParam) // how to order results
    {
        SortOptions sort = SortOptions.fromString(sortParam);
        String authHeader = header.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }
        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Document doc = accountService.retrieveUserFromJWT(jwtString);
        if(doc != null) {
            doc.remove("expires_at");
            Account acc = accountService.document_to_account(doc);

            //get all bookmarked quotes
            List<JsonObject> bookmarkedQuotes = new ArrayList<>();
            for(String oid : acc.BookmarkedQuotes) {
                Response quoteSearchResponse = quoteClient.idSearch(oid);
                if(quoteSearchResponse.getStatus() == Response.Status.OK.getStatusCode()) {
                    JsonObject quoteJson = quoteSearchResponse.readEntity(JsonObject.class);
                    bookmarkedQuotes.add(quoteJson);
                }
            }

            //filter privacy visibility, removes quotes if "private" value is not in visibility list
            bookmarkedQuotes.removeIf(quote -> (quote.getBoolean("private") && !visibility.contains("private")) || (!quote.getBoolean("private") && !visibility.contains("public")));

            //filter shared quotes

            //filter usage

            //filter tags
            if(!tags.isEmpty()){ //if there are specified tags
                bookmarkedQuotes.removeIf(quote -> {
                    JsonArray tagsArray = quote.getJsonArray("tags");
                    if(tagsArray == null || tagsArray.isEmpty()) {
                        return true; //remove quote if no tags
                    }
                    //convert json array to list
                    List<String> tagsList = new ArrayList<>();
                    for(int i = 0; i < tagsArray.size(); i++) {
                        tagsList.add(tagsArray.getString(i));
                    }

                    //remove if no intersection
                    return intersection(tags, tagsList).isEmpty();
                });
            }

            //reorder
            switch(sort) {
                case NONE -> {break;}
                case USED_NEWEST -> {}

            }
        }

        return Response.ok().build();
    }

    private List<String> intersection(List<String> list1, List<String> list2) {
        List<String> result = new ArrayList<String>(list1);
        result.retainAll(list2);
        return result;
    }

}