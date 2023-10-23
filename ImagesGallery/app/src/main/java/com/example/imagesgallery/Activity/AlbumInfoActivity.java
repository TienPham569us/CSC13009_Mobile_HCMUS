package com.example.imagesgallery.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.R;

import java.io.Serializable;
import java.util.Objects;

public class AlbumInfoActivity extends AppCompatActivity {

    TextView txtAlbumName, txtAlbumDescription;
    ImageView imgCoverAlbum;
    Toolbar toolbar;
    Album album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        init();

        // add ellipsize at the end of textview if it is long
        txtAlbumDescription.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                txtAlbumDescription.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int noOfLinesVisible = txtAlbumDescription.getHeight() / txtAlbumDescription.getLineHeight();
                txtAlbumDescription.setText(album.getDescription());
                txtAlbumDescription.setMaxLines(noOfLinesVisible);
                txtAlbumDescription.setEllipsize(TextUtils.TruncateAt.END);

            }
        });

        // set info of album
        album = (Album) getIntent().getSerializableExtra("album");
        txtAlbumDescription.setText(album.getDescription());
        /* TODO: SET COVER OF ALBUM*/

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(album.getName());

        // set return button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // when click the description of album
        txtAlbumDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlbumInfoActivity.this, DescriptionActivity.class);
                intent.putExtra("album", (Serializable) album);
                startActivity(intent);
            }
        });

        // when click the cover of album
        imgCoverAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AlbumInfoActivity.this, "Change cover", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        txtAlbumDescription = (TextView) findViewById(R.id.txtAlbumDescription);
        imgCoverAlbum = (ImageView) findViewById(R.id.imgCoverAlbum);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        } else if (itemID == R.id.changeCover) {
            Toast.makeText(this, "Doi hinh dai dien", Toast.LENGTH_SHORT).show();
        } else if (itemID == R.id.deleteAlbum) {
            Toast.makeText(this, "Xoa album", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}