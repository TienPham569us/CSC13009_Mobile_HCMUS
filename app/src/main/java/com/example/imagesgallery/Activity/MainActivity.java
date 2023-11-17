package com.example.imagesgallery.Activity;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.imagesgallery.Fragment.AlbumFragment;
import com.example.imagesgallery.Fragment.ImageFragment;
import com.example.imagesgallery.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    public static SQLiteDatabase db;
    public static final String pathNoImage = "no_image";
    AlbumFragment albumFragment = new AlbumFragment();
    ImageFragment imageFragment = new ImageFragment();
    boolean isStorageImagePermitted = false;
    boolean isStorageVideoPermitted = false;
    boolean isStorageAudioPermitted = false;//no use now
    boolean isCameraPermitted = false;
    boolean isWriteExternalStoragePermitted = false;
    boolean isSetWallpaperPermitted = false;
    boolean isManageExternalStoragePermitted = false;
    boolean isInternetAccessPermitted = false;

    //AT
    // Method to start the slideshow activity with selected images
//    public void startSlideshowActivity(ArrayList<String> selectedImages) {
//        Intent intent = new Intent(this, SlideshowActivity.class);
//        intent.putStringArrayListExtra("selectedImages", selectedImages);
//        startActivity(intent);
//    }
    String[] permissionsStr = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.SET_WALLPAPER,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            MANAGE_EXTERNAL_STORAGE};
    AlertDialog alertDialog;
    String DatabaseName = "myDatabase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (!isStorageImagePermitted) {
            requestPermissionStorageImage();
        } else {
            Toast.makeText(MainActivity.this, "you accepted the permission to read image", Toast.LENGTH_SHORT).show();
        }
        if (isCameraPermitted == false) {
            requestPermissionCamera();
        } else {
            Toast.makeText(MainActivity.this, "you accepted the permission to use camera", Toast.LENGTH_SHORT).show();
        }
        if (!isManageExternalStoragePermitted) {
            requestPermissionManageExternalStorage();
        }  else {
            Toast.makeText(MainActivity.this, "you accepted the permission to manage external storage", Toast.LENGTH_SHORT).show();
        }
        if (!isInternetAccessPermitted) {
            requestPermissionAccessInternet();
        }
        if (!hasManageExternalStoragePermission()) {
            requestManageExternalStoragePermission();
        }
        // create database
        File storagePath = getApplication().getFilesDir();
        String myDbPath = storagePath + "/" + DatabaseName;
        try {
            db = SQLiteDatabase.openDatabase(myDbPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
            db.execSQL("CREATE TABLE IF NOT EXISTS Album(id_album INTEGER PRIMARY KEY AUTOINCREMENT, description TEXT, cover INTEGER, name TEXT, isFavored INTEGER)");
            db.execSQL("CREATE TABLE IF NOT EXISTS Image(path TEXT PRIMARY KEY,  description TEXT, isFavored INTEGER)");
            db.execSQL("CREATE TABLE IF NOT EXISTS Album_Contain_Images(id INTEGER PRIMARY KEY AUTOINCREMENT, id_album INTEGER, path TEXT)");
        } catch (SQLiteException ignored) {
        }


        // Set default fragment when open app
        getSupportFragmentManager().beginTransaction().replace(R.id.container, imageFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.album) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, albumFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.image) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, imageFragment).commit();
                    return true;
                }
                return false;
            }
        });
    }

    public void hideBottomNavigationView() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    public void showBottomNavigationView() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void requestPermissionManageExternalStorage() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,permissionsStr[10])==PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[10] +" Granted");
            isManageExternalStoragePermitted=true;
        } else  {
            request_permission_launcher_manage_external_storage.launch(permissionsStr[10]);
        }
//        request_permission_launcher_manage_external_storage.launch(permissionsStr[10]);

    }
    private ActivityResultLauncher<String> request_permission_launcher_manage_external_storage =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d(TAG, permissionsStr[10] + " Granted");
                            isManageExternalStoragePermitted=true;

                        } else {
                            Log.d(TAG, permissionsStr[10] + " Not Granted");
                            isManageExternalStoragePermitted=false;
                            sendToSettingDialog();
                        }
                    });

    public void requestPermissionWriteExternalStorage() {
       /* if (ContextCompat.checkSelfPermission(MainActivity.this,permissionsStr[9])==PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[9] +" Granted");
            isWriteExternalStoragePermitted=true;
        } else  {
            request_permission_launcher_write_external_storage.launch(permissionsStr[9]);
        }*/
        request_permission_launcher_write_external_storage.launch(permissionsStr[9]);
    }
    private ActivityResultLauncher<String> request_permission_launcher_write_external_storage =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d(TAG, permissionsStr[9] + " Granted");
                            isWriteExternalStoragePermitted=true;

                        } else {
                            Log.d(TAG, permissionsStr[9] + " Not Granted");
                            isWriteExternalStoragePermitted=false;
                            sendToSettingDialog();
                        }
                    });

    public void requestPermissionAccessInternet() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,permissionsStr[3])==PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[3] + " Granted");
            isInternetAccessPermitted = true;

        } else {
            request_permission_launcher_internet_access.launch(permissionsStr[3]);
        }
    }
    private ActivityResultLauncher<String> request_permission_launcher_internet_access =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d(TAG, permissionsStr[3] + " Granted");
                            isInternetAccessPermitted = true;
                        } else {
                            Log.d(TAG, permissionsStr[3] + " Not Granted");
                            isInternetAccessPermitted = false;
                            sendToSettingDialog();
                        }
                    });
    public void requestPermissionStorageImage() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permissionsStr[0]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[0] + " Granted");
            isStorageImagePermitted = true;

        } else {
            request_permission_launcher_storage_images.launch(permissionsStr[0]);
        }
       // request_permission_launcher_storage_images.launch(permissionsStr[0]);

    }

    private ActivityResultLauncher<String> request_permission_launcher_storage_images =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d(TAG, permissionsStr[0] + " Granted");
                            isStorageImagePermitted = true;
                        } else {
                            Log.d(TAG, permissionsStr[0] + " Not Granted");
                            isStorageImagePermitted = false;
                            sendToSettingDialog();
                        }
                    });

    public void sendToSettingDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Alert for permission")
                .setMessage("Go to settings for Permissions")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent rIntent = new Intent();
                        rIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        rIntent.setData(uri);
                        startActivity(rIntent);
                        alertDialog.dismiss();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
    }

    public void requestPermissionCamera() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permissionsStr[4]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[4] + " Granted");
            isCameraPermitted = true;

        } else {
            request_permission_launcher_camera.launch(permissionsStr[4]);

        }
    }

    private ActivityResultLauncher<String> request_permission_launcher_camera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Log.d(TAG, permissionsStr[4] + " Granted");
                            isCameraPermitted = true;
                        } else {
                            Log.d(TAG, permissionsStr[4] + " Not Granted");
                            isCameraPermitted = false;
                            sendToSettingDialog();
                        }
                    });
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 100;

    private void requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted, perform the operations that require access to external storage.
                } else {
                    // Permission denied, handle accordingly (e.g., show a message or disable functionality).
                }
            }
        }
    }
    public boolean hasManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            // For versions prior to Android 11, the permission is granted automatically.
            return true;
        }
    }
}