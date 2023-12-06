package com.example.android_photos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_photos.Album;
import com.example.android_photos.User;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AlbumList extends AppCompatActivity {

    private ListView albumListView;
    private ArrayAdapter<Album> allAlbums;
    private EditText albumNameField;
    private Button createAlbumButton;
    private Button deleteAlbumButton;
    private Button renameAlbumButton;
    private Button openAlbumButton;
    private User currentUser;
    private Album selectedAlbum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);

        // Initialize UI components
        albumListView = findViewById(R.id.albumListView);
        albumNameField = findViewById(R.id.albumNameField);
        createAlbumButton = findViewById(R.id.createAlbumButton);
        deleteAlbumButton = findViewById(R.id.deleteAlbumButton);
        renameAlbumButton = findViewById(R.id.renameAlbumButton);
        openAlbumButton = findViewById(R.id.openAlbumButton);

        // Retrieve the current user from UserManager
        UserManager userManager = UserManager.getInstance();
        currentUser = userManager.getCurrentUser();

        if (currentUser == null) {
            User newUser = new User("default user");
            userManager.setCurrentUser(newUser);
            currentUser = userManager.getCurrentUser();
        }
        // Set click listener for album selection
        allAlbums = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currentUser.getListOfUserAlbums());
        albumListView.setAdapter(allAlbums);

            albumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedAlbum = (Album) parent.getItemAtPosition(position);

                    // Now you have the selected album, you can implement your logic here.
                    // For example, you can display album details, navigate to another activity, etc.
                    Toast.makeText(AlbumList.this, "Selected Album: " + selectedAlbum.getAlbumName(), Toast.LENGTH_SHORT).show();
                }
            });

        // Set click listeners for buttons
        createAlbumButton.setOnClickListener(v -> onCreateAlbumButtonClicked());
        deleteAlbumButton.setOnClickListener(v -> onDeleteAlbumButtonClicked());
        renameAlbumButton.setOnClickListener(v -> onRenameAlbumButtonClicked());
        openAlbumButton.setOnClickListener(v -> onOpenAlbumButtonClicked());
    }

    private void onCreateAlbumButtonClicked() {
        String albumName = albumNameField.getText().toString();
        boolean albumExists = albumExists(albumName);
        if(!albumName.isEmpty()){
            if(!albumExists){
                Album newAlbum = new Album(albumName);
                currentUser.addAlbum(newAlbum);
                allAlbums.notifyDataSetChanged();
                Toast.makeText(this, "Success!",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "An album with that name already exists", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Please enter an album name to create a new album.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onDeleteAlbumButtonClicked() {
        if (selectedAlbum != null) {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to delete this album?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        currentUser.getListOfUserAlbums().remove(selectedAlbum);
                        allAlbums.remove(selectedAlbum);
                        allAlbums.notifyDataSetChanged();
                        Toast.makeText(this, "Successfully Deleted!",Toast.LENGTH_SHORT).show();
                        selectedAlbum = null;
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            // Handle the case where no album is selected
            Toast.makeText(this, "Please select an album to delete", Toast.LENGTH_SHORT).show();
        }
    }


    private void onRenameAlbumButtonClicked() {
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


    private void onOpenAlbumButtonClicked() {
        // Implement open album logic
    }

    public boolean albumExists(String albumName){
        List<Album> currentAlbums = IntStream.range(0, allAlbums.getCount())
                .mapToObj(allAlbums::getItem)
                .collect(Collectors.toList());
        return currentAlbums.stream().anyMatch(album -> album.getAlbumName().equals(albumName));
    }
}