package com.example.android_photos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
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

public class AlbumViewActivity extends AppCompatActivity implements PictureAdapter.OnItemClickListener {
    private static final int PERMISSION_REQUEST_CODE = 998;
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
    private Picture selectedPicture;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);
        loadAlbumsFromFile();
        Intent intent = getIntent();
        selectedAlbum =(Album) intent.getSerializableExtra("selectedAlbum");
        savedAlbums = (ArrayList<Album>) intent.getSerializableExtra("savedAlbums");
        saveAlbumsToFile(savedAlbums);
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
            Log.d("Debug", "Its empty");
            selectedAlbum.addPictureList(pictureList);
        }
        File imagesFolder = new File(getFilesDir(), IMAGES_FOLDER_NAME);
        if (!imagesFolder.exists()) {
            imagesFolder.mkdir();
        }
        pictureAdapter = new PictureAdapter(pictureList);
        pictureAdapter.setOnItemClickListener(this);
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
        List<Integer> selectedPositions = pictureAdapter.getSelectedPositions();
        for (int i = selectedPositions.size() - 1; i >= 0; i--) {
            int position = selectedPositions.get(i);
            pictureList.remove(position);
            pictureAdapter.notifyItemRemoved(position);
        }
        selectedAlbum.addPictureList(pictureList);
        pictureAdapter.clearSelection();
        saveAlbumsToFile(savedAlbums);
    }

    private void onOpenButtonClicked() {
        Intent intent = new Intent(this, PictureViewActivity.class);
        intent.putExtra("selectedPictureUri", selectedPicture.getUri().toString());
        intent.putExtra("selectedAlbum", selectedAlbum);
        startActivity(intent);
    }

    private void onSearchButtonClicked() {
        // Add code to handle search button click
    }

    private void onAddButtonClicked() {
        Log.d("Debug", "Add button clicked");
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            Log.d("Debug", "passed permission");
            if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(photoPickerIntent, REQUEST_IMAGE_GET);
            }
    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            // Handle the selected photo
            if (data != null) {
                Uri selectedImageUri = data.getData();
                String fileName = getFileName(selectedImageUri);

                // Create a Bitmap from the selected image
                Bitmap selectedBitmap = null;
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Create a Picture object with the Bitmap
                if (selectedBitmap != null) {
                    Picture selectedPicture = null;
                    try {
                        saveImageToFile(selectedImageUri, fileName);
                        selectedPicture = new Picture(getImageFileUri(fileName),fileName,generateUniqueId(),selectedBitmap);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    selectedAlbum.addPicture(selectedPicture);
                    pictureAdapter.notifyDataSetChanged();
                    saveAlbumsToFile(savedAlbums);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedSavedAlbums", savedAlbums);
                    setResult(RESULT_OK, resultIntent);
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions are granted, proceed with your app logic
                // ...
            } else {
                // Some permissions were not granted
                // Handle this case, possibly show a message to the user
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
                // Add other necessary permissions
        };

        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, add it to the list of permissions to request
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            // Convert the list to an array and request the permissions on the UI thread
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);

            runOnUiThread(() -> {
                ActivityCompat.requestPermissions(this, permissionsArray, PERMISSION_REQUEST_CODE);
            });

            return false;  // Permissions are not granted yet
        } else {
            // All permissions are already granted
            // Continue with your app logic here
            return true;  // Permissions are granted
        }
    }
    @Override
    public void onItemClick(int position) {
        selectedPicture = pictureList.get(position);
        pictureAdapter.toggleSelection(position);
    }
}
