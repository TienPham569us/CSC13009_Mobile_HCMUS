package com.example.imagesgallery.Activity;

import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Fragment.ImageFragment;
import com.example.imagesgallery.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ImageInfoActivity extends AppCompatActivity {
    public String imageTemp;
    public String nextImageTemp;
    private Context context;
    ImageView imageView;
    ImageAdapter myAdapter;
    int imagePosition =0;
    TextView totalImages;
    ArrayList<String> images;
    ImageFragment imageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_image);

        // Get the path to the image from the intent
        String imagePath = getIntent().getStringExtra("image_path");
        String nextImagePath = getIntent().getStringExtra("next_image_path");
        imagePosition = getIntent().getExtras().getInt("position");
        imageTemp=imagePath;
        nextImageTemp=nextImagePath;
        // Get the ImageView element
        imageView = findViewById(R.id.imageFullScreen);
        Toolbar toolbar =(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        imagePosition = getIntent().getExtras().getInt("position");
        Toast.makeText(getApplicationContext(),"image position:"+imagePosition,Toast.LENGTH_SHORT).show();
        // Load the image into the ImageView element
        Glide.with(this).load(imagePath).into(imageView);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


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

        }
        else if (itemID == R.id.deleteImage) {
            //Toast.makeText(this, "Xoa anh", Toast.LENGTH_SHORT).show();

            File deleteImage =new File(imageTemp);
            if (deleteImage.exists()) {
                if (deleteImage.delete()) {
                   // ArrayList<String> newImageList= myAdapter.getImages_list();
                    //newImageList.remove(imageTemp);
                    //myAdapter.setImages_list(newImageList);
                    //myAdapter.notifyItemRemoved(imagePosition);
                    //myAdapter.notifyDataSetChanged();

                    // Sau khi xóa tệp tin, thông báo cho MediaScanner cập nhật thư viện ảnh
                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageTemp}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            //imageView = findViewById(R.id.imageFullScreen);
                            //Glide.with(context).load(nextImageTemp).into(imageView);
                        }
                    });
                    Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa Activity Stack
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Tạo mới Task
                        startActivity(intent);
                    }
                    //Glide.with(this).load(nextImageTemp).into(imageView);

                }
            }
            //Glide.with(this).load(nextImageTemp).into(imageView);
        } else if (itemID == R.id.infomation) {
            Toast.makeText(this, "Thong tin", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ImageInfoActivity.this,DetailImageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("position",imagePosition);
            intent.putExtras(bundle);
            ImageInfoActivity.this.startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }


}

