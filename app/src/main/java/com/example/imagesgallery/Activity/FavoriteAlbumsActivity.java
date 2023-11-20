package com.example.imagesgallery.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.GridView;

import com.example.imagesgallery.Adapter.AlbumAdapter;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.R;

import java.util.ArrayList;
import java.util.Objects;

public class FavoriteAlbumsActivity extends AppCompatActivity {

    GridView gridView;
    ArrayList<Album> albumArrayList;
    AlbumAdapter albumAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_albums);

        init();

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Favorite albums");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        albumArrayList = new ArrayList<>();
        albumAdapter = new AlbumAdapter(FavoriteAlbumsActivity.this,albumArrayList);
        gridView.setAdapter(albumAdapter);
    }

    private void init(){
        gridView = (GridView) findViewById(R.id.gridview_FavoriteAlbums);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }
}