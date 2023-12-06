package com.example.imagesgallery.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.util.ArrayList;
import java.util.Objects;

public class FavoriteImagesActivity extends AppCompatActivity {

    Toolbar toolbar;
    MainActivity mainActivity;

    RecyclerView recycler;
    ArrayList<Image> images;
    ImageAdapter adapter;

    LinearLayout linearLayout;

    ClickListener clickListener = new ClickListener() {
        @Override
        public void click(int index) {
            // Toast.makeText(mainActivity,"clicked item index is "+index,Toast.LENGTH_LONG).show();
        }

        @Override
        public void longClick(int index) {

        }
    };
    GridLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_images);

        //getSupportFragmentManager().beginTransaction().replace(R.id.container, albumFragment).commit();
        init();
        //linearLayout = (LinearLayout) findViewById(R.id)
        recycler = findViewById(R.id.favorite_gallery_recycler);
        images = new ArrayList<>();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthInDp = displayMetrics.widthPixels / displayMetrics.density;
        int imageWidth = 110; // size of an image
        int desiredColumnCount = (int)screenWidthInDp / imageWidth; // the number of images in a row
        loadFavoritesImage();
        adapter = new ImageAdapter(this, images, clickListener);
        manager = new GridLayoutManager(this, desiredColumnCount);

        recycler.setLayoutManager(manager);
        recycler.setAdapter(adapter);




        recycler.getAdapter().notifyDataSetChanged();

        // using toolbar as ActionBar
        /*setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Favorite images");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);*/
    }

    private void init(){
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void loadFavoritesImage() {

        String args[] = {"1"};
        Cursor cursor = mainActivity.db.rawQuery("SELECT * FROM Image WHERE isFavored = ?",args);
        cursor.moveToPosition(-1);

        int isFavored=1;
        String description = "";
        String path="";
        while(cursor.moveToNext()) {
            int favorColumn = cursor.getColumnIndex("isFavored");
            int descriptionColumn = cursor.getColumnIndex("description");
            int pathColumn = cursor.getColumnIndex("path");
            isFavored = cursor.getInt(favorColumn);
            description = cursor.getString(descriptionColumn);
            path = cursor.getString(pathColumn);

            Image newImage = new Image(path, description, isFavored);


            images.add(newImage);
        }

        Log.d("favor","oke");

    }
}