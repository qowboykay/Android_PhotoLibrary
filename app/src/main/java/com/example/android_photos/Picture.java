package com.example.android_photos;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

public class Picture implements Serializable {

    private String caption;
    private ArrayList<Tag> tags;
    private transient Bitmap imageBitmap;
    private transient Uri uri;
    private String id;
    static final long serialVersionUID = 1L;

    public Picture(Uri uri, String caption, String id, Bitmap imageBitmap) throws IOException {
        this.uri = uri;
        this.caption = caption;
        this.tags = new ArrayList<Tag>();
        this.id = id;
        this.imageBitmap = imageBitmap;
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

    public ArrayList<Tag> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return caption;
    }

    public Bitmap getImage() {
        return imageBitmap;
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
        out.writeUTF(uri.toString());

        // Convert the Bitmap to a byte array and write it to the ObjectOutputStream
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
        byte[] byteArray = byteStream.toByteArray();
        out.writeInt(byteArray.length);
        out.write(byteArray);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        uri = Uri.parse(in.readUTF());

        // Read the byte array and convert it back to a Bitmap
        int length = in.readInt();
        byte[] byteArray = new byte[length];
        in.readFully(byteArray);
        imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, length);
    }

    private String saveImageToFile(Uri sourceUri, String fileName, Context context) {
        OutputStream outputStream = null;

        try {
            // Create a folder for the images if it doesn't exist
            File imagesFolder = new File(context.getFilesDir(), "album_images");
            if (!imagesFolder.exists()) {
                imagesFolder.mkdir();
            }

            File outputFile = new File(imagesFolder, fileName);

            outputStream = new FileOutputStream(outputFile);

            // Use your preferred method to copy the content
            // This is just an example, you may want to adjust it based on your needs
            // Here, we're assuming the image is a bitmap, so we convert it to bytes
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), sourceUri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Handle the error accordingly
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

