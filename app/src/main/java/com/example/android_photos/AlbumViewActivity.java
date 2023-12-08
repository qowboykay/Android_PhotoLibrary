package com.example.android_photos;

import static java.lang.Long.parseLong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;

import java.util.ArrayList;

public class AlbumViewActivity extends AppCompatActivity {

    private static final int PICK_PICTURE_REQUEST = 1;
    private Button backButton;
    private Button deleteButton;
    private Button openButton;
    private Button searchButton;
    private Button addButton;
    private RecyclerView pictureRecyclerView;
    private Album selectedAlbum;
    private PictureAdapter pictureAdapter;
    private ArrayList<Picture> pictureList;
    private ArrayList<Album> savedAlbums;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);
        Intent intent = getIntent();
        selectedAlbum =(Album) intent.getSerializableExtra("selectedAlbum");
        savedAlbums = (ArrayList<Album>) intent.getSerializableExtra("savedAlbums");
        TextView albumNameTextView = findViewById(R.id.textViewAlbumView);
        albumNameTextView.setText("Album Name: " + selectedAlbum.getAlbumName());
        pictureRecyclerView = findViewById(R.id.pictureRecyclerView);
        pictureList = new ArrayList<>();
        selectedAlbum.addPictureList(pictureList);
        pictureAdapter = new PictureAdapter(pictureList);
        pictureRecyclerView.setAdapter(pictureAdapter);
        int spanCount = 3; // Number of columns in the grid
        pictureRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));


        backButton = findViewById(R.id.backButton);
        pictureRecyclerView= findViewById(R.id.pictureRecyclerView);
        deleteButton = findViewById(R.id.deleteButton);
        openButton = findViewById(R.id.openButton);
        searchButton = findViewById(R.id.searchButton);
        addButton = findViewById(R.id.addButton);

        backButton.setOnClickListener(v -> onBackButtonClicked());
        deleteButton.setOnClickListener(v -> onDeleteButtonClicked());
        openButton.setOnClickListener(v -> onOpenButtonClicked());
        addButton.setOnClickListener(v -> onAddButtonClicked());
        searchButton.setOnClickListener(v -> onSearchButtonClicked());
    }


    private void onBackButtonClicked() {
        finish();
    }

    private void onDeleteButtonClicked() {
        // Add code to handle delete button click
    }

    private void onOpenButtonClicked() {
        // Add code to handle recaption button click
    }

    private void onSearchButtonClicked() {
        // Add code to handle search button click
    }

    private void onAddButtonClicked() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_PICTURE_REQUEST);
        saveAlbums();


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PICTURE_REQUEST && resultCode == RESULT_OK) {
            // Handle the selected photo
            if (data != null) {
                Uri selectedImageUri = data.getData();
                String fileName = getFileName(selectedImageUri);
                Picture selectedPicture = null;
                try {
                    selectedPicture = new Picture(selectedImageUri, fileName , generateUniqueId());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // Add the picture to the album (update your album data structure)
                selectedAlbum.addPicture(selectedPicture);
                // Notify the adapter that the data set has changed
                pictureAdapter.notifyDataSetChanged();
            }
        }
    }


    private String generateUniqueId() {
        return UUID.randomUUID().toString();
    }
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void saveAlbums() {
        try {
            FileOutputStream fos = openFileOutput("saved_albums.ser", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(savedAlbums);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}