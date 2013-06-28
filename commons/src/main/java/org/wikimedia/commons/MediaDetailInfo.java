package org.wikimedia.commons;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MediaDetailInfo {
    private ArrayList<String> categories;
    private Map<String, String> descriptions;
    private String author;
    private Date date;

    public MediaDetailInfo(ArrayList<String> categories, Map<String, String>descriptions, String author, Date date) {
        this.categories = categories;
        this.descriptions = descriptions;
        this.author = author;
        this.date = date;
    }

    public ArrayList<String> getCategories() {
        return (ArrayList<String>)categories.clone(); // feels dirty
    }

    public String getDescription(String preferredLanguage) {
        if (descriptions.containsKey(preferredLanguage)) {
            // See if the requested language is there.
            return descriptions.get(preferredLanguage);
        } else if (descriptions.containsKey("en")) {
            // Ah, English. Language of the world, until the Chinese crush us.
            return descriptions.get("en");
        } else if (descriptions.containsKey("default")) {
            // No languages marked...
            return descriptions.get("default");
        } else {
            // FIXME: return the first available non-English description?
            return "";
        }
    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }
}
