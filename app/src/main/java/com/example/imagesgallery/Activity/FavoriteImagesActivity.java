package com.example.imagesgallery.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.example.imagesgallery.R;

import java.util.Objects;

public class FavoriteImagesActivity extends AppCompatActivity {

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_images);

        init();

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Favorite images");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void init(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }
}