package com.example.android_photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import android.net.Uri;

public class Picture implements Serializable {

    private String caption;
    private ArrayList<Tag> tags;
    private transient Bitmap image;
    private Uri uri;
    private String id;
    static final long serialVersionUID = 1L;


    public Picture(Uri uri, String caption, String id) throws IOException {
        this.uri = uri;
        this.caption = caption;
        this.tags = new ArrayList<Tag>();
        this.id = id;
    }


    public Uri getUri() {
        return uri;
    }
    public String getId() {return id;}


    public String getCaption() {
        return caption;
    }


    public void recaptionPicture(String caption) {
        this.caption = caption;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }


    public ArrayList<Tag> getPictureTags() {
        return tags;
    }


    public boolean hasTag(String tagName, String tagValue) {
        // Using the correct method name from Tag.java
        return tags.stream().anyMatch(tag -> tag.getTagName().equals(tagName) && tag.getAllTagValues().contains(tagValue));
    }


    @Override
    public String toString() {
        return caption;
    }

    public Bitmap getImage() {
        return image;
    }


    public String getFileName() {
        return getFileNameFromUri(uri);
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri != null) {
            File file = new File(uri.getPath());
            fileName = file.getName();
        }
        return fileName;
    }
}
