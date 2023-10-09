package com.example.imagesgallery.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagesgallery.Adapter.AlbumAdapter;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.util.ArrayList;
import java.util.Objects;

// Sau này đổi activity này thành fragment
public class AlbumTemp extends AppCompatActivity {

    GridView gridView;
    ArrayList<Album> albumArrayList;
    AlbumAdapter albumAdapter;
    ImageButton btnAddAlbum;
    Button btnAdd, btnCancel;
    EditText edtNameAlbum;
    TextView txtTitleDialog;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_temp);

        init();

        addToGridview();

        btnAddAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    /* Ánh xạ và khởi tạo*/
    private void init() {
        albumArrayList = new ArrayList<>();
        albumAdapter = new AlbumAdapter(AlbumTemp.this, albumArrayList);

        gridView = (GridView) findViewById(R.id.gridview_album);
        btnAddAlbum = (ImageButton) findViewById(R.id.btnAdd_album);
    }

    /* Thêm album vào mảng và cho hiển thị lên gridview */
    private void addToGridview() {
        gridView.setAdapter(albumAdapter);
        albumArrayList.add(new Album(new Image(R.drawable.image1), "Image1111111111111111111111"));
        albumArrayList.add(new Album(new Image(R.drawable.image2), "Image2"));
        albumArrayList.add(new Album(new Image(R.drawable.image3), "I3"));
        albumArrayList.add(new Album(new Image(R.drawable.image4), "I3"));
        albumArrayList.add(new Album(new Image(R.drawable.image5), "I3"));
        albumArrayList.add(new Album(new Image(R.drawable.image3), "I3"));
        albumArrayList.add(new Album(new Image(R.drawable.image3), "I3"));
        albumArrayList.add(new Album(new Image(R.drawable.image3), "I3"));
        albumArrayList.add(new Album(new Image(R.drawable.image3), "I3"));
        albumArrayList.add(new Album(new Image(R.drawable.image3), "I3"));
        albumArrayList.add(new Album(new Image(R.drawable.image3), "I3"));
        albumArrayList.add(new Album(new Image(R.drawable.image3), "I3"));
        albumAdapter.notifyDataSetChanged();
    }

    /* Hiện dialog khi nhấn nút thêm album */
    private void showDialog() {
        dialog = new Dialog(AlbumTemp.this);
        dialog.setContentView(R.layout.dialog_add_album);

        btnAdd = (Button) dialog.findViewById(R.id.buttonAdd);
        btnCancel = (Button) dialog.findViewById(R.id.buttonCancel);
        edtNameAlbum = (EditText) dialog.findViewById(R.id.edtAlbumName);
        txtTitleDialog = (TextView) dialog.findViewById(R.id.title_dialog);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtNameAlbum.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(AlbumTemp.this, "Bạn chưa nhập tên cho album", Toast.LENGTH_SHORT).show();
                } else {
                    albumArrayList.add(new Album(new Image(R.drawable.no_image), name));
                    albumAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        resizeDialog();
    }

    private void resizeDialog() {
        // Thay đổi kích thước dialog
        Display display = ((WindowManager) getSystemService(getApplicationContext().WINDOW_SERVICE)).getDefaultDisplay();
        int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        Objects.requireNonNull(dialog.getWindow()).setLayout((6 * width) / 7, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Lấy kích thước màn hình
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float screenWidth = displayMetrics.widthPixels;

        // Đặt kích thước chữ mới cho view
        float newTextSize = screenWidth * 0.05f;
        edtNameAlbum.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

        newTextSize = screenWidth * 0.08f;
        txtTitleDialog.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

        newTextSize = screenWidth * 0.04f;
        btnAdd.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
    }


}