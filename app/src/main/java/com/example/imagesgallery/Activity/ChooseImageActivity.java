package com.example.imagesgallery.Activity;

import static android.os.Environment.MEDIA_MOUNTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.example.imagesgallery.Adapter.ChooseImageAdapter;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ChooseImageActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<Image> imageArrayList;
    ChooseImageAdapter chooseImageAdapter;
    Album album;
    boolean isLoading = false, isAllItemsLoaded = false;
    private int CurrentMaxPosition = 0;
    private final int ItemsPerLoading = 21;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);

        init();

        isAllItemsLoaded = false;
        CurrentMaxPosition = 0;

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

        // load first images
        loadImages();

        // set return button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && gridLayoutManager != null && gridLayoutManager.findLastCompletelyVisibleItemPosition() >= imageArrayList.size() - 1 && !isAllItemsLoaded) {
                    isLoading = true;
                    // Create an executor that executes tasks in the main thread and background thread
                    Executor mainExecutor = ContextCompat.getMainExecutor(ChooseImageActivity.this);
                    ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
                    // Load data in the background thread.
                    backgroundExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            loadImages();
                            // Update list images in a album on the main thread
                            mainExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    chooseImageAdapter.notifyDataSetChanged();
                                    isLoading = false;
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.listImageToAdd);
    }

    private void loadImages() {
        Log.d("aaaaa", "before: " + imageArrayList.size());

        String sql = "SELECT * FROM Image LIMIT ? OFFSET ?";
        String[] args = {String.valueOf(ItemsPerLoading), String.valueOf(CurrentMaxPosition)};
        Cursor cursor = null;
        try {
            cursor = MainActivity.db.rawQuery(sql, args);
        } catch (Exception exception) {
            Log.d("aaaa",exception.getMessage().toString());
            return;
        }

        if (!cursor.moveToFirst()) {
            isAllItemsLoaded = true;
        }
        cursor.moveToPosition(-1);

        while (cursor.moveToNext()) {
            int pathColumnIndex = cursor.getColumnIndex("path");
            int descriptionColumnIndex = cursor.getColumnIndex("description");
            int favorColumnIndex = cursor.getColumnIndex("isFavored");

            String path = cursor.getString(pathColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int isFavored = cursor.getInt(favorColumnIndex);
            imageArrayList.add(new Image(path, description, isFavored));
        }

        int action = getIntent().getIntExtra("action", 0);
        if (action == AlbumInfoActivity.ACTION_ADD_IMAGE) {
            for (int i = 0; i < imageArrayList.size(); i++) {
                for (int j = 0; j < album.getListImage().size(); j++) {
                    if (imageArrayList.get(i).getPath().equals(album.getListImage().get(j).getPath())) {
                        imageArrayList.get(i).setCanAddToCurrentAlbum(false);
                    }
                }
            }
        }
        cursor.close();
        CurrentMaxPosition += ItemsPerLoading;
        Log.d("aaaaa", "after: " + imageArrayList.size());
    }
}