package com.example.android_photos;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import android.net.Uri;

public class Picture implements Serializable {

    private String caption;
    private ArrayList<Tag> tags;
    private transient Bitmap image;
    private transient Uri uri;
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
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        // Serialize the necessary fields of Uri
        out.writeUTF(uri.toString());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Deserialize the necessary fields of Uri
        uri = Uri.parse(in.readUTF());
    }
}

