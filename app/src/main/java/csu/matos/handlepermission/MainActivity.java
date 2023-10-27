package csu.matos.handlepermission;


import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.os.Environment.MEDIA_MOUNTED;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    RecyclerView recycler;
    ArrayList<String> images;
    ImageAdapter adapter;
    GridLayoutManager manager;

    TextView totalimages;


    ArrayList<String> permissionsList;
    String[] permissionsStr = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            MANAGE_EXTERNAL_STORAGE};
    /* Manifest.permission.CAMERA,
     Manifest.permission.SET_WALLPAPER,
     Manifest.permission.INTERNET,

     };*/
    int permissionsCount = 0;
    ActivityResultLauncher<String[]> permissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onActivityResult(Map<String, Boolean> result) {
                            ArrayList<Boolean> list = new ArrayList<>(result.values());
                            permissionsList = new ArrayList<>();
                            permissionsCount = 0;
                            for (int i = 0; i < list.size(); i++) {
                                if (shouldShowRequestPermissionRationale(permissionsStr[i])) {
                                    permissionsList.add(permissionsStr[i]);
                                } else if (!hasPermission(MainActivity.this, permissionsStr[i])) {
                                    ActivityCompat.requestPermissions(MainActivity.this, permissionsStr, permissionsCode);
                                    permissionsCount++;
                                }
                            }
                            if (permissionsList.size() > 0) {
                                //Some permissions are denied and can be asked again.
                                askForPermissions(permissionsList);
                            } else if (permissionsCount > 0) {
                                //Show alert dialog
                                showPermissionDialog();
                            } else {
                                //All permissions granted. Do your stuff 🤞
                            }
                        }
                    });
    int permissionsCode = 42;


    boolean isStorageImagePermitted = false;
    boolean isStorageVideoPermitted = false;
    boolean isStorageAudioPermitted = false;

    String TAG = "Permission";
    ClickListener clickListener;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R)
        {
            if (!Environment.isExternalStorageManager())
            {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                    startActivityIfNeeded(intent, 101);

                } catch (Exception e)
                {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    startActivityIfNeeded(intent,101);
                    e.printStackTrace();
                }
            }
        }*/
      /*  if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }*/
        setContentView(R.layout.activity_main);
       /* permissionsList = new ArrayList<>();
        permissionsList.addAll(Arrays.asList(permissionsStr));
        askForPermissions(permissionsList);*/


        if (isStorageImagePermitted == false) {
            requestPermissionStorageImage();
        } else {
            Toast.makeText(MainActivity.this, "you accepted the permission to read image", Toast.LENGTH_SHORT).show();
        }


        recycler = findViewById(R.id.gallery_recycler);
        images = new ArrayList<>();


        clickListener = new ClickListener() {
            @Override
            public void click(int index) {
                Toast.makeText(MainActivity.this,"clicked item index is "+index,Toast.LENGTH_LONG).show();
            }
        };
        adapter = new ImageAdapter(this, images,clickListener);
        manager = new GridLayoutManager(this, 3);
        totalimages = findViewById(R.id.gallery_total_images);

        recycler.setAdapter(adapter);
        recycler.setLayoutManager(manager);

        loadImages();
        recycler.getAdapter().notifyDataSetChanged();
       /* if (checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
            loadImages();
        }*/


    }


    private boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
/*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if (requestCode == PERMISSION_REQUEST_CODE) {
        if (grantResults.length > 0) {
            boolean accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (accepted) {
                loadImages();
            } else {
                Toast.makeText(this, "You have denied the permissions..", Toast.LENGTH_SHORT).show();
            }
        }

       /* try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        if (requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, perform your operations here
                // Load the image from SD card or any other tasks
                loadImages();
            } else {
                // Permission is denied, handle accordingly (e.g., show a message, disable functionality)
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        permission.checkResult(requestCode, permissions, grantResults);*/
       /* super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permission.checkResult(requestCode, permissions, grantResults);*/


    private void loadImages() {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);
        if (SDCard) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";

            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, order);
            int count = cursor.getCount();
            totalimages.setText("Total items: " + count);

            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                images.add(cursor.getString(columnindex));
            }

            recycler.getAdapter().notifyDataSetChanged();
            cursor.close();
        }
    }

    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);

        } else {
        /* User has pressed 'Deny & Don't ask again' so we have to show the enable permissions dialog
        which will lead them to app details page to enable permissions from there. */
            showPermissionDialog();
        }
    }

    AlertDialog alertDialog;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission required")
                .setMessage("Some permissions are need to be allowed to use this app without any problems.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    dialog.dismiss();
                });
        if (alertDialog == null) {
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private boolean hasPermission(Context context, String permissionStr) {
        return ContextCompat.checkSelfPermission(context, permissionStr) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (grantResults.length > 0) {
            boolean accepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (accepted) {
                loadImages();
            } else {
                Toast.makeText(this, "You have denied the permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }
/*
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "sms & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showDialogOK("SMS and Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }*/

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private boolean allPermissionResultCheck() {
        return isStorageAudioPermitted && isStorageImagePermitted && isStorageVideoPermitted;
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
    public void sendToSettingDialog()
    {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Alert for permission")
                .setMessage("Go to settings for Permissions")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent rIntent = new Intent();
                        rIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri= Uri.fromParts("package",getPackageName(),null);
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


}