package com.example.imagesgallery.Activity;

import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Interface.ClickListener;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;
import com.example.imagesgallery.Utility.FileUtility;

import java.io.File;
import java.util.ArrayList;

public class TrashImageActivity extends AppCompatActivity {
    RecyclerView recycler;
    ArrayList<Image> images;
    ImageAdapter adapter;

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
        setContentView(R.layout.activity_trash_images);

        //getSupportFragmentManager().beginTransaction().replace(R.id.container, albumFragment).commit();
        init();
        //linearLayout = (LinearLayout) findViewById(R.id)
        recycler = findViewById(R.id.trash_gallery_recycler);
        images = new ArrayList<>();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthInDp = displayMetrics.widthPixels / displayMetrics.density;
        int imageWidth = 110; // size of an image
        int desiredColumnCount = (int)screenWidthInDp / imageWidth; // the number of images in a row

        adapter = new ImageAdapter(this, images, clickListener);
        manager = new GridLayoutManager(this, desiredColumnCount);

        recycler.setLayoutManager(manager);
        recycler.setAdapter(adapter);

        recycler.getAdapter().notifyDataSetChanged();


    }

    private void init(){
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (images!=null) {
            images.clear();
            loadTrashImage();
        }
        if (adapter!=null)
        {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadTrashImage() {

        String trashFolderPath = Environment.getExternalStorageDirectory()+ File.separator+".trash_image_folder";
        ArrayList<File> resultFile = FileUtility.getAllImageInADirectory(trashFolderPath);
        if (resultFile!=null) {
            for (File file : resultFile) {
                Image newImage = new Image(file.getAbsolutePath(), "", 0, false, true);
                images.add(newImage);
            }
            Log.d("hidden", "oke");
        }

    }
}
