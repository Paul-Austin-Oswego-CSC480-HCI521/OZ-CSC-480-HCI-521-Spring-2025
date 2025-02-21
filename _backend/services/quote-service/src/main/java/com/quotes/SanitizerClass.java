package com.quotes;

import java.util.ArrayList;

public class SanitizerClass {

    public static String sanitize(String input) {
        if(input == null) return null;
        //forces string to not contain any special characters
        return input.replaceAll("[^a-zA-Z0-9 .,!?'\"()\\-]", "");
    }

    public static QuoteObject sanitizeQuote(QuoteObject quote) {
        if(quote != null) {
            quote.setAuthor(sanitize(quote.getAuthor()));
            quote.setText(sanitize(quote.getText()));

            //do tags
            ArrayList<String> tagList = quote.getTags();
            if(!tagList.isEmpty()) {
                for(int i = 0; i < tagList.size(); i++) {
                    String safeTag = sanitize(tagList.get(i));
                    tagList.set(i, safeTag);
                }
                quote.setTags(tagList);
            }
            return quote;
        }
        return null;
    }

    public static boolean validObjectId(String input) {
        if(input == null) return false;
        return input.matches("^[a-f0-9]{24}$"); //Enforce objectId structure
    }

}
