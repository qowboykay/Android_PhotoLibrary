package com.example.android_photos;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class PictureViewActivity extends AppCompatActivity {
    private Button addTagButton;
    private ArrayList<Album> savedAlbums;
    private Button nextButton;
    private Button prevButton;
    private Button backButton;
    private Button moveButton;
    private TextView tagsTextView;
    private ImageView imageView;
    private Album selectedAlbum;
    private ArrayList<Picture> pictureList;
    private int currentPictureIndex;
    private Picture currentPicture;
    private String[] presetTagNames = {"Location", "Person"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_view_activity);
        selectedAlbum = (Album)getIntent().getSerializableExtra("selectedAlbum");
        savedAlbums = (ArrayList<Album>) getIntent().getSerializableExtra("savedAlbums");
        pictureList = selectedAlbum.returnPictures();
        if (pictureList == null || pictureList.isEmpty()) {
            pictureList = new ArrayList<>();
            Log.d("Debug", "Its empty");
            selectedAlbum.addPictureList(pictureList);
        }
        String selectedPictureUriString = getIntent().getStringExtra("selectedPictureUri");
        Uri selectedPictureUri = Uri.parse(selectedPictureUriString);
        // Set the URI to the ImageView
        imageView = findViewById(R.id.imageView);
        imageView.setImageURI(selectedPictureUri);
        if(pictureList.get(0).getPictureTags().isEmpty()) {
            Log.d("Debug", "Tag is empty");
        }
        tagsTextView = findViewById(R.id.tagsTextView);
        backButton = findViewById(R.id.backButton);
        moveButton = findViewById(R.id.moveButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        addTagButton = findViewById(R.id.addTagButton);

        updateDisplayedPicture();
        updateTagsTextView();

        backButton.setOnClickListener(v -> onBackButtonClicked());
        moveButton.setOnClickListener(v -> onMoveButtonClicked());
        prevButton.setOnClickListener(v -> onPrevButtonClicked());
        nextButton.setOnClickListener(v -> onNextButtonClicked());
        addTagButton.setOnClickListener(v -> onAddTagButtonClicked());

    }

    private void onBackButtonClicked(){
        setResultAndFinish();
    }
    private void onMoveButtonClicked(){

    }

    private void onPrevButtonClicked(){
        if (currentPictureIndex > 0) {
            currentPictureIndex--;
            updateTagsTextView();
            updateDisplayedPicture();
        }
    }

    private void onNextButtonClicked(){
        if (currentPictureIndex < pictureList.size() - 1) {
            currentPictureIndex++;
            updateTagsTextView();
            updateDisplayedPicture();
        }
    }

    private void onAddTagButtonClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_tag, null);
        builder.setView(dialogView);

        // Assuming you have a Spinner for preset tag names and an EditText for the tag value
        Spinner spinnerTagName = dialogView.findViewById(R.id.spinnerTagName);
        EditText editTagValue = dialogView.findViewById(R.id.editTagValue);

        // Set up the spinner with preset tag names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, presetTagNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTagName.setAdapter(adapter);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedTagName = (String) spinnerTagName.getSelectedItem();
                String tagValue = editTagValue.getText().toString().trim();

                if (!tagValue.isEmpty()) {
                    // Create a new Tag object and add it to the current picture
                    Tag newTag = new Tag(selectedTagName);
                    newTag.addTagValue(tagValue);
                    currentPicture = pictureList.get(currentPictureIndex);
                    currentPicture.addTag(newTag);
                    saveAlbumsToFile(savedAlbums);
                    // Update the tagsTextView to display the tags
                    updateTagsTextView();
                    if(savedAlbums == null){
                        Log.d("Debug", "saved albums is null");
                    }
                } else {
                    Toast.makeText(PictureViewActivity.this, "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void updateDisplayedPicture() {
        Uri selectedPictureUri = pictureList.get(currentPictureIndex).getUri();
        imageView.setImageURI(selectedPictureUri);
    }


    private void updateTagsTextView() {
        Picture currentPicture = pictureList.get(currentPictureIndex);
        ArrayList<Tag> tags = currentPicture.getPictureTags();
        StringBuilder tagsText = new StringBuilder();

        for (Tag tag : tags) {
            tagsText.append(tag.toString()).append("\n");
        }

        tagsTextView.setText(tagsText.toString());
    }



    protected void saveAlbumsToFile(ArrayList<Album> albums) {
        Log.d("SaveAlbums", "Saving albums to file");
        try {
            // Open a file for writing in internal storage
            FileOutputStream fileOutputStream = openFileOutput("albums.txt", Context.MODE_PRIVATE);

            // Create a stream to write data
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            // Write the entire ArrayList to the file
            objectOutputStream.writeObject(albums);

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

            // Read the entire ArrayList from the file
            ArrayList<Album> loadedAlbums = (ArrayList<Album>) objectInputStream.readObject();

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

    private void setResultAndFinish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedSelectedAlbum", selectedAlbum);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}