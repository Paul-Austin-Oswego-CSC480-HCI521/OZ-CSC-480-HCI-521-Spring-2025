package com.quotes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonId;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuoteObject {
    @BsonId
    @JsonProperty("_id")
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;

    @JsonProperty("author")
    @BsonProperty("author")
    private String author;

    @JsonProperty("quote")
    @BsonProperty("quote")
    private String quote;

    @JsonProperty("bookmarks")
    @BsonProperty("bookmarks")
    private int bookmarks = -1;

    @JsonProperty("shares")
    @BsonProperty("shares")
    private int shares = -1;

    @JsonProperty("date")
    @BsonProperty("date")
    private int date = -1;

    @JsonProperty("tags")
    @BsonProperty("tags")
    private ArrayList<String> tags;

    @JsonProperty("flags")
    @BsonProperty("flags")
    private int flags = -1;

    @JsonProperty("private")
    @BsonProperty("private")
    private boolean isPrivate;

    @JsonProperty("creator")
    @BsonProperty("creator")
    private ObjectId creator;

    //getters
    public ObjectId getId() {return id;}
    public String getAuthor() {return author;}
    public String getText() {return  quote;}
    public int getBookmarks() {return bookmarks;}
    public int getShares() {return shares;}
    public int getDate() {return date;}
    public ArrayList<String> getTags() {return tags;}
    public int getFlags() {return flags;}
    public boolean getisPrivate() {return isPrivate;}
    public ObjectId getCreator() {return creator;}

    //setters
    public void setId(ObjectId id) {this.id = id;}
    public void setAuthor(String author) {this.author = author;}
    public void setText(String text) {this.quote = text;}
    public void setBookmarks(int bookmarks) {this.bookmarks = bookmarks;}
    public void setShares(int shares) {this.shares = shares;}
    public void setDate(int date) {this.date = date;}
    public void setTags(ArrayList<String> tags) {this.tags = tags;}
    public void setFlags(int flags) {this.flags = flags;}
    public void setPrivate(boolean privacy) {this.isPrivate = privacy;}
    public void setCreator(ObjectId creator) {this.creator = creator;}

    //printers
    public void PrintQuote(){
        System.out.println("ID: "+id);
        System.out.println("Author: "+author);
        System.out.println("Quote: "+quote);
        System.out.println("Bookmarks: "+bookmarks);
        System.out.println("Shares: "+shares);
        if(tags != null){
            System.out.println("Tags: "+tags.toString());
        } else {
            System.out.println("Tags: null");
        }
        System.out.println("Flags: "+flags);
    }
}
