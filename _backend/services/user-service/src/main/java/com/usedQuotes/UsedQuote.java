package com.usedQuotes;

import com.google.gson.Gson;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonId;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;

public class UsedQuote {

    @BsonId
    @JsonProperty("_id")
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;
    public Date used;
    public int count;

    public UsedQuote(int Count, Date Used) {
      count = Count;
      used = Used;
    }

    public ObjectId getId() {return id;}
    public Date getUsed() {return used;}
    public int getCount() {return count;}
    
    public void setId(ObjectId id) {this.id = id;}
    public void setUsed(Date used) {this.used = used;}
    public void setCount(int count) {this.count = count;}


    public String toJson() {
        Document doc = Document.parse(new Gson().toJson(this));
        if (doc.containsKey("_id")) {
            doc.replace("_id", doc.getObjectId("_id").toHexString());
        }
        return doc.toJson();
    }
}
