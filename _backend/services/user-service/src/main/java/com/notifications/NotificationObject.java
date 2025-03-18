package com.notifications;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationObject {
    @BsonId
    @JsonProperty("_id")
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;

    @JsonProperty("from")
    @BsonProperty("from")
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId from;

    @JsonProperty("to")
    @BsonProperty("to")
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId to;

    @JsonProperty("type")
    @BsonProperty("type")
    private String type;

    @JsonProperty("quote_id")
    @BsonProperty("quote_id")
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId quoteId;

    @JsonProperty("created_at")
    @BsonProperty("created_at")
    private int createdAt;

    public ObjectId getId() {return id;}
    public ObjectId getFrom() {return from;}
    public ObjectId getTo() {return to;}
    public String getType() {return type;}
    public ObjectId getQuoteId() {return quoteId;}
    public int getCreatedAt() {return createdAt;}

    public void setId(ObjectId id) {this.id = id;}
    public void setFrom(ObjectId from) {this.from = from;}
    public void setTo(ObjectId to) {this.to = to;}
    public void setType(String type) {this.type = type;}
    public void setQuoteId(ObjectId quoteId) {this.quoteId = quoteId;}
    public void setCreatedAt(int createdAt) {this.createdAt = createdAt;}

    public void printNotification() {
        System.out.println("ID: " + id);
        System.out.println("From: " + from);
        System.out.println("To: " + to);
        System.out.println("Type: " + type);
        System.out.println("Quote ID: " + quoteId);
        System.out.println("Created At: " + createdAt);
    }
}
