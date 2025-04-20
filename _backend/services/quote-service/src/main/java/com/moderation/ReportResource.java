package com.moderation;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import com.quotes.MongoUtil;
import com.quotes.QuotesRetrieveAccount;
import com.quotes.SanitizerClass;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/report")
public class ReportResource {

    private MongoClient client;
    private MongoDatabase moderationDB;
    private MongoCollection<Document> reportsCollection;

    public ReportResource() {
        client = MongoClients.create(System.getenv("CONNECTION_STRING"));
        moderationDB = client.getDatabase("Moderation");
        reportsCollection = moderationDB.getCollection("Reports");
    }

    /*
     * TODO: endpoint to change the status of a report
     * TODO: endpoint to get all reports 
     * TODO: fix create ep to increment the number of reports on a quote
     */


    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Report successfully created or updated"),
            @APIResponse(responseCode = "400", description = "Invalid request data"),
            @APIResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT"),
            @APIResponse(responseCode = "409", description = "User has already reported this quote")
    })
    @Operation(summary = "Create or update a report for a quote", 
               description = "Creates a new report for a quote or updates an existing report if one already exists")
    @RequestBody(description = "Report data", required = true, 
                 content = @Content(mediaType = "application/json",
                 examples = @ExampleObject(name = "Sample Report", 
                 value = "{\n  \"quote_id\": \"67abf3b6b0d20a5237456441\",\n  \"context_type\": \"offensive\",\n  \"message\": \"\"\n}")))
    public Response createReport(String reportJson, @Context HttpHeaders headers) {
        // auth
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new Document("error", "Missing or invalid Authorization header").toJson())
                    .build();
        }

        String jwtString = authHeader.replaceFirst("(?i)^Bearer\\s+", "");

        Map<String, String> jwtMap= QuotesRetrieveAccount.retrieveJWTData(jwtString);

        if (jwtMap == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to create quotes").toJson()).build();
        }

        // get account ID from JWT
        String accountID = jwtMap.get("subject");

        // get group from JWT
        String group = jwtMap.get("group");

        if (group == null || accountID == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized to create quotes").toJson()).build();
        }

        ObjectId accountObjectId = new ObjectId(accountID);

        try {
            Document reportDoc = Document.parse(reportJson);
            
            String quoteId = reportDoc.getString("quote_id");
            String contextType = reportDoc.getString("context_type");
            
            if (quoteId == null || contextType == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new Document("error", "quote_id and context_type are required").toJson())
                        .build();
            }
            
            if (!SanitizerClass.validObjectId(quoteId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new Document("error", "Invalid quote_id format").toJson())
                        .build();
            }
            
            String message = reportDoc.getString("message");
            
            // check if a report for this quote already exists
            Document existingReport = reportsCollection.find(eq("quote_id", quoteId)).first();
            
            if (existingReport != null) {
                // report exists, check if this user already reported it
                @SuppressWarnings("unchecked")
                List<String> reporterIds = (List<String>) existingReport.get("reporter_ids");
                
                if (reporterIds.contains(accountID)) {
                    return Response.status(Response.Status.CONFLICT)
                            .entity(new Document("error", "User has already reported this quote").toJson())
                            .build();
                }
                
                // update existing report
                reporterIds.add(accountID);
                existingReport.put("reporter_ids", reporterIds);
                
                // add new context_type if its not the same as an existing one
                @SuppressWarnings("unchecked")
                List<String> contextTypes = (List<String>) existingReport.get("context_types");
                if (!contextTypes.contains(contextType)) {
                    contextTypes.add(contextType);
                    existingReport.put("context_types", contextTypes);
                }
                
                // add custom_message (ignores if empty)
                if (message != null && !message.trim().isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<String> customMessages = existingReport.get("message", List.class);
                    if (customMessages == null) {
                        customMessages = new ArrayList<>();
                        existingReport.put("message", customMessages);
                    }
                    customMessages.add(message.trim());
                }
                
                reportsCollection.replaceOne(eq("_id", existingReport.getObjectId("_id")), existingReport);
                
                return Response.status(Response.Status.CREATED)
                        .entity(new Document("message", "Report updated successfully").toJson())
                        .build();
            } else {
                // create new report
                Document newReport = new Document()
                        .append("_id", new ObjectId())
                        .append("quote_id", quoteId)
                        .append("reporter_ids", new ArrayList<String>(List.of(accountID)))
                        .append("context_types", new ArrayList<String>(List.of(contextType)))
                        .append("report_date", (int)(System.currentTimeMillis() / 1000))
                        .append("status", ReportObject.STATUS_OPEN);
                
                // add custom_message (ignores if empty)
                if (message != null && !message.trim().isEmpty()) {
                    newReport.append("message", new ArrayList<String>(List.of(message.trim())));
                }
                
                reportsCollection.insertOne(newReport);
                
                return Response.status(Response.Status.CREATED)
                        .entity(new Document()
                                .append("message", "Report created successfully")
                                .append("_id", newReport.getObjectId("_id").toString())
                                .toJson())
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new Document("error", "Error processing request: " + e.getMessage()).toJson())
                    .build();
        }
    }

    @DELETE
    @Path("/delete/{reportId}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Report successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid report ID format"),
            @APIResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT"),
            @APIResponse(responseCode = "404", description = "Report not found")
    })
    @Operation(summary = "Delete a report", 
               description = "Removes a report from the database")
    public Response deleteReport(@PathParam("reportId") String reportId, @Context HttpHeaders headers) {
        // auth
        
        try {
            if (!SanitizerClass.validObjectId(reportId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new Document("error", "Invalid report ID format").toJson())
                        .build();
            }
            
            ObjectId objectId = new ObjectId(reportId);
            Document report = reportsCollection.find(eq("_id", objectId)).first();
            
            if (report == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new Document("error", "Report not found").toJson())
                        .build();
            }
            
            reportsCollection.deleteOne(eq("_id", objectId));
            
            return Response.status(Response.Status.OK)
                    .entity(new Document("message", "Report deleted successfully").toJson())
                    .build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new Document("error", "Error deleting report: " + e.getMessage()).toJson())
                    .build();
        }
    }
}