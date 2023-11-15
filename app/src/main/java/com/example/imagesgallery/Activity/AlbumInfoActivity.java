package com.example.imagesgallery.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AlbumInfoActivity extends AppCompatActivity {

    TextView txtAlbumName, txtAlbumDescription;
    ImageView imgCoverAlbum;
    Toolbar toolbar;
    Album album;
    RecyclerView recyclerView;
    ImageButton btnAddImage;
    ImageAdapter adapter;
    ArrayList<Image> images;
    int OrderInDatabase;
    boolean isLoading = false, isAllItemsLoaded = false;
    private final int ItemsPerLoading = 21;
    private int CurrentMaxPosition = 0, IdMaxWhenStartingLoadData = 0;
    ClickListener clickListener = new ClickListener() {
        @Override
        public void click(int index) {
            int id_album = album.getId();
            OrderInDatabase = 1;  // Order of image among identical images in the same album
            ArrayList<Image> listImage = album.getListImage();
            String pathImage = listImage.get(index).getPath();
            for (int i = 0; i < index; i++) {
                if (listImage.get(i).getPath().equals(pathImage)) {
                    OrderInDatabase++;
                }
            }

            Intent intent = new Intent(AlbumInfoActivity.this, ImageInfoActivity.class);
            intent.putExtra("PreviousActivity", "AlbumInfoActivity");
            intent.putExtra("id_album", id_album);
            intent.putExtra("position", index);
            intent.putExtra("image_path", pathImage);
            intent.putExtra("OrderInDatabase", OrderInDatabase);
            startIntentSeeImageInfo.launch(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        init();

        isAllItemsLoaded = false;
        CurrentMaxPosition = 0;
        IdMaxWhenStartingLoadData = 0;

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

        // init to prepare load images to album
        images = new ArrayList<>();
        album.setListImage(images);
        adapter = new ImageAdapter(AlbumInfoActivity.this, startIntentSeeImageInfo, images, clickListener);
        GridLayoutManager manager = new GridLayoutManager(AlbumInfoActivity.this, 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        // load first images in album
        loadDataFromDatabase();

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

        // click button add to insert image
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
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
                if (!isLoading && gridLayoutManager != null && gridLayoutManager.findLastCompletelyVisibleItemPosition() >= images.size() - 1 && !isAllItemsLoaded) {
                    isLoading = true;
                    // Create an executor that executes tasks in the main thread and background thread
                    Executor mainExecutor = ContextCompat.getMainExecutor(AlbumInfoActivity.this);
                    ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
                    // Load data in the background thread.
                    backgroundExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            loadDataFromDatabase();
                            // Update list images in a album on the main thread
                            mainExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    isLoading = false;
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    // activity launcher of adding image to album
    ActivityResultLauncher<Intent> startIntentAddImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Image image = (Image) data.getSerializableExtra("image");
                        if (image != null) {
                            images.add(0, image);
                            album.setListImage(images);
                            adapter.notifyDataSetChanged();
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("id_album", album.getId());
                            contentValues.put("path", image.getPath());
                            long rowID = MainActivity.db.insert("Album_Contain_Images", null, contentValues);
                        }
                    }
                }
            }
    );

    private void chooseImage() {
        Intent intent = new Intent(AlbumInfoActivity.this, ChooseImageActivity.class);
        intent.putExtra("album", (Serializable) album);
        startIntentAddImage.launch(intent);
    }

    private void loadDataFromDatabase() {
        Log.d("aaaaa","before: " + images.size());
        String sql = "";
        Cursor cursor = null;
        if (IdMaxWhenStartingLoadData == 0) {
            String[] argsAlbum = {String.valueOf(ItemsPerLoading), String.valueOf(CurrentMaxPosition)};
            try {
                sql = "SELECT MAX(id) FROM Album_Contain_Images";
                cursor = MainActivity.db.rawQuery(sql, null);
            } catch (Exception exception) {
                return;
            }

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                IdMaxWhenStartingLoadData = cursor.getInt(0);
            }
            cursor.close();
        }

        String sqlContainImages = "SELECT * FROM Album_Contain_Images Contain, Image I " +
                "WHERE id_album = ? AND Contain.path = I.path AND Contain.id <= ? ORDER BY id DESC LIMIT ? OFFSET ?";
        String[] argsContainImages = {String.valueOf(album.getId()), String.valueOf(IdMaxWhenStartingLoadData), String.valueOf(ItemsPerLoading), String.valueOf(CurrentMaxPosition)};
        Cursor cursorContainImages = null;
        try {
            cursorContainImages = MainActivity.db.rawQuery(sqlContainImages, argsContainImages);
        } catch (Exception exception) {
            return;
        }

        if (!cursorContainImages.moveToFirst()) {
            isAllItemsLoaded = true;
        }
        cursorContainImages.moveToPosition(-1);

        int pathImageColumn = cursorContainImages.getColumnIndex("Contain.path");
        int descriptionImageColumn = cursorContainImages.getColumnIndex("I.description");
        int isFavoredImageColumn = cursorContainImages.getColumnIndex("I.isFavored");

        String pathImageInAlbum = MainActivity.pathNoImage;
        String descriptionImageInAlbum = "";
        int isFavoredImageInAlbum = 0;

        //images = new ArrayList<>();
        while (cursorContainImages.moveToNext()) {
            descriptionImageInAlbum = cursorContainImages.getString(descriptionImageColumn);
            isFavoredImageInAlbum = cursorContainImages.getInt(isFavoredImageColumn);
            pathImageInAlbum = cursorContainImages.getString(pathImageColumn);
            Image image = new Image(pathImageInAlbum, descriptionImageInAlbum, isFavoredImageInAlbum);
            images.add(image);
        }
        cursorContainImages.close();
        CurrentMaxPosition += ItemsPerLoading;
        Log.d("aaaaa","after: " + images.size());
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
        resultIntent.putExtra("CoverPath", album.getCover().getPath());
        resultIntent.putExtra("description", album.getDescription());
        resultIntent.putExtra("images", images);
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
            chooseImage();
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
                        Image image = (Image) data.getSerializableExtra("image");
                        if (image != null) {
                            album.setCover(image);
                            Glide.with(AlbumInfoActivity.this).load(album.getCover().getPath()).into(imgCoverAlbum);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("cover", image.getPath());
                            String[] args2 = {String.valueOf(album.getId())};
                            MainActivity.db.update("Album", contentValues, "id_album = ?", args2);
                        }
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

    // when return from ImageInfoActivity
    public ActivityResultLauncher<Intent> startIntentSeeImageInfo = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String pathDeleted = data.getStringExtra("ImageDeleted");
                        String pathRemoved = data.getStringExtra("ImageRemoved");

                        // if user choose delete image
                        if (pathDeleted != null) {
                            // delete images in album
                            for (int i = 0; i < images.size(); i++) {
                                if (images.get(i).getPath().equals(pathDeleted)) {
                                    images.remove(i);
                                    adapter.notifyItemRemoved(i);
                                    i--;
                                }
                            }
                            if (album.getCover().getPath().equals(pathDeleted)) {
                                // change cover if deleting image used as cover
                                album.getCover().setPath(MainActivity.pathNoImage);
                                imgCoverAlbum.setImageResource(R.drawable.no_image);
                            }
                        }

                        // if user choose remove image from album
                        if (pathRemoved != null) {
                            int count = 1;
                            for (int i = 0; i < images.size(); i++) {
                                if (images.get(i).getPath().equals(pathRemoved)) {
                                    if (count == OrderInDatabase) {
                                        images.remove(i);
                                        adapter.notifyItemRemoved(i);
                                        break;
                                    } else {
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
    );

}