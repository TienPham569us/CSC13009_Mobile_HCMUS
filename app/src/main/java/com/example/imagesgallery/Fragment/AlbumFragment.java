package com.example.imagesgallery.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.imagesgallery.Activity.AlbumInfoActivity;
import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.Adapter.AlbumAdapter;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;


public class AlbumFragment extends Fragment {

    GridView gridView;
    ArrayList<Album> albumArrayList;
    AlbumAdapter albumAdapter;
    ImageButton btnAddAlbum;
    Button btnAdd, btnCancel;
    EditText edtNameAlbum;
    TextView txtTitleDialog;
    Dialog dialog;
    MainActivity mainActivity;
    FrameLayout frameLayoutAlbum;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=(MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        frameLayoutAlbum = (FrameLayout) inflater.inflate(R.layout.fragment_album, container, false);
        init();

        // display gridview
        addToGridview();

        // when click button add of activity
        btnAddAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        // when click item of girdview
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(mainActivity, AlbumInfoActivity.class);
                intent.putExtra("album", (Serializable) albumArrayList.get(i));
                startActivity(intent);
            }
        });
        return frameLayoutAlbum;
    }

    private void init() {
        albumArrayList = new ArrayList<>();
        albumAdapter = new AlbumAdapter(mainActivity, albumArrayList);

        gridView = (GridView) frameLayoutAlbum.findViewById(R.id.gridview_album);
        btnAddAlbum = (ImageButton) frameLayoutAlbum.findViewById(R.id.btnAdd_album);
    }

    // add data to grid view and display them
    private void addToGridview() {
        gridView.setAdapter(albumAdapter);
        albumArrayList.add(new Album(new Image(R.drawable.image1), "Image1111111111111111111111", "Des1"));
        albumArrayList.add(new Album(new Image(R.drawable.image2), "Image2", "Des2"));
        albumArrayList.add(new Album(new Image(R.drawable.image3), "I3", "Des3"));
        albumArrayList.add(new Album(new Image(R.drawable.image4), "I3", "Des1"));
        albumArrayList.add(new Album(new Image(R.drawable.image5), "I3", "Des1"));
        albumArrayList.add(new Album(new Image(R.drawable.image5), "I3", "Des1"));
        albumArrayList.add(new Album(new Image(R.drawable.image5), "I3", "Des1"));
        albumArrayList.add(new Album(new Image(R.drawable.image5), "I3", "Des1"));
        albumArrayList.add(new Album(new Image(R.drawable.image5), "I3", "Des1"));
        albumArrayList.add(new Album(new Image(R.drawable.image5), "I3", "Des1"));
        albumArrayList.add(new Album(new Image(R.drawable.image5), "I3", "Des1"));
        albumArrayList.add(new Album(new Image(R.drawable.image5), "I3", "Des1"));
        albumAdapter.notifyDataSetChanged();
    }

    // show dialog when click button add album
    private void showDialog() {
        dialog = new Dialog(mainActivity);
        dialog.setContentView(R.layout.dialog_add_album);

        btnAdd = (Button) dialog.findViewById(R.id.buttonAdd);
        btnCancel = (Button) dialog.findViewById(R.id.buttonCancel);
        edtNameAlbum = (EditText) dialog.findViewById(R.id.edtAlbumName);
        txtTitleDialog = (TextView) dialog.findViewById(R.id.title_dialog);

        // when click button add of dialog
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtNameAlbum.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(mainActivity, "Bạn chưa nhập tên cho album", Toast.LENGTH_SHORT).show();
                } else {
                    albumArrayList.add(new Album(new Image(R.drawable.no_image), name, ""));
                    albumAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });

        // when click button cancel of dialog
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        resizeDialog();
    }

    // resize the dialog to fit the screen size
    private void resizeDialog() {
        // resize dialog size
        Display display = ((WindowManager) mainActivity.getSystemService(mainActivity.getApplicationContext().WINDOW_SERVICE)).getDefaultDisplay();
        int width = mainActivity.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        Objects.requireNonNull(dialog.getWindow()).setLayout((6 * width) / 7, ViewGroup.LayoutParams.WRAP_CONTENT);

        // get screen size
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        float screenWidth = displayMetrics.widthPixels;

        // resize text size
        float newTextSize = screenWidth * 0.05f;
        edtNameAlbum.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

        newTextSize = screenWidth * 0.08f;
        txtTitleDialog.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

        newTextSize = screenWidth * 0.04f;
        btnAdd.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
    }

}