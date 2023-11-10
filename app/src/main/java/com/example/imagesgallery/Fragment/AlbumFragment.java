package com.example.imagesgallery.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


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
    ConstraintLayout constraintLayoutAlbum;
    ContentValues rowValues;
    int clickPosition = -1;
    ImageView imgCheckAlbum;
    Toolbar toolbar;
    private int currentMaxPosition = 0;
    private final int ItemsPerLoading = 10;
    boolean isLoading = false;
    Handler handler;
    private boolean isAllItemsLoaded = false;
    private int IdMmaxWhenStartingLoadData = 0;
    private final String DefaultSearchName = "";
    private String SearchName = DefaultSearchName;
    SearchView searchView;
//    SharedPreferences sharedPreferences;
//    boolean isFirstRun = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();

        setHasOptionsMenu(true);

//        sharedPreferences = mainActivity.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
//        if (isFirstRun) {
//            isFirstRun = false;
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.clear();
//            editor.apply();
//        } else{
//            SearchName = sharedPreferences.getString("SearchName", "");
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        constraintLayoutAlbum = (ConstraintLayout) inflater.inflate(R.layout.fragment_album, container, false);
        init();
        albumArrayList = new ArrayList<>();
        albumAdapter = new AlbumAdapter(mainActivity, albumArrayList);
        rowValues = new ContentValues();
        gridView.setAdapter(albumAdapter);

        mainActivity.setSupportActionBar(toolbar);
        Objects.requireNonNull(mainActivity.getSupportActionBar()).setTitle("");

        handler = new Handler();

        // need to set them when load data to album tab the second time or more
        currentMaxPosition = 0;
        isAllItemsLoaded = false;
        IdMmaxWhenStartingLoadData = 0;

        // when click button add of activity
        btnAddAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        // when click button back in toolbar or in smartphone to finish AlbumInfoActivity
        ActivityResultLauncher<Intent> startIntentAlbumInfo = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String path = data.getStringExtra("path");
                            String description = data.getStringExtra("description");
                            long isDelete = data.getLongExtra("isDelete", 0);

                            if (isDelete != 0) {
                                albumArrayList.remove(clickPosition);
                            } else {
                                Album album = albumArrayList.get(clickPosition);
                                if (path != null) {
                                    Image image = album.getCover();
                                    image.setPath(path);
                                    album.setCover(image);
                                }
                                if (description != null) {
                                    album.setDescription(description);
                                }
                                albumArrayList.set(clickPosition, album);
                            }

                            albumAdapter.notifyDataSetChanged();
                        }
                    }
                }
        );

        // when click item of girdview
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(mainActivity, AlbumInfoActivity.class);
                clickPosition = i;
                intent.putExtra("album", (Serializable) albumArrayList.get(clickPosition));
                startIntentAlbumInfo.launch(intent);
            }
        });

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstItem, int visibelItemCount, int totalItemCount) {
                if (!isLoading && absListView.getLastVisiblePosition() == totalItemCount - 1 && !isAllItemsLoaded) {
                    isLoading = true;
                    // Create an executor that executes tasks in the main thread.
                    Executor mainExecutor = ContextCompat.getMainExecutor(mainActivity);
                    // Create an executor that executes tasks in a background thread.
                    ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
                    // Execute a task in the background thread.
                    backgroundExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            loadDataFromDatabase(SearchName);
                            // Update gridview on the main thread
                            mainExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    albumAdapter.notifyDataSetChanged();
                                    isLoading = false;
                                }
                            });
                        }
                    });
                }
            }
        });

        return constraintLayoutAlbum;
    }

    // Load album from database and add to arraylist
    private void loadDataFromDatabase(String SearchName) {
        String sql = "";
        Cursor cursor = null;
        if (IdMmaxWhenStartingLoadData == 0) {
            String[] argsAlbum = {String.valueOf(ItemsPerLoading), String.valueOf(currentMaxPosition)};
            try {
                sql = "SELECT MAX(id_album) FROM Album";
                cursor = MainActivity.db.rawQuery(sql, null);
            } catch (Exception exception) {
                return;
            }

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                IdMmaxWhenStartingLoadData = cursor.getInt(0);
            }
        }
        String[] argsAlbum = {String.valueOf(IdMmaxWhenStartingLoadData), "%" + SearchName + "%", String.valueOf(ItemsPerLoading), String.valueOf(currentMaxPosition)};
        try {
            sql = "SELECT * FROM Album WHERE id_album <= ? AND name LIKE ? ORDER BY id_album DESC LIMIT ? OFFSET ?";
            cursor = MainActivity.db.rawQuery(sql, argsAlbum);
        } catch (Exception exception) {
            return;
        }

        if (!cursor.moveToFirst()) {
            isAllItemsLoaded = true;
        }
        cursor.moveToPosition(-1);
        // load data and add album to arrayList
        while (cursor.moveToNext()) {
            int idAlbumColumn = cursor.getColumnIndex("id_album");
            int descriptionAlbumColumn = cursor.getColumnIndex("description");
            int nameAlbumColumn = cursor.getColumnIndex("name");
            int isFavoredAlbumColumn = cursor.getColumnIndex("isFavored");
            int coverAlbumColumn = cursor.getColumnIndex("cover");

            int idAlbum = cursor.getInt(idAlbumColumn);
            String descriptionAlbum = cursor.getString(descriptionAlbumColumn);
            String nameAlbum = cursor.getString(nameAlbumColumn);
            int isFavoredAlbum = cursor.getInt(isFavoredAlbumColumn);
            String coverAlbum = cursor.getString(coverAlbumColumn);

            String[] args = {coverAlbum};
            Cursor cursorImage = null;
            try {
                cursorImage = MainActivity.db.rawQuery("SELECT * FROM Image WHERE path = ?", args);
            } catch (Exception exception) {
                return;
            }
            cursorImage.moveToPosition(-1);
            int pathImageColumn = cursorImage.getColumnIndex("path");
            int descriptionImageColumn = cursorImage.getColumnIndex("description");
            int isFavoredImageColumn = cursorImage.getColumnIndex("isFavored");

            String pathImage = MainActivity.pathNoImage;
            String descriptionImage = "";
            int isFavoredImage = 0;

            while (cursorImage.moveToNext()) {
                descriptionImage = cursorImage.getString(descriptionImageColumn);
                isFavoredImage = cursorImage.getInt(isFavoredImageColumn);
                pathImage = cursorImage.getString(pathImageColumn);
            }
            cursorImage.close();
            albumArrayList.add(new Album(new Image(pathImage, descriptionImage, isFavoredImage), nameAlbum, descriptionAlbum, isFavoredAlbum, idAlbum, new ArrayList<>()));
        }
        cursor.close();
        currentMaxPosition += ItemsPerLoading;
    }

    private void init() {
        gridView = (GridView) constraintLayoutAlbum.findViewById(R.id.gridview_album);
        btnAddAlbum = (ImageButton) constraintLayoutAlbum.findViewById(R.id.btnAdd_album);
        toolbar = (Toolbar) constraintLayoutAlbum.findViewById(R.id.toolbar);
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
                    Toast.makeText(mainActivity, "Please enter the name of album", Toast.LENGTH_SHORT).show();
                } else {
                    rowValues.clear();
                    rowValues.put("description", "");
                    rowValues.put("isFavored", 0);
                    rowValues.put("name", name);
                    rowValues.put("cover", MainActivity.pathNoImage);
                    long rowId = MainActivity.db.insert("Album", null, rowValues);
                    albumArrayList.add(0, new Album(new Image(MainActivity.pathNoImage, "", 0), name, "", 0, (int) rowId, new ArrayList<>()));
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        mainActivity.getMenuInflater().inflate(R.menu.menu_album_home_page, menu);
        MenuItem menuItemSearch = menu.findItem(R.id.search_album);

        searchView = (SearchView) menuItemSearch.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        searchView.setQuery(SearchName,false);
        searchView.clearFocus();
        //View closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchName = query;
                currentMaxPosition = 0;
                isAllItemsLoaded = false;
                IdMmaxWhenStartingLoadData = 0;
                albumArrayList.clear();
                loadDataFromDatabase(SearchName);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")){
                    this.onQueryTextSubmit("");
                }
                return true;
            }
        });

//        closeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                currentMaxPosition = 0;
//                isAllItemsLoaded = false;
//                IdMmaxWhenStartingLoadData = 0;
//                albumArrayList.clear();
//                SearchName = "";
//                loadDataFromDatabase(SearchName);
//                searchView.setQuery(SearchName,false);
//                searchView.clearFocus();
//            }
//        });

        super.onCreateOptionsMenu(menu, inflater);
    }

//    @Override
//    public void onPause() {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("SearchName", SearchName);
//        editor.apply();
//        super.onPause();
//    }
}