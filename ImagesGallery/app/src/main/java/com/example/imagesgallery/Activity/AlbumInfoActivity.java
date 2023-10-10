package com.example.imagesgallery.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagesgallery.R;

import java.util.Objects;

public class AlbumInfoActivity extends AppCompatActivity {

    TextView txtAlbumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        init();

        Intent intent = getIntent();
        String name = intent.getStringExtra("NameAlbum");

        // assigning ID of the toolbar to a variable
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void init() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.addImage) {
            Toast.makeText(this, "Them hinh", Toast.LENGTH_SHORT).show();
        } else if (itemID == R.id.changeAvatar) {
            Toast.makeText(this, "Doi hinh dai dien", Toast.LENGTH_SHORT).show();
        } else if (itemID == R.id.deleteAlbum) {
            Toast.makeText(this, "Xoa album", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);

    }
}