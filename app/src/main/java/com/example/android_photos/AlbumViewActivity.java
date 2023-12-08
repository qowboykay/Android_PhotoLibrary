package com.example.android_photos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class AlbumViewActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int REQUEST_IMAGE_GET = 2;
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
    private static final String IMAGES_FOLDER_NAME = "album_images";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);
        Intent intent = getIntent();
        selectedAlbum =(Album) intent.getSerializableExtra("selectedAlbum");
        savedAlbums = (ArrayList<Album>) intent.getSerializableExtra("savedAlbums");
        selectedAlbum = savedAlbums.stream()
                .filter(album -> album.getAlbumName().equals(selectedAlbum.getAlbumName()))
                .findFirst()
                .orElse(null);
        TextView albumNameTextView = findViewById(R.id.textViewAlbumView);
        albumNameTextView.setText("Album Name: " + selectedAlbum.getAlbumName());
        pictureRecyclerView = findViewById(R.id.pictureRecyclerView);
        pictureList = selectedAlbum.returnPictures();
        if (pictureList == null || pictureList.isEmpty()) {
            pictureList = new ArrayList<>();
            selectedAlbum.addPictureList(pictureList);
        }
        File imagesFolder = new File(getFilesDir(), IMAGES_FOLDER_NAME);
        if (!imagesFolder.exists()) {
            imagesFolder.mkdir();
        }
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
        // Update the selected album in savedAlbums
        savedAlbums.stream()
                .filter(album -> album.getAlbumName().equals(selectedAlbum.getAlbumName()))
                .findFirst()
                .ifPresent(album -> album.addPictureList(selectedAlbum.returnPictures()));

        List<Uri> allPictureUris = savedAlbums.stream()
                .flatMap(album -> album.returnPictures().stream())
                .map(Picture::getUri)
                .collect(Collectors.toList());

        Log.d("Debug","Saved Photos:"  + allPictureUris);
        setResultAndFinish();
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
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(photoPickerIntent, REQUEST_IMAGE_GET);
        }
    }


        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
                // Handle the selected photo
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    String fileName = getFileName(selectedImageUri);

                    // Save the image to the folder
                    saveImageToFile(selectedImageUri, fileName);

                    // Create a Picture object with the file URI
                    Picture selectedPicture = null;
                    try {
                        selectedPicture = new Picture(getImageFileUri(fileName), fileName, generateUniqueId());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    selectedAlbum.addPicture(selectedPicture);
                    pictureAdapter.notifyDataSetChanged();
                    saveAlbums();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedSavedAlbums", savedAlbums);
                    setResult(RESULT_OK, resultIntent);
                }
            }
        }

        private Uri getImageFileUri(String fileName) {
            File imageFile = new File(getFilesDir() + File.separator + IMAGES_FOLDER_NAME, fileName);
            return Uri.fromFile(imageFile);
        }
        private String generateUniqueId () {
            return UUID.randomUUID().toString();
        }
        private void saveImageToFile(Uri sourceUri, String fileName) {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = getContentResolver().openInputStream(sourceUri);
                File outputFile = new File(getFilesDir() + File.separator + IMAGES_FOLDER_NAME, fileName);
                outputStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                    if (outputStream != null) outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    private String getFileName (Uri uri){
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
        private void setResultAndFinish () {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updatedSavedAlbums", savedAlbums);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
        private void saveAlbums() {
            try {
                // Before saving
                FileOutputStream fos = openFileOutput("saved_albums.ser", Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                Log.d("Debug", "Saved Albums: " + savedAlbums.toString());
                oos.writeObject(savedAlbums);
                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    @SuppressWarnings("unchecked")
    private void loadAlbums() {
        try {
            FileInputStream fis = openFileInput("saved_albums.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            savedAlbums = (ArrayList<Album>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // If the savedAlbums list is null, initialize it
        if (savedAlbums == null) {
            savedAlbums = new ArrayList<>();
        }
    }

}
