package com.moderation;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.ibm.websphere.security.jwt.InvalidConsumerException;
import com.ibm.websphere.security.jwt.InvalidTokenException;
import com.ibm.websphere.security.jwt.JwtConsumer;
import com.ibm.websphere.security.jwt.JwtToken;
import com.quotes.QuotesRetrieveAccount;
import com.quotes.SanitizerClass;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
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
@ApplicationScoped
public class ReportResource {

    @Inject
    private MongoClient client;

    private MongoDatabase moderationDB;
    private MongoCollection<Document> reportsCollection;

    private MongoDatabase dataDB;
    private MongoCollection<Document> quotesCollection;

    @PostConstruct
    public void init() {
        moderationDB = client.getDatabase("Moderation");
        reportsCollection = moderationDB.getCollection("Reports");

        dataDB = client.getDatabase("Data");
        quotesCollection = dataDB.getCollection("Quotes");
    }

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
            boolean isNewReporter = false;

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
                
                // update existing report - This is a new reporter
                isNewReporter = true;
                reporterIds.add(accountID);
                existingReport.put("reporter_ids", reporterIds);
                
                // add new context_type if its not the same as an existing one
                @SuppressWarnings("unchecked")
                List<String> contextTypes = (List<String>) existingReport.get("context_types");
                
                // support for single user, multiple resons
                String[] splitTypes = contextType.split(",");
                for (String type : splitTypes) {
                    String trimmed = type.trim();
                    if (!trimmed.isEmpty() && !contextTypes.contains(trimmed)) {
                        contextTypes.add(trimmed);
                    }
                }
                existingReport.put("context_types", contextTypes);
                
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
                
            } else {
                // create new report - This is a new reporter
                isNewReporter = true;
                Document newReport = new Document()
                        .append("_id", new ObjectId())
                        .append("quote_id", quoteId)
                        .append("reporter_ids", new ArrayList<String>(List.of(accountID)));
                
                // support for single user, multiple resons
                List<String> contextTypesList = new ArrayList<>();
                String[] splitTypes = contextType.split(",");
                for (String type : splitTypes) {
                    String trimmed = type.trim();
                    if (!trimmed.isEmpty()) {
                        contextTypesList.add(trimmed);
                    }
                }
                newReport.append("context_types", contextTypesList);
                
                
                newReport.append("report_date", (int)(System.currentTimeMillis() / 1000))
                        .append("status", ReportObject.STATUS_OPEN);
                
                // add custom_message (ignores if empty)
                if (message != null && !message.trim().isEmpty()) {
                    newReport.append("message", new ArrayList<String>(List.of(message.trim())));
                }
                
                reportsCollection.insertOne(newReport);
            }
            
            // if this is a new reporter, increment the flags counter of the quote
            if (isNewReporter) {
                ObjectId quoteObjectId = new ObjectId(quoteId);
                
                Document quote = quotesCollection.find(eq("_id", quoteObjectId)).first();
                if (quote != null) {
                    int currentFlags = quote.getInteger("flags", 0);

                    quotesCollection.updateOne(
                        eq("_id", quoteObjectId),
                        new Document("$set", new Document("flags", currentFlags + 1))
                    );
                }
            }
            
            return Response.status(Response.Status.CREATED)
                    .entity(new Document("message", "Report " + 
                           (existingReport != null ? "updated" : "created") + " successfully")
                           .toJson())
                    .build();
                    
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new Document("error", "Error processing request: " + e.getMessage()).toJson())
                    .build();
        }
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully retrieved all reports"),
            @APIResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "Get all reports", 
              description = "Returns JSON array of all reports in the system. Only accessible by moderators and administrators.")
    public Response getAllReports(@Context HttpHeaders headers) {
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
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized for admin panel").toJson()).build();
        }
        // get account ID from JWT
        String accountID = jwtMap.get("subject");

        // get group from JWT
        String group = jwtMap.get("group");
        if (group == null || accountID == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized for admin panel").toJson()).build();
        }

        try {
            List<Document> reports = reportsCollection.find().into(new ArrayList<>());
            
            for (Document report : reports) {
                if (report.containsKey("_id")) {
                    report.put("_id", report.getObjectId("_id").toString());
                }

                //full quote information from id, add to report
                String quoteId = report.getString("quote_id");
                if (quoteId != null && SanitizerClass.validObjectId(quoteId)) {
                    ObjectId quoteObjectId = new ObjectId(quoteId);
                    Document quote = quotesCollection.find(eq("_id", quoteObjectId)).first();
                    if (quote != null) {
                        quote.put("_id", quote.getObjectId("_id").toString());
                        report.put("quote", quote); 
                    }
                }
            }
            
            Document result = new Document("reports", reports);
            return Response.ok(result.toJson()).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new Document("error", "Error retrieving reports: " + e.getMessage()).toJson())
                    .build();
        }
    }

    @GET
    @Path("/filter")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Successfully retrieved filtered reports"),
            @APIResponse(responseCode = "400", description = "Invalid filter or sort parameters"),
            @APIResponse(responseCode = "409", description = "Exception occurred during operation")
    })
    @Operation(summary = "Get filtered and sorted reports", 
              description = "Returns JSON array of reports filtered by type and/or sorted by specified criteria")
    public Response getFilteredReports(
            @Parameter(
                description = "Filter by report context_type -- for multiple filters, use comma-separated values (ex: 'offensive,spam,hateful')",
                required = false
            )
            @QueryParam("filterType") String filterType,
            
            @Parameter(
                description = "Field to sort by -- valid values: 'date' or 'flags'",
                required = false,
                schema = @Schema(defaultValue = "date", enumeration = {"date", "flags"})
            )
            @QueryParam("sortBy") @DefaultValue("date") String sortBy,
            
            @Parameter(
                description = "Sort order -- valid values: 'asc' or 'desc'",
                required = false,
                schema = @Schema(defaultValue = "desc", enumeration = {"asc", "desc"})
            )
            @QueryParam("sortOrder") @DefaultValue("desc") String sortOrder,
            
            @Context HttpHeaders headers) {

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
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized for admin panel").toJson()).build();
        }
        // get account ID from JWT
        String accountID = jwtMap.get("subject");

        // get group from JWT
        String group = jwtMap.get("group");
        if (group == null || accountID == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(new Document("error", "User not authorized for admin panel").toJson()).build();
        } 
        
        
        try {
            List<Document> filteredReports = new ArrayList<>();
            
            if (filterType != null && !filterType.isEmpty()) {
                String[] types = filterType.split(",");
                List<String> typesList = new ArrayList<>();
                for (String type : types) {
                    String trimmed = type.trim();
                    if (!trimmed.isEmpty()) {
                        typesList.add(trimmed);
                    }
                }
                
                List<Document> regexPatterns = new ArrayList<>();
                for (String type : typesList) {
                    regexPatterns.add(new Document("context_types", 
                        new Document("$regex", type).append("$options", "i")));
                }
                
                if (!regexPatterns.isEmpty()) {
                    filteredReports = reportsCollection.find(new Document("$or", regexPatterns))
                        .into(new ArrayList<>());
                } else {
                    filteredReports = reportsCollection.find().into(new ArrayList<>());
                }
            } else {
                filteredReports = reportsCollection.find().into(new ArrayList<>());
            }
            
            if (!sortBy.equalsIgnoreCase("flags") && !sortBy.equalsIgnoreCase("date")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new Document("error", "Invalid sortBy parameter. Use 'flags' or 'date'").toJson())
                        .build();
            }
            
            if (!sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new Document("error", "Invalid sortOrder parameter. Use 'asc' or 'desc'").toJson())
                        .build();
            }
            

            for (Document report : filteredReports) {
                if (report.containsKey("_id")) {
                    report.put("_id", report.getObjectId("_id").toString());
                }
                
                //full quote information from id, add to report
                String quoteId = report.getString("quote_id");
                if (quoteId != null && SanitizerClass.validObjectId(quoteId)) {
                    ObjectId quoteObjectId = new ObjectId(quoteId);
                    Document quote = quotesCollection.find(eq("_id", quoteObjectId)).first();
                    if (quote != null) {
                        quote.put("_id", quote.getObjectId("_id").toString());
                        report.put("quote", quote);
                    }
                }
            }
            
            final boolean ascending = sortOrder.equalsIgnoreCase("asc");
            
            if (sortBy.equalsIgnoreCase("flags")) {
                List<Document> validReports = new ArrayList<>();
                
                for (Document report : filteredReports) {
                    Document quote = (Document) report.get("quote");
                    
                    // only includes reports where -- quote exists, flags field exists, flags value is greater than 0
                    if (quote != null && quote.containsKey("flags") && quote.getInteger("flags") > 0) {
                        validReports.add(report);
                    }
                }
                
                filteredReports = validReports;
                
                filteredReports.sort((a, b) -> {
                    Document quoteA = (Document) a.get("quote");
                    Document quoteB = (Document) b.get("quote");
                    
                    int flagsA = quoteA.getInteger("flags");
                    int flagsB = quoteB.getInteger("flags");
                    
                    return ascending ? Integer.compare(flagsA, flagsB) : Integer.compare(flagsB, flagsA);
                });
            } else {
                filteredReports.sort((a, b) -> {
                    int dateA = a.getInteger("report_date", 0);
                    int dateB = b.getInteger("report_date", 0);
                    
                    return ascending ? Integer.compare(dateA, dateB) : Integer.compare(dateB, dateA);
                });
            }
            
            Document result = new Document("reports", filteredReports);
            return Response.ok(result.toJson()).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new Document("error", "Error retrieving filtered reports: " + e.getMessage()).toJson())
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

    @PATCH
    @Path("/ignore/{reportId}")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Report status successfully changed to ignored"),
            @APIResponse(responseCode = "400", description = "Invalid report ID format"),
            @APIResponse(responseCode = "404", description = "Report not found")
    })
    @Operation(summary = "Ignore a report", 
               description = "Changes the status of a report to 'ignored'")
    public Response ignoreReport(@PathParam("reportId") String reportId, @Context HttpHeaders headers) {
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
            
            reportsCollection.updateOne(
                eq("_id", objectId),
                new Document("$set", new Document("status", ReportObject.STATUS_IGNORED))
            );
            
            return Response.status(Response.Status.OK)
                    .entity(new Document("message", "Report status changed to ignored").toJson())
                    .build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new Document("error", "Error updating report status: " + e.getMessage()).toJson())
                    .build();
        }
    }
}