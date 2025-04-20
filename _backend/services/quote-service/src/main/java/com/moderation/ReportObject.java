package com.moderation;

import com.quotes.ObjectIdDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.ArrayList;
import java.util.List;

public class ReportObject {

    // status constants (this is done this way incase there are more statuses in the future)
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_IGNORED = "ignored";

    /*
     * This class represents a report object that is used to store information about a report.
     * each report contains the following fields:
     *  _id             - ObjectId   - id for the report
     *  quote_id        - String     - id for the quote being reported
     *  reporter_ids    - List<String> - list of ids for users who reported the quote
     *  context_types   - List<String> - list of reasons users reported the quote (from drop-down list)
     *  custom_messages - List<String> - custom messages provided by users if context_type is 'other' (optional)
     *  report_date     - int        - date & time when the report was created (epoch timestamp)
     *  status          - String     - status of the report (either 'open' or 'ignored') [NOTE: report gets deleted if the reported quote is deleted]
     */

    @BsonId
    @JsonProperty("_id")
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;

    @JsonProperty("quote_id")
    @BsonProperty("quote_id")
    private String quote_id;
    
    @JsonProperty("reporter_ids")
    @BsonProperty("reporter_ids")
    private List<String> reporter_ids = new ArrayList<>();
    
    @JsonProperty("context_types")   
    @BsonProperty("context_types")
    private List<String> context_types = new ArrayList<>();
    
    @JsonProperty("message")
    @BsonProperty("message")
    private List<String> custom_messages = new ArrayList<>();

    @JsonProperty("report_date")
    @BsonProperty("report_date")    
    private int report_date;
    
    @JsonProperty("status")
    @BsonProperty("status")
    private String status = STATUS_OPEN; // Default status is "open"

    //getters
    public ObjectId getID() {return id;}
    public String getQuoteID() { return quote_id;}
    public List<String> getReporterIDs() { return reporter_ids; }
    public List<String> getContextTypes() { return context_types; }
    public List<String> getCustomMessages() { return custom_messages; }
    public int getReportDate() { return report_date; }
    public String getStatus() { return status; }

    //setters
    public void setID(ObjectId id) { this.id = id; }
    public void setQuoteID(String quote_id) { this.quote_id = quote_id; }
    public void setReporterIDs(List<String> reporter_ids) { this.reporter_ids = reporter_ids; }
    public void setContextTypes(List<String> context_types) { this.context_types = context_types; }
    public void setCustomMessages(List<String> custom_messages) { this.custom_messages = custom_messages; }
    public void setReportDate(int report_date) { this.report_date = report_date; }
    public void setStatus(String status) { this.status = status; }
    
    // add a single reporter ID
    public void addReporterID(String reporter_id) {
        if (this.reporter_ids == null) {
            this.reporter_ids = new ArrayList<>();
        }
        this.reporter_ids.add(reporter_id); // duplicate IDs are handled by the endpoint logic
    }
    
    // add a single context type
    public void addContextType(String context_type) {
        if (this.context_types == null) {
            this.context_types = new ArrayList<>();
        }
        // only add the context type if it doesn't already exist in the list
        if (!this.context_types.contains(context_type)) {
            this.context_types.add(context_type);
        }
    }
    
    // add a single custom message
    public void addCustomMessage(String message) {
        if (this.custom_messages == null) {
            this.custom_messages = new ArrayList<>();
        }
        if (message != null && !message.trim().isEmpty()) {
            this.custom_messages.add(message);
        }
    }

    // checks if status is valid
    public boolean isValidStatus(String status) {return STATUS_OPEN.equals(status) || STATUS_IGNORED.equals(status);}
    // mark report as ignored
    public void markAsIgnored() { this.status = STATUS_IGNORED;}
    // mark report as open
    public void markAsOpen() { this.status = STATUS_OPEN;}
}
