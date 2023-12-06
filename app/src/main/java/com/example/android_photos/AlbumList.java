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

import java.util.Objects;

public class AlbumList extends AppCompatActivity {

    private ListView albumListView;
    private EditText albumNameField;
    private Button createAlbumButton;
    private Button deleteAlbumButton;
    private Button renameAlbumButton;
    private Button openAlbumButton;
    private User currentUser;

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
        ArrayAdapter<Album> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currentUser.getListOfUserAlbums());
        albumListView.setAdapter(adapter);

        // Set click listener for album selection
        albumListView.setOnItemClickListener((parent, view, position, id) -> {
            Album selectedAlbum = (Album) parent.getItemAtPosition(position);
            // Handle album selection if needed
        });

        // Set click listeners for buttons
        createAlbumButton.setOnClickListener(v -> onCreateAlbumButtonClicked());
        deleteAlbumButton.setOnClickListener(v -> onDeleteAlbumButtonClicked());
        renameAlbumButton.setOnClickListener(v -> onRenameAlbumButtonClicked());
        openAlbumButton.setOnClickListener(v -> onOpenAlbumButtonClicked());
    }

    private void onCreateAlbumButtonClicked() {
        // Implement create album logic
    }

    private void onDeleteAlbumButtonClicked() {
        // Implement delete album logic
    }

    private void onRenameAlbumButtonClicked() {
        // Implement rename album logic
    }

    private void onOpenAlbumButtonClicked() {
        // Implement open album logic
    }
}