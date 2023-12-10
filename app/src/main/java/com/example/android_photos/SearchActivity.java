package com.example.android_photos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private Button backButton;
    private EditText searchTagEditText;
    private RecyclerView searchResultsRecyclerView;
    private ArrayList<Album> savedAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        // Retrieve the album list passed from AlbumViewActivity
        savedAlbums = (ArrayList<Album>) getIntent().getSerializableExtra("savedAlbums");

        backButton = findViewById(R.id.backButton);
        searchTagEditText = findViewById(R.id.searchTagEditText);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button searchButton = findViewById(R.id.searchButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close this activity
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
    }

    private void performSearch() {
        String tagValue = searchTagEditText.getText().toString().trim();

        if (tagValue.isEmpty()) {
            Toast.makeText(this, "Please enter a tag to search for", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use savedAlbums to perform the search
        Search search = new Search(savedAlbums);
        List<Picture> searchResults = search.searchByTag("Location", tagValue);

        // Update the RecyclerView with the search results
        PictureAdapter adapter = new PictureAdapter(searchResults);
        searchResultsRecyclerView.setAdapter(adapter);
    }
}
