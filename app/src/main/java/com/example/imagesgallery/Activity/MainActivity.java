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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.imagesgallery.Fragment.AlbumFragment;
import com.example.imagesgallery.Fragment.ImageFragment;
import com.example.imagesgallery.Interface.DownloadService;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

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
    boolean isSetWallpaperPermitted = false;
    boolean isManageExternalStoragePermitted = false;

    boolean isInternetAccessPermitted = false;
    CallbackManager callbackManager;
    ProgressDialog progressDialog;
    Dialog dialogNavBottom;
    Button btnFavoriteAlbums, btnFavoriteImages;

    //AT
    // Method to start the slideshow activity with selected images
    public void startSlideshowActivity(ArrayList<String> selectedImages) {
        Intent intent = new Intent(this, SlideshowActivity.class);
        intent.putStringArrayListExtra("selectedImages", selectedImages);
        startActivity(intent);
    }

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

        // Login Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this.getApplication());
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        /*callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));*/

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (!isStorageImagePermitted) {
            requestPermissionStorageImage();
        } else {
            Toast.makeText(MainActivity.this, "you accepted the permission to read image", Toast.LENGTH_SHORT).show();
        }
        if (!isCameraPermitted) {
            requestPermissionCamera();
        }
        if (!isInternetAccessPermitted) {
            requestInternetAccessPermission();
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
                } else if (item.getItemId() == R.id.itemBottomNav) {
                    showDialogNavBottom();
                }
                return false;
            }
        });
    }

    public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void hideBottomNavigationView() {
        bottomNavigationView.setVisibility(View.GONE);
    }

    public void showBottomNavigationView() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public void requestPermissionStorageImage() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permissionsStr[0]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[0] + " Granted");
            isStorageImagePermitted = true;

        } else {
            request_permission_launcher_storage_images.launch(permissionsStr[0]);

        }
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

    public void requestInternetAccessPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permissionsStr[3]) == PackageManager.PERMISSION_GRANTED) {
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

    public boolean hasManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return true;
        }
    }

    Button btnDownloadImage;

    private void showDialogNavBottom() {
        dialogNavBottom = new Dialog(MainActivity.this);
        dialogNavBottom.setContentView(R.layout.dialog_nav_bottom);

        dialogNavBottom.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogNavBottom.getWindow().setGravity(Gravity.BOTTOM);

        btnDownloadImage = (Button) dialogNavBottom.findViewById(R.id.buttonDownload);
        btnFavoriteAlbums = (Button) dialogNavBottom.findViewById(R.id.buttonFavoriteAlbums);
        btnDownloadImage = (Button) dialogNavBottom.findViewById(R.id.buttonFavoriteImages);
        btnDownloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Loading ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();

                String imageUrl = "https://en.wikipedia.org/wiki/Main_Page#/media/File:Australiformis_Distribution.png";
                downloadImage(imageUrl);
            }
        });

        btnFavoriteAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FavoriteAlbumsActivity.class);
                startActivity(intent);
            }
        });

        btnDownloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FavoriteImagesActivity.class);
                startActivity(intent);
            }
        });

        /*btnAdd = (Button) dialogNavBottom.findViewById(R.id.buttonAdd);
        btnCancel = (Button) dialogNavBottom.findViewById(R.id.buttonCancel);


        // when click button add of dialog
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "add",Toast.LENGTH_SHORT).show();
            }
        });*/

        // when click button cancel of dialog


        dialogNavBottom.show();

    }

    boolean writeFileToStorage(ResponseBody body) {
        String nameOfFile = "Downloaded_image" + System.currentTimeMillis() + ".jpg";

        File location = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                location = new File(String.valueOf(MediaStore.Downloads.EXTERNAL_CONTENT_URI), nameOfFile);

                if (location.exists()) {
                    location.delete();
                }
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, nameOfFile);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, nameOfFile);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                Uri uri = null;
                uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(uri, "w");
                FileDescriptor fileDescriptor = descriptor.getFileDescriptor();

                InputStream inputStream = body.byteStream();

                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                OutputStream outputstream = new FileOutputStream(fileDescriptor);
                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputstream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                    Log.d(TAG, "File download size: " + fileSizeDownloaded + " from " + fileSize);

                }

                outputstream.flush();

                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputstream != null) {
                    outputstream.close();
                }

                File readLocation = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS) + "/" + nameOfFile);
                Log.d(TAG, "Read location: " + readLocation);
                //setImageDrawable(drawable.createFromPath(readLocation.sotString()));


            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    public void downloadImage(String fileUrl) {
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(fileUrl);
        Retrofit retrofit = builder.build();

        DownloadService downloadService = retrofit.create(DownloadService.class);
        Call<ResponseBody> call = downloadService.downloadFileFromUrl(fileUrl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        boolean writeToDisk = writeFileToStorage(response.body());
                        Log.d(TAG, "file downloaded or not status -> " + writeToDisk);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "server connection error");
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Something went wrong");
                progressDialog.dismiss();
            }
        });
    }

}