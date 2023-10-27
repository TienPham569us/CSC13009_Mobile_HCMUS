package csu.matos.handlepermission;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ShowSoloPicture extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.solo_big_picture);

        // Get the path to the image from the intent
        String imagePath = getIntent().getStringExtra("image_path");

        // Get the ImageView element
        ImageView imageView = findViewById(R.id.imageViewSolo);

        // Load the image into the ImageView element
        Glide.with(this).load(imagePath).into(imageView);
    }
}
