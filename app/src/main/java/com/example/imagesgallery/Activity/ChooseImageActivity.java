package com.example.imagesgallery.Activity;

import static android.os.Environment.MEDIA_MOUNTED;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.example.imagesgallery.Adapter.ChooseImageAdapter;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.util.ArrayList;
import java.util.Objects;

public class ChooseImageActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<Image> imageArrayList;
    ChooseImageAdapter chooseImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);

        init();
        imageArrayList = new ArrayList<>();
        chooseImageAdapter = new ChooseImageAdapter(ChooseImageActivity.this, imageArrayList);
        recyclerView.setLayoutManager(new GridLayoutManager(ChooseImageActivity.this, 3));
        recyclerView.setAdapter(chooseImageAdapter);
        loadImages();
        chooseImageAdapter.notifyDataSetChanged();

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("");

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
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, order);
            cursor.moveToPosition(-1);
//            ContentValues rowValues= new ContentValues();
            while (cursor.moveToNext()) {
                int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                String path = cursor.getString(columnindex);
                imageArrayList.add(new Image(path, "", "", 0));
//                rowValues.clear();
//                rowValues.put("path", path);
//                rowValues.put("id_albumContain", "");
//                rowValues.put("description", "");
//                rowValues.put("isFavored", 0);
//                MainActivity.db.insert("Image", null, rowValues);
            }
            cursor.close();
        }
    }
}