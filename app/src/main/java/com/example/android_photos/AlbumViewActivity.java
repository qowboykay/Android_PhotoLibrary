package com.example.android_photos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
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
    private static final int pictureViewRequestCode = 000;

    private Button backButton;
    private Button deleteButton;
    private Button openButton;
    private Button searchButton;
    private Button addButton;
    private Button moveButton;
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
        moveButton = findViewById(R.id.moveButton);

        backButton.setOnClickListener(v -> onBackButtonClicked());
        deleteButton.setOnClickListener(v -> onDeleteButtonClicked());
        openButton.setOnClickListener(v -> onOpenButtonClicked());
        addButton.setOnClickListener(v -> onAddButtonClicked());
        searchButton.setOnClickListener(v -> onSearchButtonClicked());
        moveButton.setOnClickListener(v -> onMoveButtonClicked());
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
        showToast("Returning to Albums List");
        setResultAndFinish();
    }

    private void onDeleteButtonClicked() {
        if(selectedPicture != null) {
            List<Integer> selectedPositions = pictureAdapter.getSelectedPositions();
            for (int i = selectedPositions.size() - 1; i >= 0; i--) {
                int position = selectedPositions.get(i);
                pictureList.remove(position);
                pictureAdapter.notifyItemRemoved(position);
            }
            selectedAlbum.addPictureList(pictureList);
            pictureAdapter.clearSelection();
            saveAlbumsToFile(savedAlbums);
            showToast("Successfully Deleted!");
        }else{
            showToast("Please select a picture to delete first");
        }
    }

    private void onOpenButtonClicked() {
        if(selectedPicture != null) {
            Intent intent = new Intent(this, PictureViewActivity.class);
            intent.putExtra("selectedPictureUri", selectedPicture.getUri().toString());
            intent.putExtra("selectedAlbum", selectedAlbum);
            showToast("Successfully Opened!");
            startActivityForResult(intent, pictureViewRequestCode); // Use a unique request code
        }else{
            showToast("Please select a picture to open display screen");
        }
    }


    public void onSearchButtonClicked() {
            Intent intent = new Intent(this, SearchActivity.class);
            // Since ArrayList<Album> is Serializable, we can pass it directly as an extra
            intent.putExtra("savedAlbums", savedAlbums);
            showToast("Opening Search!");
            startActivity(intent);

    }


    private void onAddButtonClicked() {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            Log.d("Debug", "passed permission");
            if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(photoPickerIntent, REQUEST_IMAGE_GET);
            }
    }
    private void onMoveButtonClicked(){
        if(selectedPicture != null) {
            showMoveToAlbumDialog();
        }else{
            showToast("Please select a picture before moving");
        }

    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == pictureViewRequestCode && resultCode == RESULT_OK) {
            if (data != null) {
                // Handle the updated selectedAlbum from the PictureViewActivity
                Album newSelectedAlbum = (Album) data.getSerializableExtra("updatedSelectedAlbum");
                selectedAlbum.setAlbum(newSelectedAlbum);
                pictureList = newSelectedAlbum.returnPictures();
                pictureAdapter = new PictureAdapter(pictureList);
                pictureAdapter.setOnItemClickListener(this);
                pictureRecyclerView.setAdapter(pictureAdapter);
                // Save all albums to file
                saveAlbumsToFile(savedAlbums);
            }
        }
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            if (data != null) {
                if(data.hasExtra("updatedSavedAlbums")) {
                    savedAlbums = (ArrayList<Album>) data.getSerializableExtra("updatedSavedAlbums");
                    saveAlbumsToFile(savedAlbums);
                    Log.d("Debug","we made it");
                }
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
                    showToast("Successfully Added!");
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
                    // Log album details for debugging
                    Log.d("SaveAlbums", "Album: " + album.getAlbumName());

                    for (Picture picture : album.getPics()) {
                        Log.d("SaveAlbums", "  Photo: " + picture.getFileName());

                        for (Tag tag : picture.getTags()) {
                            Log.d("SaveAlbums", "    Tag: " + tag.getTagName() + ", Values: " + tag.getAllTagValues());
                        }
                    }
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

    private void showMoveToAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Move Picture To Album");

        // Get the list of album names from savedAlbums
        ArrayList<String> albumNames = new ArrayList<>();
        for (Album album : savedAlbums) {
            albumNames.add(album.getAlbumName());
        }

        // Convert the ArrayList to a string array
        final String[] albumArray = albumNames.toArray(new String[0]);

        builder.setItems(albumArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Move the picture to the selected album
                Album destinationAlbum = savedAlbums.get(which);
                movePictureToAlbum(destinationAlbum);
                showToast("Picture has been moved!");

                // Save the updated albums
                saveAlbumsToFile(savedAlbums);

                // Update the displayed picture and pictureAdapter
                updateDisplayedPicture();
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateDisplayedPicture() {
        // Get the latest list of pictures from the selected album
        pictureList = selectedAlbum.returnPictures();
        pictureAdapter.setPictureList(pictureList);

        // Notify the adapter that the data has changed
        pictureAdapter.notifyDataSetChanged();
    }

    private void movePictureToAlbum(Album destinationAlbum) {
        // Remove the picture from the current album
        selectedAlbum.removePicture(selectedPicture);

        // Add the picture to the destination album
        destinationAlbum.addPicture(selectedPicture);
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
