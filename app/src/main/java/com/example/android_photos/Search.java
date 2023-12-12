package com.example.android_photos;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Search {

    private List<Album> allAlbums;

    public Search(List<Album> allAlbums) {
        this.allAlbums = allAlbums;
    }

    public List<Picture> searchPhotos(String searchType, String tagName1, String tagValue1, String tagName2, String tagValue2) {
        List<Picture> results = new ArrayList<>();

        for (Album album : allAlbums) {
            for (Picture pic : album.getPics()) {
                boolean matchesFirstCriteria = matchesCriteria(pic, tagName1, tagValue1);
                boolean matchesSecondCriteria = (tagName2 != null && !tagName2.isEmpty()) && matchesCriteria(pic, tagName2, tagValue2);

                switch (searchType.toLowerCase()) {
                    case "single":
                        if (matchesFirstCriteria) results.add(pic);
                        break;
                    case "conjunctive":
                        if (matchesFirstCriteria && matchesSecondCriteria) results.add(pic);
                        break;
                    case "disjunctive":
                        if (matchesFirstCriteria || matchesSecondCriteria) results.add(pic);
                        break;
                }
            }
        }

        return results;
    }

    private boolean matchesCriteria(Picture pic, String tagName, String tagValue) {
        if (tagName == null || tagValue == null || tagName.isEmpty() || tagValue.isEmpty()) {
            return false;
        }

        for (Tag tag : pic.getTags()) {
            if (tag.getTagName().equalsIgnoreCase(tagName)) {
                // Assuming the tag object itself represents a tag-value
                // Compare the tag's string representation with the tagValue
                if (tag.toString().toLowerCase().contains(tagValue.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> getAutoCompleteSuggestions(String tagName, String startingSubstring) {
        List<String> suggestions = new ArrayList<>();
        Log.d("Debug", "tagName: " + tagName + ", startingSubstring: " + startingSubstring);
        if (tagName == null || startingSubstring == null || tagName.isEmpty() || startingSubstring.isEmpty()) {
            return suggestions;
        }

        for (Album album : allAlbums) {
            for (Picture pic : album.getPics()) {
                for (Tag tag : pic.getTags()) {
                    if (tag.getTagName().equalsIgnoreCase(tagName)) {
                        // Iterate through all tag values
                        for (String tagValue : tag.getAllTagValues()) {
                            // Check if the tag value starts with the provided substring
                            if (tagValue.toLowerCase().startsWith(startingSubstring.toLowerCase())) {
                                // If it matches, add the tagValue to suggestions
                                suggestions.add(tagValue);
                            }
                        }
                    }
                }
            }
        }

        return suggestions.stream().distinct().collect(Collectors.toList());
    }
}