package com.example.android_photos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private Button backButton, searchButton;
    private AutoCompleteTextView searchTagEditText1, searchTagEditText2;
    private RecyclerView searchResultsRecyclerView;
    private RadioGroup searchTypeRadioGroup;
    private ArrayList<Album> savedAlbums;
    private ArrayAdapter<String> autoCompleteAdapter1, autoCompleteAdapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        savedAlbums = (ArrayList<Album>) getIntent().getSerializableExtra("savedAlbums");
        for (Album album : savedAlbums) {
            Log.d("Album", "Album: " + album.getAlbumName());

            for (Picture picture : album.getPics()) {
                Log.d("Photo", "  Photo: " + picture.getFileName());

                for (Tag tag : picture.getTags()) {
                    Log.d("Tag", "    Tag: " + tag.getTagName() + ", Values: " + tag.getAllTagValues());
                }
            }

            Log.d("Divider", ""); // Add a log with an empty message to act as a divider between albums for clarity
        }
        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.searchButton);
        searchTagEditText1 = findViewById(R.id.searchTagEditText1);
        searchTagEditText2 = findViewById(R.id.searchTagEditText2);
        searchTypeRadioGroup = findViewById(R.id.searchTypeRadioGroup);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        autoCompleteAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        searchTagEditText1.setAdapter(autoCompleteAdapter1);
        searchTagEditText2.setAdapter(autoCompleteAdapter2);

        backButton.setOnClickListener(v -> finish());
        searchButton.setOnClickListener(v -> performSearch());

        setupAutoCompleteListeners();
    }

    private void setupAutoCompleteListeners() {
        searchTagEditText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateAutoCompleteSuggestions(s.toString(), autoCompleteAdapter1, "Location");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchTagEditText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateAutoCompleteSuggestions(s.toString(), autoCompleteAdapter2, "Person");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateAutoCompleteSuggestions(String input, ArrayAdapter<String> adapter, String tagType) {
        if (input.isEmpty()) {
            adapter.clear();
            return;
        }
        Search search = new Search(savedAlbums);
        List<String> suggestions = search.getAutoCompleteSuggestions(tagType, input);

        adapter.clear();
        adapter.addAll(suggestions);
        adapter.notifyDataSetChanged();
    }
    private void performSearch() {
        String tagValue1 = searchTagEditText1.getText().toString().trim();
        String tagValue2 = searchTagEditText2.getText().toString().trim();

        int selectedSearchType = searchTypeRadioGroup.getCheckedRadioButtonId();
        String searchType = (selectedSearchType == R.id.radioConjunctive) ? "conjunctive" : "disjunctive";

        if (tagValue1.isEmpty() && tagValue2.isEmpty()) {
            Toast.makeText(this, "Please enter at least one tag to search for", Toast.LENGTH_SHORT).show();
            return;
        }

        Search search = new Search(savedAlbums);
        List<Picture> searchResults = search.searchPhotos(searchType, "Location", tagValue1, "Person", tagValue2);

        PictureAdapter adapter = new PictureAdapter(searchResults);
        searchResultsRecyclerView.setAdapter(adapter);
    }

}
