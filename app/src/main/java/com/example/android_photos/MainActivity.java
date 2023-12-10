package com.example.android_photos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeApp();
    }



    private void initializeApp() {
        Intent intent = new Intent(this, AlbumListActivity.class);
        startActivity(intent);
    }



}
