package com.example.imagesgallery.Activity;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.R;

import java.io.File;
import java.util.ArrayList;

public class ImageInfoActivity extends AppCompatActivity {
    public String imageTemp;
    private Context context;
    public String nextImageTemp;
    ImageView imageView;
    ImageAdapter myAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_image);
        // Get the path to the image from the intent
        String imagePath = getIntent().getStringExtra("image_path");
        String nextImagePath = getIntent().getStringExtra("next_image_path");
        imageTemp=imagePath;
        //nextImageTemp=nextImagePath;
        // Get the ImageView element
        imageView = findViewById(R.id.imageFullScreen);
        Toolbar toolbar =(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Load the image into the ImageView element
        Glide.with(this).load(imageTemp).into(imageView);
        //Glide.with(this).load(nextImageTemp).into(imageView);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.addImage) {
            Toast.makeText(this, "Them hinh", Toast.LENGTH_SHORT).show();

        } else if (itemID == R.id.deleteImage) {
            //Toast.makeText(this, "Xoa anh", Toast.LENGTH_SHORT).show();

            File deleteImage =new File(imageTemp);
            if (deleteImage.exists()) {
                if (deleteImage.delete()) {
                    ArrayList<String> newImageList= myAdapter.getImages_list();
                    newImageList.remove(imageTemp);
                    myAdapter.setImages_list(newImageList);

                    // Sau khi xóa tệp tin, thông báo cho MediaScanner cập nhật thư viện ảnh
                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageTemp}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            imageView = findViewById(R.id.imageFullScreen);
                            Glide.with(context).load(nextImageTemp).into(imageView);
                        }
                    });
                }
            }
            //Glide.with(this).load(nextImageTemp).into(imageView);
        } else if (itemID == R.id.infomation) {
            Toast.makeText(this, "Thong tin", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


}

