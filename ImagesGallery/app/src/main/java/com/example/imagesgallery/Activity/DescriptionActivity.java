package com.example.imagesgallery.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.R;

import java.util.Objects;

public class DescriptionActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText edtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        init();

        // set description of album
        Album album = (Album) getIntent().getSerializableExtra("album");
        edtDescription.setText(album.getDescription());
        edtDescription.setFocusableInTouchMode(false);

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

    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        edtDescription = (EditText) findViewById(R.id.edtDescriptionAlbum);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_description, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.changeDescription) {
            if (edtDescription.isFocusableInTouchMode()) { // click done
                String description_changed = edtDescription.getText().toString();
                /* TODO: CHANGE DATABASE*/
                edtDescription.setText(description_changed);
                edtDescription.setFocusable(false);
                item.setIcon(R.drawable.edit);
            } else { // click edit
                item.setIcon(R.drawable.done);
                edtDescription.setFocusableInTouchMode(true);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}