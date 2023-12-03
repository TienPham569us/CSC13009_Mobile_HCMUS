package com.example.imagesgallery.Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.File;
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
    //AT: Add button multiSelectButton
//    Button multiSelectButton; //id: multiSelectBtnAlbum
//    Button deleteButton;
//    Button slideshowButton;
    ArrayList<Image> deletedImagesArrayList;
    boolean isLoading = false, isAllItemsLoaded = false;
    private final int ItemsPerLoading = 21;
    private int CurrentMaxPosition = 0, IdMaxWhenStartingLoadData = 0;
    public static final int ACTION_CHANGE_COVER = 1, ACTION_ADD_IMAGE = 2;
    int clickPosition = -1;
    ClickListener clickListener = new ClickListener() {
        @Override
        public void click(int index) {
            clickPosition = index;
            int id_album = album.getId();
            ArrayList<Image> listImage = album.getListImage();

            Intent intent = new Intent(AlbumInfoActivity.this, ImageInfoActivity.class);
            intent.putExtra("PreviousActivity", "AlbumInfoActivity");
            intent.putExtra("id_album", id_album);
            intent.putExtra("position", index);
            intent.putExtra("image", (Serializable) listImage.get(index));
            startIntentSeeImageInfo.launch(intent);
        }

        @Override
        public void longClick(int index) {
            // change toolbar
            adapter.setMultiSelectMode(true);
            invalidateOptionsMenu();
            // Enter multi-select mode
            enterMultiselectMode(index);
        }
    };

    //AT
//    private void toggleButtonsOfMultiSelectMode(Boolean isMultiSelectMode) {
//        if (isMultiSelectMode) {
//            deleteButton.setVisibility(View.VISIBLE);
//            slideshowButton.setVisibility(View.VISIBLE);
//        } else {
//            deleteButton.setVisibility(View.INVISIBLE);
//            slideshowButton.setVisibility(View.INVISIBLE);
//        }
//    }

    public void createDialogDeleteImage() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        if (adapter.getSelectedImages().size() == 0) {
            Toast.makeText(this, "You have not chosen any images", Toast.LENGTH_SHORT).show();
            return;
        }
        builder.setMessage("Are you sure you want to delete these images ?");

        // click yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "onClick: delete image");

                ArrayList<String> selectedImages = adapter.getSelectedImages();
                Log.d(TAG, "selectedImages size " + adapter.getSelectedImages().size());
                // Define variables to track the number of successfully deleted images
                for (String imagePath : selectedImages) {
                    deleteImagesInAlbum(imagePath);
                }
                //toggleButtonsOfMultiSelectMode(multiSelectMode);
                //multiSelectMode = false;
                //deleteButton.setVisibility(View.GONE);
                //slideshowButton.setVisibility(View.GONE);
                //adapter.setMultiSelectMode(multiSelectMode);
                //adapter.clearSelection();
                exitMultiselectMode();
                Log.d("selected images: ", adapter.getSelectedImages().toString());
                //multiSelectButton.setText("Select");
            }
        });
        // click no
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    ;
    //AT

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_info);

        deletedImagesArrayList = new ArrayList<>();
//        multiSelectButton = findViewById(R.id.multiSelectBtnAlbum);
//        deleteButton = findViewById(R.id.deleteBtnAlbum);
//        slideshowButton = findViewById(R.id.slideshowBtnAlbum);
//        multiSelectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (multiSelectMode) {
//                    multiSelectMode = false;
//                    adapter.setMultiSelectMode(multiSelectMode);
//                    adapter.clearSelection();
//                    multiSelectButton.setText("Select");
//
//                    // Handle actions in multi-select mode
//                } else {
//                    // Enter multi-select mode
//                    multiSelectMode = true;
//                    adapter.setMultiSelectMode(multiSelectMode);
//                    // Update UI, e.g., change button text
//                    multiSelectButton.setText("Cancel"); // Optionally, you can change the button label
//                }
//                toggleButtonsOfMultiSelectMode(multiSelectMode);
//            }
//        });
//
//        multiSelectButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                isLongClick = true;
//                invalidateOptionsMenu();
//                return true;
//            }
//        });
//
//        deleteButton.setEnabled(true);
//        slideshowButton.setEnabled(true);
//        // Set state for buttons when in multi-select mode
////        adapter.setSelectionChangeListener(this);
//        deleteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                createDialogDeleteImage();
////                manager = new GridLayoutManager(mainActivity, 3);
////                recycler.setLayoutManager(manager);
////                loadImages();
//
//                // Handle actions in multi-select mode
//            }
//        });
//
//        slideshowButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (adapter.getSelectedImages().size() <= 1) {
//                    Toast.makeText(AlbumInfoActivity.this, "You have to choose more than one image", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                ArrayList<String> selectedImages = adapter.getSelectedImages();
//                if (!selectedImages.isEmpty()) {
//                    // Call the method in MainActivity to start the SlideshowActivity
//                    Intent slideshowIntent = new Intent(AlbumInfoActivity.this, SlideshowActivity.class);
//                    slideshowIntent.putStringArrayListExtra("selectedImages", selectedImages);
//                    startActivity(slideshowIntent);
//                }
//            }
//        });

        //AT

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
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthInDp = displayMetrics.widthPixels / displayMetrics.density;
        int imageWidth = 110; // size of an image
        int desiredColumnCount = (int)screenWidthInDp / imageWidth; // the number of images in a row

        images = new ArrayList<>();
        album.setListImage(images);
        adapter = new ImageAdapter(AlbumInfoActivity.this, images, clickListener);
        GridLayoutManager manager = new GridLayoutManager(AlbumInfoActivity.this, desiredColumnCount);
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
                if (!adapter.isInMultiSelectMode()) {
                    finishActivity(); // return to album tab
                } else {
                    // cancel multi select mode
                    exitMultiselectMode();
                }
            }
        });


        // when click the description of album
        txtAlbumDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToChangeDescriptionScreen();
            }
        });

        // when click the cover of album
        imgCoverAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToChangeCoverScreen();
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

    private void exitMultiselectMode() {
        invalidateOptionsMenu();
        adapter.setMultiSelectMode(false);
        adapter.clearSelection();
        btnAddImage.setVisibility(View.VISIBLE);
    }

    private void enterMultiselectMode(int index) {
        adapter.setMultiSelectMode(true);
        adapter.toggleSelection(index);
        btnAddImage.setVisibility(View.GONE);
    }

    private void deleteImagesInAlbum(String path) {
        File deleteImage = new File(path);
        if (deleteImage.exists()) {
            if (deleteImage.delete()) {
                // change cover if this image is used as cover of album
                if (album.getCover().getPath().equals(path)) {
                    album.getCover().setPath(MainActivity.pathNoImage);
                    imgCoverAlbum.setImageResource(R.drawable.no_image);
                }
                // change database
                String[] args = {path};
                long rowID = MainActivity.db.delete("Image", "path = ?", args);
                long rowID2 = MainActivity.db.delete("Album_Contain_Images", "path = ?", args);

                // Sau khi xóa tệp tin, thông báo cho MediaScanner cập nhật thư viện ảnh
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });

                // change arrayList image in album
                for (int i = 0; i < images.size(); i++) {
                    if (images.get(i).getPath().equals(path)) {
                        images.remove(i);
                        break;
                    }
                }
            }
        }
    }

    private void moveToChangeDescriptionScreen() {
        Intent intent = new Intent(AlbumInfoActivity.this, DescriptionActivity.class);
        intent.putExtra("album", (Serializable) album);
        startIntentChangeDescription.launch(intent);
    }

    private void chooseImage() {
        Intent intent = new Intent(AlbumInfoActivity.this, ChooseImageActivity.class);
        intent.putExtra("album", (Serializable) album);
        intent.putExtra("action", ACTION_ADD_IMAGE);
        startIntentAddImages.launch(intent);
    }

    private void loadDataFromDatabase() {
        Log.d("aaaaa", "before: " + images.size());
        String sql = "";
        Cursor cursor = null;
        if (IdMaxWhenStartingLoadData == 0) {
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

        String sqlContainImages = "SELECT * FROM Album_Contain_Images AS Contain, Image AS I " +
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
        Log.d("aaaaa", "after: " + images.size());
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
        resultIntent.putExtra("isFavored", album.getIsFavored());
        resultIntent.putExtra("PreviousActivity", "FavoriteAlbumActivity");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void moveToChangeCoverScreen() {
        Intent intent = new Intent(AlbumInfoActivity.this, ChooseImageActivity.class);
        intent.putExtra("album", (Serializable) album);
        intent.putExtra("action", ACTION_CHANGE_COVER);
        startIntentChangeCover.launch(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (adapter.isInMultiSelectMode()) {
            getMenuInflater().inflate(R.menu.menu_album_info_long_click, menu);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.close_icon);
        } else {
            getMenuInflater().inflate(R.menu.menu_album_info, menu);
            getSupportActionBar().setHomeAsUpIndicator(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            int isFavored = album.getIsFavored();
            if (isFavored == 1) {
                menu.findItem(R.id.removeAnAlbumFromFavorites).setVisible(true);
                menu.findItem(R.id.addAnAlbumToFavorites).setVisible(false);
            } else if (isFavored == 0) {
                menu.findItem(R.id.removeAnAlbumFromFavorites).setVisible(false);
                menu.findItem(R.id.addAnAlbumToFavorites).setVisible(true);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.addImage) {
            chooseImage();
        } else if (itemID == R.id.changeCover) {
            moveToChangeCoverScreen();
        } else if (itemID == R.id.deleteAlbum) {
            createDialogDeleteAlbum();
        } else if (itemID == R.id.changeDescription) {
            moveToChangeDescriptionScreen();
        } else if (itemID == R.id.addAnAlbumToFavorites) {
            addAlbumToFavorites();
            invalidateOptionsMenu();
        } else if (itemID == R.id.removeAnAlbumFromFavorites) {
            removeAlbumFromFavorites();
            invalidateOptionsMenu();
        } else if (itemID == R.id.deleteImages) {
            createDialogDeleteImage();
        } else if (itemID == R.id.slideshow) {
            slideshowImages();
        } else if (itemID == R.id.removeFromAlbum) {
            createDialogRemoveImages();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createDialogRemoveImages() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        if (adapter.getSelectedImages().size() == 0) {
            Toast.makeText(this, "You have not chosen any images", Toast.LENGTH_SHORT).show();
            return;
        }
        builder.setMessage("Are you sure you want to remove these images from album ?");

        // click yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ArrayList<String> selectedImages = adapter.getSelectedImages();
                // Define variables to track the number of successfully deleted images
                for (String imagePath : selectedImages) {
                    removeImageFromAlbum(imagePath);
                }
                exitMultiselectMode();
            }
        });
        // click no
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeImageFromAlbum(String imagePath) {
        String[] args = {imagePath, String.valueOf(album.getId())};
        long rowID = MainActivity.db.delete("Album_Contain_Images", "path = ? AND id_album = ?", args);
        if (rowID > 0) {
            // change arrayList image in album
            for (int i = 0; i < images.size(); i++) {
                if (images.get(i).getPath().equals(imagePath)) {
                    images.remove(i);
                    break;
                }
            }
        }
    }

    private void slideshowImages() {
        if (adapter.getSelectedImages().size() <= 1) {
            Toast.makeText(AlbumInfoActivity.this, "You have to choose more than one image", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> selectedImages = adapter.getSelectedImages();
        if (!selectedImages.isEmpty()) {
            // Call the method in MainActivity to start the SlideshowActivity
            Intent slideshowIntent = new Intent(AlbumInfoActivity.this, SlideshowActivity.class);
            slideshowIntent.putStringArrayListExtra("selectedImages", selectedImages);
            startActivity(slideshowIntent);
        }
    }

    private void addAlbumToFavorites() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("isFavored", 1);
        String[] args = {String.valueOf(album.getId())};
        long rowID = MainActivity.db.update("Album", contentValues, "id_album = ?", args);
        if (rowID > 0) {
            album.setIsFavored(1);
        }
    }

    private void removeAlbumFromFavorites() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("isFavored", 0);
        String[] args = {String.valueOf(album.getId())};
        long rowID = MainActivity.db.update("Album", contentValues, "id_album = ?", args);
        if (rowID > 0) {
            album.setIsFavored(0);
        }
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
            resultIntent.putExtra("isDelete", 1);
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
                        int isFavored = data.getIntExtra("isFavored", 0);

                        // if user choose delete image
                        if (pathDeleted != null) {
                            images.remove(clickPosition);
                            adapter.notifyItemRemoved(clickPosition);

                            // change cover if deleting image used as cover
                            if (album.getCover().getPath().equals(pathDeleted)) {
                                album.getCover().setPath(MainActivity.pathNoImage);
                                imgCoverAlbum.setImageResource(R.drawable.no_image);
                            }
                        }

                        // if user choose remove image from album
                        if (pathRemoved != null) {
                            images.remove(clickPosition);
                            adapter.notifyItemRemoved(clickPosition);
                        }

                        if (pathRemoved == null && pathDeleted == null) {
                            // update favorite of image
                            images.get(clickPosition).setIsFavored(isFavored);
                        }
                    }
                }
            }
    );

    // activity launcher of adding images to album
    ActivityResultLauncher<Intent> startIntentAddImages = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Image image = (Image) data.getSerializableExtra("image");
                        ArrayList<Image> addedImageArrayList = (ArrayList<Image>) data.getSerializableExtra("selectedImages");

                        if (image != null) {
                            // add image to album
                            images.add(0, image);
                            album.setListImage(images);
                            adapter.notifyItemInserted(0);

                            // change selection index after add image to album
//                            ArrayList<Integer> selectedPositions = adapter.getSelectedPositions();
//                            selectedPositions.replaceAll(integer -> integer + 1);
//                            adapter.setSelectedPositions(selectedPositions);
                        }

                        if (addedImageArrayList != null) {
                            for (int i = 0; i < addedImageArrayList.size(); i++) {
                                images.add(0, addedImageArrayList.get(i));
                            }
                            adapter.notifyItemRangeChanged(0, addedImageArrayList.size());
                        }
                    }
                }
            }
    );

}