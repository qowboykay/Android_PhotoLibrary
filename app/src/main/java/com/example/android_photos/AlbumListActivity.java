package com.example.android_photos;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AlbumListActivity extends AppCompatActivity {

    private ArrayAdapter<Album> allAlbums;
    private EditText albumNameField;
    private Album selectedAlbum;
    protected ArrayList<Album> savedAlbums;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);

        // Initialize UI components
        ListView albumListView = findViewById(R.id.albumListView);
        albumNameField = findViewById(R.id.albumNameField);
        Button createAlbumButton = findViewById(R.id.createAlbumButton);
        Button deleteAlbumButton = findViewById(R.id.deleteAlbumButton);
        Button renameAlbumButton = findViewById(R.id.renameAlbumButton);
        Button openAlbumButton = findViewById(R.id.openAlbumButton);
        if(loadAlbumsFromFile() == null) {
            savedAlbums = new ArrayList<>();
            Album defaultAlbum = new Album("defaultAlbum");
            savedAlbums.add(defaultAlbum);
        }
        else{
            savedAlbums = loadAlbumsFromFile();
        }
            // Set click listener for album selection
            allAlbums = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedAlbums);
            albumListView.setAdapter(allAlbums);

            albumListView.setOnItemClickListener((parent, view, position, id) -> {
                selectedAlbum = (Album) parent.getItemAtPosition(position);

                // Now you have the selected album, you can implement your logic here.
                // For example, you can display album details, navigate to another activity, etc.
                Toast.makeText(AlbumListActivity.this, "Selected Album: " + selectedAlbum.getAlbumName(), Toast.LENGTH_SHORT).show();
            });

            // Set click listeners for buttons
            createAlbumButton.setOnClickListener(v -> onCreateAlbumButtonClicked());
            deleteAlbumButton.setOnClickListener(v -> onDeleteAlbumButtonClicked());
            renameAlbumButton.setOnClickListener(v -> onRenameAlbumButtonClicked());
            openAlbumButton.setOnClickListener(v -> onOpenAlbumButtonClicked());
        }

        private void onCreateAlbumButtonClicked () {
            String albumName = albumNameField.getText().toString();
            boolean albumExists = albumExists(albumName);
            if (!albumName.isEmpty()) {
                if (!albumExists) {
                    Album newAlbum = new Album(albumName);
                    savedAlbums.add(newAlbum);
                    allAlbums.notifyDataSetChanged();
                    saveAlbumsToFile(savedAlbums);
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "An album with that name already exists", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter an album name to create a new album.", Toast.LENGTH_SHORT).show();
            }
        }

        protected void onDeleteAlbumButtonClicked () {
            if (selectedAlbum != null) {
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to delete this album?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            savedAlbums.remove(selectedAlbum);
                            allAlbums.remove(selectedAlbum);
                            allAlbums.notifyDataSetChanged();
                            saveAlbumsToFile(savedAlbums);
                            Toast.makeText(this, "Successfully Deleted!", Toast.LENGTH_SHORT).show();
                            selectedAlbum = null;
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                // Handle the case where no album is selected
                Toast.makeText(this, "Please select an album to delete", Toast.LENGTH_SHORT).show();
            }
        }


        private void onRenameAlbumButtonClicked () {
            String newAlbumName = albumNameField.getText().toString();
            if (selectedAlbum != null) {
                if (!albumExists(newAlbumName)) {
                    new AlertDialog.Builder(this)
                            .setMessage("Are you sure you want to rename this album?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                selectedAlbum.setAlbumName(newAlbumName);
                                allAlbums.notifyDataSetChanged();
                                Toast.makeText(this, "Successfully Renamed!", Toast.LENGTH_SHORT).show();
                                selectedAlbum = null;
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    Toast.makeText(this, "An album with that name already exists", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the case where no album is selected
                Toast.makeText(this, "Please select an album to rename and fill in the name field", Toast.LENGTH_SHORT).show();
            }
        }


        private void onOpenAlbumButtonClicked () {

            if (selectedAlbum != null) {
                // Create an Intent to start AlbumViewActivity
                Intent intent = new Intent(this, AlbumViewActivity.class);
                intent.putExtra("albums", (Serializable) savedAlbums);
                // Pass the selected album to AlbumViewActivity
                intent.putExtra("selectedAlbum", selectedAlbum);

                // Start AlbumViewActivity
                startActivity(intent);
                selectedAlbum = null;
            } else {
                // Handle the case where no album is selected
                Toast.makeText(this, "Please select an album to open.", Toast.LENGTH_SHORT).show();
            }
        }


        public boolean albumExists (String albumName){
            List<Album> currentAlbums = IntStream.range(0, allAlbums.getCount())
                    .mapToObj(allAlbums::getItem)
                    .collect(Collectors.toList());
            return currentAlbums.stream().anyMatch(album -> album.getAlbumName().equals(albumName));
        }
    protected void saveAlbumsToFile(ArrayList<Album> albums) {
        Log.d("SaveAlbums", "Saving albums to file");
        try {
            // Open a file for writing in internal storage
            FileOutputStream fileOutputStream = openFileOutput("albums.txt", Context.MODE_PRIVATE);

            // Create a stream to write data
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            // Write the size of the ArrayList
            objectOutputStream.writeInt(albums.size());

            // Write each Parcelable object to the file
            for (Album album : albums) {
                objectOutputStream.writeObject(album);
            }

            // Close the streams
            objectOutputStream.close();
            fileOutputStream.close();
            Log.d("SaveAlbums", "File saved to: " + getFilesDir() + "/albums.txt");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SaveAlbums", "Error saving albums to file: " + e.getMessage());
        }
    }
    protected ArrayList<Album> loadAlbumsFromFile() {
        Log.d("LoadAlbums", "Loading albums from file");
        try {
            // Open the file for reading from internal storage
            FileInputStream fileInputStream = openFileInput("albums.txt");

            // Create a stream to read data
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            // Read the size of the ArrayList
            int size = objectInputStream.readInt();

            // Read each Parcelable object from the file
            ArrayList<Album> loadedAlbums = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                Album album = (Album) objectInputStream.readObject();
                if (album != null) {
                    loadedAlbums.add(album);
                }
            }

            // Close the streams
            objectInputStream.close();
            fileInputStream.close();
            Log.d("LoadAlbums", "File loaded from: " + getFilesDir() + "/albums.txt");
            return loadedAlbums;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.e("LoadAlbums", "Error loading albums from file: " + e.getMessage());
            return null;
        }
    }

    }