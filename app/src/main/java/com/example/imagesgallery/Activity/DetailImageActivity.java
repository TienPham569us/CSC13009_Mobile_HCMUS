package com.example.imagesgallery.Activity;

import static android.os.Environment.MEDIA_MOUNTED;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagesgallery.R;

public class DetailImageActivity extends AppCompatActivity {
    private int imagePosition=-1;
    private String imageLink="abc";

    TextView txtViewLink;
    TextView Date;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_image);
        Bundle bundle = getIntent().getExtras();

        imagePosition = bundle.getInt("position");
        Toast.makeText(getApplicationContext(),"Detail postion: "+imagePosition,Toast.LENGTH_SHORT ).show();
        txtViewLink = (TextView) findViewById(R.id.txtViewLink);
        //loadImageInformation();
        txtViewLink.setText(imageLink);

    }

    private void loadImageInformation()
    {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            //Log.d("test","6");
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, order);

            cursor.moveToPosition(imagePosition);
            int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            imageLink = cursor.getString(columnindex);




        }
    }


}
