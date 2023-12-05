package com.example.imagesgallery.Activity;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Fragment.ImageFragment;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class ImageInfoActivity extends AppCompatActivity {
    public String imageTemp;
    public String nextImageTemp;
    private Context context;
    ImageView imageView;
    ImageAdapter myAdapter;
    int imagePosition = 0;
    TextView totalImages;
    ArrayList<String> images;
    ImageFragment imageFragment;
    String imagePath = "";
    Image image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_image);

        // Get the path to the image from the intent
        imagePath = getIntent().getStringExtra("image_path");
        String nextImagePath = getIntent().getStringExtra("next_image_path");
        imagePosition = getIntent().getExtras().getInt("position");
        image = (Image) getIntent().getSerializableExtra("image");
        if (image != null) {
            imagePath = image.getPath();
        }
        imageTemp = imagePath;
        nextImageTemp = nextImagePath;
        // Get the ImageView element
        imageView = findViewById(R.id.imageFullScreen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Toast.makeText(getApplicationContext(), "image position:" + imagePosition, Toast.LENGTH_SHORT).show();
        // Load the image into the ImageView element
        Glide.with(this).load(imagePath).into(imageView);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check whether user go to this activity from AlbumInfoActivity or not
                String PreviousActivity = getIntent().getStringExtra("PreviousActivity");
                if (PreviousActivity != null && PreviousActivity.equals("AlbumInfoActivity")) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("isFavored", image.getIsFavored());
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    finish();
                }
            }
        });


        /*// Lấy Intent từ Activity gọi đến
        Intent intent = getIntent();
        // Lấy Bundle từ Intent
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            // Lấy dữ liệu từ Bundle
            int receivedData = bundle.getInt("CountImage");

            // Hiển thị dữ liệu trong Fragment
            ImageFragment fragment = new ImageFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }*/
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_info, menu);
        MenuItem disabledMenuItem = menu.findItem(R.id.RemoveImage);
        // Disable item if user go to this activity from ImageTab
        String PreviousActivity = getIntent().getStringExtra("PreviousActivity");
        if (disabledMenuItem != null && (PreviousActivity == null || !(PreviousActivity.equals("AlbumInfoActivity")))) {
            menu.findItem(R.id.RemoveImage).setVisible(false);
        }

        int isFavored = image.getIsFavored();
        if (isFavored == 1) {
            menu.findItem(R.id.removeFavorites).setVisible(true);
            menu.findItem(R.id.addFavorites).setVisible(false);
        } else if (isFavored == 0) {
            menu.findItem(R.id.removeFavorites).setVisible(false);
            menu.findItem(R.id.addFavorites).setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.addImage) {
            Toast.makeText(this, "Them hinh", Toast.LENGTH_SHORT).show();

        } else if (itemID == R.id.deleteImage) {
            //Toast.makeText(this, "Xoa anh", Toast.LENGTH_SHORT).show();
            createDialogDeleteImage();

            //Glide.with(this).load(nextImageTemp).into(imageView);
        } else if (itemID == R.id.infomation) {
            //Toast.makeText(this, "Thong tin", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ImageInfoActivity.this, DetailImageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("position", imagePosition);
            intent.putExtras(bundle);
            ImageInfoActivity.this.startActivity(intent);
        } else if (itemID == R.id.RemoveImage) {
            RemoveImageFromAlbum();
        } else if (itemID == R.id.setAsWallpaper) {
            setAsWallpaper();
        } else if (itemID == R.id.shareImage) {
            shareImage();
        } else if (itemID == R.id.addFavorites) {
            addImageToFavorites();
            invalidateOptionsMenu();
        } else if (itemID == R.id.removeFavorites) {
            removeAlbumFromFavorites();
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    private void addImageToFavorites() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("isFavored", 1);
        String[] args = {image.getPath()};
        long rowID = MainActivity.db.update("Image", contentValues, "path = ?", args);
        if (rowID > 0) {
            image.setIsFavored(1);
        }
    }

    private void removeAlbumFromFavorites() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("isFavored", 0);
        String[] args = {image.getPath()};
        long rowID = MainActivity.db.update("Image", contentValues, "path = ?", args);
        if (rowID > 0) {
            image.setIsFavored(0);
        }
    }

    private void shareImage() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        shareImageAndText(bitmap);
    }

    private void shareImageAndText(Bitmap bitmap) {
        Uri uri = getImageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, "Image Text");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Image Subject");
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private Uri getImageToShare(Bitmap bitmap) {
        File folder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            folder.mkdirs();
            File file = new File(folder, "image.ipg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            uri = FileProvider.getUriForFile(this, "com.example.imagesgallery.Activity", file);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return uri;
    }

    //AT
    private void setAsWallpaper() {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            // Load the image from the file
            Uri imageUri = Uri.fromFile(new File(imagePath));
            wallpaperManager.setStream(getContentResolver().openInputStream(imageUri));
            Toast.makeText(this, "Wallpaper set successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Set Wallpaper", "Error setting wallpaper", e);
            Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
        }
    }
    //Update set as wallpaper


    public void createDialogDeleteImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete these images ?");

        // click yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteImage();
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

    private void deleteImage() {
        File deleteImage = new File(imageTemp);
        if (deleteImage.exists()) {
            if (deleteImage.delete()) {
                // ArrayList<String> newImageList= myAdapter.getImages_list();
                //newImageList.remove(imageTemp);
                //myAdapter.setImages_list(newImageList);
                //myAdapter.notifyItemRemoved(imagePosition);
                //myAdapter.notifyDataSetChanged();
                // change database
                String[] args = {imageTemp};
                long rowID = MainActivity.db.delete("Image", "path = ?", args);
                long rowID2 = MainActivity.db.delete("Album_Contain_Images", "path = ?", args);

                if (rowID > 0 && rowID2 > 0) {
                    Toast.makeText(this, "Delete success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                }


                // Sau khi xóa tệp tin, thông báo cho MediaScanner cập nhật thư viện ảnh
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageTemp}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        //imageView = findViewById(R.id.imageFullScreen);
                        //Glide.with(context).load(nextImageTemp).into(imageView);
                    }
                });

                String PreviousActivity = getIntent().getStringExtra("PreviousActivity");
                if (Objects.equals(PreviousActivity, "AlbumInfoActivity")) {
                    // return to previous activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("ImageDeleted", imageTemp);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa Activity Stack
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Tạo mới Task
                        startActivity(intent);
                    }
                }
            }
        }
    }

    private void RemoveImageFromAlbum() {
        int id_album = getIntent().getIntExtra("id_album", -1);
        int OrderInDatabase = getIntent().getIntExtra("OrderInDatabase", 1);

        String[] args = {String.valueOf(id_album), image.getPath(), String.valueOf(OrderInDatabase - 1)};
        String sql = "DELETE FROM Album_Contain_Images WHERE id = (SELECT id FROM Album_Contain_Images WHERE id_album = ? AND path = ? LIMIT 1 OFFSET ?)";
        MainActivity.db.execSQL(sql, args);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("ImageRemoved", image.getPath());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}

