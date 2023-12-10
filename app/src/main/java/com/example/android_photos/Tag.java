package com.example.android_photos;


import java.io.Serializable;
import java.util.ArrayList;

public class Tag implements Serializable {

    private String tagName;
    private ArrayList<String> tagValues;
    static final long serialVersionUID = 1L;

    /**
     * Constructs a new Tag with the specified name.
     *
     * @param tagName The name of the tag.
     */
    public Tag(String tagName){
        this.tagName = tagName;
        this.tagValues = new ArrayList<>();
    }

    /**
     * Retrieves the name of the tag.
     *
     * @return The name of the tag.
     */
    public String getTagName(){
        return tagName;
    }
    /**
     * Adds a new value to the tag.
     *
     * @param tagValue The value to be added to the tag.
     */
    public void addTagValue(String tagValue){
        tagValues.add(tagValue);
    }
    /**
     * Retrieves all values associated with the tag.
     *
     * @return A list of values for the tag.
     */
    public ArrayList<String> getAllTagValues(){
        return tagValues;
    }
    /**
     * Removes a specific value from the tag.
     *
     * @param tagValue The value to be removed from the tag.
     */
    public void removeTagValue(String tagValue){
        tagValues.removeIf(element -> element.equals(tagValue));

    }
    /**
     * Returns a string representation of the tag, including its name and all values.
     *
     * @return A string representation of the tag.
     */
    @Override
    public String toString() {
        return tagName + ": " + String.join(", ", tagValues);
    }
}
