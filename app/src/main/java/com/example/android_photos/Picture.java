package com.example.android_photos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Picture implements Parcelable {

    private String picturePath;
    private String caption;
    private ArrayList<Tag> tags;
    private transient Bitmap image;

    /**
     * Constructs a new Picture object with specified path and caption.
     *
     * @param picturePath The file path of the picture.
     * @param caption     The caption of the picture.
     * @throws IOException If there is an issue loading the picture from the path.
     */
    public Picture(String picturePath, String caption) throws IOException {
        this.picturePath = picturePath;
        this.caption = caption;
        this.tags = new ArrayList<Tag>();
        File file = new File(picturePath);
        //Add method to load picture
    }


    public String getPicturePath() {
        return picturePath;
    }


    public String getCaption() {
        return caption;
    }


    public void recaptionPhoto(String caption) {
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

    protected Picture(Parcel in) {
        picturePath = in.readString();
        caption = in.readString();
        tags = in.createTypedArrayList(Tag.CREATOR);
        byte[] byteArray = in.createByteArray();
        image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(picturePath);
        dest.writeString(caption);
        dest.writeTypedList(tags);

        // Convert Bitmap to byte array for serialization
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        dest.writeByteArray(byteArray);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Picture> CREATOR = new Creator<Picture>() {
        @Override
        public Picture createFromParcel(Parcel in) {
            return new Picture(in);
        }

        @Override
        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };


}
