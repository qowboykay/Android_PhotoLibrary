package com.example.android_photos;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ArrayAdapter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Album implements Parcelable {

        static final long serialVersionUID = 1L;
        private String albumName;
        private final ArrayList<Picture> pics;
        private ArrayAdapter<Album> albums;

        public Album(String albumName) {
            this.albumName = albumName;
            this.pics = new ArrayList<>();
        }

        public void setAlbum(String albumName) {
            this.albumName = albumName;

        }

        public void setAdapter(ArrayAdapter<Album> albums){
            this.albums = albums;
        }

        public ArrayAdapter<Album> getAdapter(){
            return albums;
        }

        public String getAlbumName() {
            return albumName;
        }

        /**
         * This method allows you to set your album name
         *
         * @param albumName
         */
        public void setAlbumName(String albumName) {
            this.albumName = albumName;
        }

        /**
         * This adds a picture to the specified album, a new picture must be initialized before adding.
         *
         * @param pic
         */
        public void addPicture(Picture pic) {
            this.pics.add(pic);
        }

        public void deletePicture(Picture pic) {
            pics.remove(pic);
        }

        /**
         * This method returns the list of pictures in an album as an ArrayList.
         *
         * @return
         */
        public ArrayList<Picture> returnPictures() {
            return pics;
        }

        /**
         * Searches for photos that match a specific tag name and value.
         *
         * @param tagName  The name of the tag to search for.
         * @param tagValue The value of the tag to match.
         * @return A list of {@link Picture} objects that have the specified tag name and value.
         */
        public List<Picture> searchPicturesByTag(String tagName, String tagValue) {
            return pics.stream()
                    .filter(picture -> picture.hasTag(tagName, tagValue))
                    .collect(Collectors.toList());
        }

        /**
         * Searches for photos that match all specified tags (conjunctive search).
         *
         * @param tags A map of tag names and values to search for. Photos must match all provided tags.
         * @return A list of {@link Picture} objects that match all the specified tags.
         */
        public List<Picture> searchPicturesByTagsConjunctive(Map<String, String> tags) {
            return pics.stream()
                    .filter(picture -> tags.entrySet().stream()
                            .allMatch(entry -> picture.hasTag(entry.getKey(), entry.getValue())))
                    .collect(Collectors.toList());
        }

        /**
         * Searches for photos that match any of the specified tags (disjunctive search).
         *
         * @param tags A map of tag names and values. Photos must match at least one of the provided tags.
         * @return A list of {@link Picture} objects that match any of the specified tags.
         */
        public List<Picture> searchPicturesByTagsDisjunctive(Map<String, String> tags) {
            return pics.stream()
                    .filter(picture -> tags.entrySet().stream()
                            .anyMatch(entry -> picture.hasTag(entry.getKey(), entry.getValue())))
                    .collect(Collectors.toList());
        }

        /**
         * Creates a new album from a list of search results.
         *
         * @param newAlbumName  The name for the new album.
         * @param searchResults The list of {@link Picture} objects to be added to the new album.
         * @return The newly created {@link Album} containing the search results.
         */
        public Album createAlbumFromSearchResults(String newAlbumName, List<Picture> searchResults) {
            Album newAlbum = new Album(newAlbumName);
            for (Picture pic : searchResults) {
                newAlbum.addPicture(pic);
            }
            return newAlbum;
        }

        @Override
        public String toString() {
            return albumName;
        }

    protected Album(Parcel in) {
        albumName = in.readString();
        pics = in.createTypedArrayList(Picture.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(albumName);
        dest.writeTypedList(pics);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}


