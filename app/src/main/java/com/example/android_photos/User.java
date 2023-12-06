package com.example.android_photos;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class User implements Parcelable {
    private String username;
    private final ArrayList<Album> albumList;

    /**
     * Constructs a new User with a specified username and password.
     *
     * @param username The username of the user.
     *
     */
    public User(String username){
        this.username = username;
        this.albumList = new ArrayList<>();
    }
    /**
     * Sets a new username for the user.
     *
     * @param username The new username to be set.
     */
    public void setNewUsername(String username){
        this.username = username;
    }
    /**
     * Retrieves the username of the user.
     *
     * @return The username.
     */
    public String getUsername(){
        return username;
    }
    /**
     * Adds an album to the user's collection.
     *
     * @param album The album to be added.
     */
    public void addAlbum(Album album){
        this.albumList.add(album);
    }
    /**
     * Gets the number of albums in the user's collection.
     *
     * @return The number of albums.
     */
    public int getNumberofAlbums(){
        return albumList.size();
    }
    /**
     * Retrieves a list of all albums in the user's collection.
     *
     * @return A list of albums.
     */
    public ArrayList<Album> getListOfUserAlbums(){
        return albumList;
    }

    @Override
    public String toString(){
        return username;
    }

    /**
     * Searches for photos by a single tag-value pair across all user's albums.
     *
     * @param tagName  The name of the tag.
     * @param tagValue The value of the tag.
     * @return A list of pictures that match the specified tag-value pair.
     */
    public List<Picture> searchPicturesByTag(String tagName, String tagValue) {
        return albumList.stream()
                .flatMap(album -> album.searchPicturesByTag(tagName, tagValue).stream())
                .collect(Collectors.toList());
    }
    /**
     * Searches for photos that match a conjunctive combination of two tag-value pairs across all albums.
     *
     * @param tag1   The first tag name.
     * @param value1 The value for the first tag.
     * @param tag2   The second tag name.
     * @param value2 The value for the second tag.
     * @return A list of pictures that match both tag-value pairs.
     */
    public List<Picture> searchPicturesByTagsConjunctive(String tag1, String value1, String tag2, String value2) {
        List<Picture> result = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();
        tags.put(tag1, value1);
        tags.put(tag2, value2);

        for (Album album : albumList) {
            result.addAll(album.searchPicturesByTagsConjunctive(tags));
        }
        return result;
    }
    /**
     * Searches for photos that match a disjunctive combination of two tag-value pairs across all albums.
     *
     * @param tag1   The first tag name.
     * @param value1 The value for the first tag.
     * @param tag2   The second tag name.
     * @param value2 The value for the second tag.
     * @return A list of pictures that match either of the tag-value pairs.
     */
    public List<Picture> searchPicturesByTagsDisjunctive(String tag1, String value1, String tag2, String value2) {
        List<Picture> result = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();
        tags.put(tag1, value1);
        tags.put(tag2, value2);

        for (Album album : albumList) {
            result.addAll(album.searchPicturesByTagsDisjunctive(tags));
        }
        return result;
    }


    protected User(Parcel in) {
        username = in.readString();
        albumList = in.createTypedArrayList(Album.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeTypedList(albumList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
