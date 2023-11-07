package com.example.imagesgallery.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class AlbumInfoActivity extends AppCompatActivity {

    TextView txtAlbumName, txtAlbumDescription;
    ImageView imgCoverAlbum;
    Toolbar toolbar;
    Album album;
    RecyclerView recyclerView;
    ImageButton btnAddImage;
    ImageAdapter adapter;

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

        // set description of album
        album = (Album) getIntent().getSerializableExtra("album");
        txtAlbumDescription.setText(album.getDescription());

        // set cover of album
        String coverPath = album.getCover().getPath();
        if (coverPath.equals(MainActivity.pathNoImage)) {
            imgCoverAlbum.setImageResource(R.drawable.no_image);
        } else {
            Glide.with(AlbumInfoActivity.this).load(coverPath).into(imgCoverAlbum);
        }

        // load image in album
        String sql = "SELECT * FROM Image WHERE id_albumContain LIKE ? ";
        String[] args = {"% " + String.valueOf(album.getId()) + " %"};
        Cursor cursor = null;
        try {
            cursor = MainActivity.db.rawQuery(sql, args);
        } catch (Exception exception) {
            return;
        }
        cursor.moveToPosition(-1);
        int pathImageColumn = cursor.getColumnIndex("path");
        int descriptionImageColumn = cursor.getColumnIndex("description");
        int id_ALbumContainColumn = cursor.getColumnIndex("id_albumContain");
        int isFavoredImageColumn = cursor.getColumnIndex("isFavored");

        String pathImage = MainActivity.pathNoImage;
        String id_AlbumContainImage = "";
        String descriptionImage = "";
        int isFavoredImage = 0;

        ArrayList<Image> images = album.getListImage();
        while (cursor.moveToNext()) {
            id_AlbumContainImage = cursor.getString(id_ALbumContainColumn);
            descriptionImage = cursor.getString(descriptionImageColumn);
            isFavoredImage = cursor.getInt(isFavoredImageColumn);
            pathImage = cursor.getString(pathImageColumn);
            Image image = new Image(pathImage, descriptionImage, id_AlbumContainImage, isFavoredImage);
            images.add(image);
        }
        cursor.close();
        album.setListImage(images);
        for (int i = 0; i < album.getListImage().size(); i++) {
            Log.d("ccccc", album.getListImage().get(i).getPath());
        }
        /*TODO: set adpater and recyclerview base on ImageAdapter*/

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(album.getName());

        // set return button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });


        // when click the description of album
        txtAlbumDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlbumInfoActivity.this, DescriptionActivity.class);
                intent.putExtra("album", (Serializable) album);
                startIntentChangeDescription.launch(intent);
            }
        });


        // when click the cover of album
        imgCoverAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToChangeDescriptionScreen();
            }
        });

        // activity launcher of adding image to album
        ActivityResultLauncher<Intent> startIntentAddImage = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            // get result from AddImageActivity and add image to album
                            String path = data.getStringExtra("path");
                            int id_album = album.getId();

                            ContentValues contentValues = new ContentValues();
                            String[] args2 = {path};
                            Cursor cursorImage = null;
                            try {
                                cursorImage = MainActivity.db.rawQuery("SELECT * FROM Image WHERE path = ?", args2);
                            } catch (Exception exception) {
                                return;
                            }
                            cursorImage.moveToPosition(-1);

                            int idALbumContainColumn = cursorImage.getColumnIndex("id_albumContain");
                            String idAlbumContainImage = "";
                            while (cursorImage.moveToNext()) {
                                idAlbumContainImage = cursorImage.getString(idALbumContainColumn);
                            }
                            cursorImage.close();
                            idAlbumContainImage = idAlbumContainImage + " " + String.valueOf(id_album) + " ";
                            contentValues.put("id_albumContain", idAlbumContainImage);
                            String[] args3 = {path};
                            long rowID = MainActivity.db.update("Image", contentValues, "path = ?", args3);
                            /*TODO: update array*/
                        }
                    }
                }
        );

        // click button add to insert image
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlbumInfoActivity.this, ChooseImageActivity.class);
                intent.putExtra("album", (Serializable) album);
                startIntentAddImage.launch(intent);
            }
        });
    }

    private void init() {
        txtAlbumDescription = (TextView) findViewById(R.id.txtAlbumDescription);
        imgCoverAlbum = (ImageView) findViewById(R.id.imgCoverAlbum);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.listImageInAlbum);
        btnAddImage = (ImageButton) findViewById(R.id.btnAddImage_album);
    }

    @Override
    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

    private void finishActivity() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("path", album.getCover().getPath());
        resultIntent.putExtra("description", album.getDescription());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void moveToChangeDescriptionScreen() {
        Intent intent = new Intent(AlbumInfoActivity.this, ChooseImageActivity.class);
        startIntentChangeCover.launch(intent);
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
            moveToChangeDescriptionScreen();
        } else if (itemID == R.id.deleteAlbum) {
            createDialogDeleteAlbum();
        }
        return super.onOptionsItemSelected(item);
    }

    public void createDialogDeleteAlbum() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this album ?");

        // click yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAlbum();
            }
        });
        // click no
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAlbum() {
        String[] args = {String.valueOf(album.getId())};
        long rowID = MainActivity.db.delete("Album", "id_album = ?", args);
        if (rowID > 0) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("isDelete", rowID);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    // when click button back in toolbar or in smartphone to finish ChooseImageActivity
    ActivityResultLauncher<Intent> startIntentChangeCover = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        // get result from AddImageActivity and change cover
                        String path = data.getStringExtra("path");
                        Image image = album.getCover();
                        image.setPath(path);
                        album.setCover(image);
                        Glide.with(AlbumInfoActivity.this).load(album.getCover().getPath()).into(imgCoverAlbum);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("cover", path);
                        String[] args2 = {String.valueOf(album.getId())};
                        MainActivity.db.update("Album", contentValues, "id_album = ?", args2);
                    }
                }
            }
    );

    // when click button back in toolbar or in smartphone to finish DescriptionActivity
    ActivityResultLauncher<Intent> startIntentChangeDescription = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        // get result from DescriptionActivity and change description
                        String description = data.getStringExtra("description");
                        album.setDescription(description);
                        txtAlbumDescription.setText(description);
                    }
                }
            }
    );
}