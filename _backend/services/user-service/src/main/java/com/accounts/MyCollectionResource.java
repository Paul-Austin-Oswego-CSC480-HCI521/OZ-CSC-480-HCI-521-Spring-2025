package com.accounts;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharedQuotes.SharedQuote;
import com.usedQuotes.UsedQuote;
import com.usedQuotes.UsedQuoteService;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.*;


@Path("/MyCollection")
public class MyCollectionResource {

    @Inject
    @RestClient
    private QuoteClient quoteClient;

    @Inject
    AccountService accountService;
    public static UsedQuoteService usedQuoteService = new UsedQuoteService();

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

    public enum UsedOptions {
        ALL, USED, UNUSED;

        public static UsedOptions fromString(String input) { //selects value from string
            try {
                return UsedOptions.valueOf(input.toUpperCase());
            } catch (Exception e) { //if string does not match any case it defaults to NONE
                return ALL;
            }
        }
    }

    @GET
    @Path("/MyQuotes")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get user data foy Collection page")
    public Response getSavedPublicQuotes(@Context HttpHeaders header,
                                         @QueryParam("visibility") @Parameter(description = "List that contains visibility you WANT to see. Enter as comma separated string Example: public,private,shared", required = true)
                                         String visibilityString, // public, private, shared
                                         @QueryParam("usage") @Parameter(description = "String value for filtering by usage, will only show quotes meeting this value. Defaults to ALL if left blank. Values can be: \"ALL\", \"USED\", \"UNUSED\" (case insensitive)", required = false)
                                             String usageString, // all, used, unused
                                         @QueryParam("tags") @Parameter(description = "List of tags the quote MUST include, used for filtering by tags. Enter tags as comma separated string", required = false)
                                             String tagsString, // tags quote must have
                                         @QueryParam("sort") @Parameter(description = "How the quotes should be sorted. Defaults to NONE if left blank. Values can be: \"NONE\", \"USED_NEWEST\", \"USED_OLDEST\", \"CREATED_NEWEST\", \"CREATED_OLDEST\" (case insensitive)", required = false)
                                             String sortParam) // how to order results
    {
        SortOptions sort = SortOptions.fromString(sortParam);
        UsedOptions used = UsedOptions.fromString(usageString);
        List<String> visibility;
        visibility = Arrays.stream(visibilityString.split(",")).toList();
        List<String> tags;
        tags = Arrays.stream(tagsString.split(",")).filter(s -> !s.isEmpty()).toList();

        String authHeader = header.getHeaderString(HttpHeaders.AUTHORIZATION);
        //System.out.println("Visibility: "+visibility);
        //System.out.println("Usage: "+used);
        //System.out.println("Tags: "+tags);
        //System.out.println("Sort: "+sort);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }
        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Document doc = accountService.retrieveUserFromJWT(jwtString);
        /*
        Response ResponseDoc = accountService.retrieveUser("680a6f20e438c004a600a543", false);
        String DocJson = ResponseDoc.readEntity(String.class);
        Document doc = Document.parse(DocJson);
        for testing
         */


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
            //get all shared quotes : if "shared"
            if(visibility.contains("shared")) {
                for(SharedQuote sharedQuote : acc.SharedQuotes) {
                    Response quoteSearchResponse = quoteClient.idSearch(sharedQuote.getQuoteId());
                    if(quoteSearchResponse.getStatus() == Response.Status.OK.getStatusCode()) {
                        JsonObject quoteJson = quoteSearchResponse.readEntity(JsonObject.class);
                        bookmarkedQuotes.add(quoteJson);
                    }
                }
            }

            //filter privacy visibility, removes quotes if "private" value is not in visibility list
            bookmarkedQuotes.removeIf(quote -> (quote.getBoolean("private") && !visibility.contains("private")) ||
                    (!quote.getBoolean("private") && !visibility.contains("public")));

            //filter usage
            switch(used) {
                case ALL -> {break;}
                case USED -> {
                    //remove all unused quotes, to show only used quotes
                    bookmarkedQuotes.removeIf(quote -> !acc.UsedQuotes.containsKey(quote.getString("_id")));
                }
                case UNUSED -> {
                    //remove all used quotes to show only unused quotes
                    bookmarkedQuotes.removeIf(quote -> acc.UsedQuotes.containsKey(quote.getString("_id")));
                }
            }

            //filter tags
            System.out.println("Tags List empty: "+tags.isEmpty());
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

                    //remove if no intersection/no matching tags
                    return intersection(tags, tagsList).isEmpty();
                });
            }

            //reorder
            switch(sort) {
                case NONE -> {break;}
                case USED_NEWEST -> {
                    //separate used quotes into new list
                    List<JsonObject> usedQuotes = new ArrayList<>();
                    for(JsonObject quote : bookmarkedQuotes) {
                        if(acc.UsedQuotes.containsKey(quote.getString("_id"))) {
                            usedQuotes.add(quote);
                            bookmarkedQuotes.remove(quote);
                        }
                    }
                    //sort by used date
                    sort_Used_Oldest(usedQuotes, acc, 0, usedQuotes.size()-1);
                    Collections.reverse(usedQuotes);
                    //add used quotes back to front of list
                    bookmarkedQuotes.addAll(0, usedQuotes);
                }
                case USED_OLDEST -> {
                    //separate used quotes into new list
                    List<JsonObject> usedQuotes = new ArrayList<>();
                    for(JsonObject quote : bookmarkedQuotes) {
                        if(acc.UsedQuotes.containsKey(quote.getString("_id"))) {
                            usedQuotes.add(quote);
                            bookmarkedQuotes.remove(quote);
                        }
                    }
                    //sort by used date
                    sort_Used_Oldest(usedQuotes, acc, 0, usedQuotes.size()-1);
                    //add used quotes back to front of list
                    bookmarkedQuotes.addAll(0, usedQuotes);
                }
                case CREATED_NEWEST -> {
                    bookmarkedQuotes.sort((a, b) -> {
                        int dateA = a.getInt("date");
                        int dateB = b.getInt("date");
                        return Integer.compare(dateB, dateA);
                    });
                }
                case CREATED_OLDEST -> {
                    bookmarkedQuotes.sort((a, b) -> {
                        int dateA = a.getInt("date");
                        int dateB = b.getInt("date");
                        return Integer.compare(dateA, dateB);
                    });
                }
            }

            return Response.ok().entity(bookmarkedQuotes).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Error finding user document").build();
    }

    private static void sort_Used_Oldest(List<JsonObject> usedQuotes, Account acc, int lower, int upper) {
        if(upper <= lower) return;

        int pivot = partition(usedQuotes, acc, lower, upper);
        sort_Used_Oldest(usedQuotes, acc, lower, pivot - 1);
        sort_Used_Oldest(usedQuotes, acc, pivot + 1, upper);
    }

    private static int partition(List<JsonObject> usedQuotes, Account acc, int lower, int upper) {
        try {
            JsonObject pivotObject = usedQuotes.get(upper);
            long pivotTime = pivotObject.getInt("date");

            int i = lower - 1;
            ObjectMapper mapper = new ObjectMapper();

            for(int j = lower; j <= upper - 1; j++) {
                //get used quote object
                Document usedQuoteDoc = usedQuoteService.retrieveUsedQuote(acc.UsedQuotes.get(usedQuotes.get(j).getString("_id")));
                UsedQuote usedQuoteObject = mapper.readValue(usedQuoteDoc.toJson(), UsedQuote.class);
                if(usedQuoteObject.getUsed().getTime() < pivotTime) {
                    i++;
                    JsonObject temp = usedQuotes.get(i);
                    usedQuotes.set(i, usedQuotes.get(j));
                    usedQuotes.set(j, temp);
                }
            }
            i++;
            JsonObject temp = usedQuotes.get(i + 1);
            usedQuotes.set(i + 1, usedQuotes.get(upper));
            usedQuotes.set(upper, temp);
            return i + 1;
        } catch (JsonProcessingException e) {
            System.out.print("Error in partition step: "+e);
            return -1;
        }
    }

    private List<String> intersection(List<String> list1, List<String> list2) {
        List<String> result = new ArrayList<String>(list1);
        result.retainAll(list2);
        return result;
    }

}