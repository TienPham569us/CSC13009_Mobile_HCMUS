package com.example.imagesgallery.Activity;

import static android.os.Environment.MEDIA_MOUNTED;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.example.imagesgallery.Adapter.ChooseImageAdapter;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.util.ArrayList;
import java.util.Objects;

public class ChooseImageActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<Image> imageArrayList;
    ChooseImageAdapter chooseImageAdapter;
    Album album;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);

        init();
        album = (Album) getIntent().getSerializableExtra("album");
        imageArrayList = new ArrayList<>();
        chooseImageAdapter = new ChooseImageAdapter(ChooseImageActivity.this, imageArrayList);
        recyclerView.setLayoutManager(new GridLayoutManager(ChooseImageActivity.this, 3));
        recyclerView.setAdapter(chooseImageAdapter);
        loadImages();
        chooseImageAdapter.notifyDataSetChanged();

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Choose Image");

        // set return button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.listImageToAdd);
    }

    private void loadImages() {
        Cursor cursor = MainActivity.db.rawQuery("SELECT * FROM Image", null);
        while (cursor.moveToNext()) {
            int pathColumnIndex = cursor.getColumnIndex("path");
            int descriptionColumnIndex = cursor.getColumnIndex("description");
            int favorColumnIndex = cursor.getColumnIndex("isFavored");

            String path = cursor.getString(pathColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int isFavored = cursor.getInt(favorColumnIndex);
            imageArrayList.add(new Image(path, description, isFavored));
        }

        for (int i = 0; i < imageArrayList.size(); i++) {
            for (int j = 0; j < album.getListImage().size(); j++) {
                if (imageArrayList.get(i).getPath().equals(album.getListImage().get(j).getPath())){
                    imageArrayList.get(i).setCanAddToCurrentAlbum(false);
                }
            }
        }
        cursor.close();
    }
}