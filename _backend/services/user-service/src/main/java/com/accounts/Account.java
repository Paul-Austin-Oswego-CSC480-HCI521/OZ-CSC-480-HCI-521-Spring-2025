package com.accounts;

import com.google.gson.Gson;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.bson.Document;


public class Account {

    // OAuth dependant (let requirements know if changed)
    public String Email;
    public String Username;
    public int admin;

    public String access_token;
    public String refresh_token;
    public Long expires_at;
    public List<String> scope;
    public String token_type;

    public List<String> Notifications;  //???

    public List<String> MyQuotes;

    public List<String> BookmarkedQuotes;

    public List<String> SharedQuotes;

    public List<String> MyTags;

    public String Profession;

    public String PersonalQuote;

    public Map<String, String> UsedQuotes;


    public Account(String email, String username, int ad, String at, String rt, Long ea,
                   List<String> sc, String tt) {
        Email = email;
        Username = username;
        admin = ad;
        access_token = at;
        refresh_token = rt;
        expires_at = ea;
        scope = sc;
        token_type = tt;
        Notifications = new ArrayList<>();
        MyQuotes = new ArrayList<>();
        BookmarkedQuotes = new ArrayList<>();
        SharedQuotes = new ArrayList<>();
        MyTags = new ArrayList<>();
        Profession = "";
        PersonalQuote = "";
        UsedQuotes = new HashMap<>();
    }

    public Account(String email, String username, int ad, String at, String rt, Long ea,
                   List<String> sc, String tt, List<String> notifications, List<String>myQuotes,
                   List<String> bookmarkedQuotes, List<String> sharedQuotes,
                   List<String> myTags, String profession, String personalQuote,Map<String, String> usedQuotes) {
        Email = email;
        Username = username;
        admin = ad;
        access_token = at;
        refresh_token = rt;
        expires_at = ea;
        scope = sc;
        token_type = tt;
        Notifications = notifications;
        MyQuotes = myQuotes;
        BookmarkedQuotes = bookmarkedQuotes;
        SharedQuotes = sharedQuotes;
        MyTags = myTags;
        Profession = profession;
        PersonalQuote = personalQuote;
        UsedQuotes = usedQuotes;
    }

    public Account updateOauthProperties(GoogleTokenResponse tokenResponse) {
        access_token = tokenResponse.getAccessToken();
        refresh_token = tokenResponse.getRefreshToken();
        expires_at = System.currentTimeMillis() + tokenResponse.getExpiresInSeconds() * 1000;
        token_type = tokenResponse.getTokenType();

        return this;
    }

    public String toJson() {
        Document doc = Document.parse(new Gson().toJson(this));
        if (doc.containsKey("_id")) {
            doc.replace("_id", doc.getObjectId("_id").toHexString());
        }
        return doc.toJson();
    }
}
