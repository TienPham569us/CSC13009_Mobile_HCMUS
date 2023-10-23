package com.example.imagesgallery.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.GridView;

import com.example.imagesgallery.Adapter.AlbumAdapter;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}