package com.example.android_photos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Search {

    private List<Album> allAlbums;

    public Search(List<Album> allAlbums) {
        this.allAlbums = allAlbums;
    }

    /**
     * Search for photos across all albums by tag-value pairs.
     * @param tagName Type of the tag (person or location).
     * @param value The value to search for.
     * @return A list of pictures that match the search criteria.
     */
    public List<Picture> searchByTag(String tagName, String value) {
        List<Picture> matchedPictures = new ArrayList<>();
        for (Album album : allAlbums) {
            for (Picture picture : album.getPics()) {
                for (Tag tag : picture.getTags()) {
                    if (tag.getTagName().equalsIgnoreCase(tagName)) {
                        for (String tagValue : tag.getAllTagValues()) {
                            if (tagValue.toLowerCase(Locale.ROOT).startsWith(value.toLowerCase(Locale.ROOT))) {
                                matchedPictures.add(picture);
                                break; // Break after finding the first matching value for this tag
                            }
                        }
                    }
                }
            }
        }
        return matchedPictures;
    }


    /**
     * Auto-complete suggestion for the search input.
     * @param tagName Type of the tag (person or location).
     * @param input The input text to match.
     * @return A list of suggested completions.
     */
    public List<String> autoComplete(String tagName, String input) {
        List<String> suggestions = new ArrayList<>();
        for (Album album : allAlbums) {
            for (Picture picture : album.getPics()) {
                for (Tag tag : picture.getTags()) {
                    if (tag.getTagName().equalsIgnoreCase(tagName)) {
                        for (String tagValue : tag.getAllTagValues()) {
                            if (tagValue.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT))) {
                                if (!suggestions.contains(tagValue)) {
                                    suggestions.add(tagValue);
                                }
                            }
                        }
                    }
                }
            }
        }
        return suggestions;
    }
}
