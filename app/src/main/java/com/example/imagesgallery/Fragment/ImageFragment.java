package com.example.imagesgallery.Fragment;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.os.Environment.MEDIA_MOUNTED;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentResolver;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagesgallery.Activity.ClickListener;
import com.example.imagesgallery.Activity.ImageInfoActivity;
import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ImageFragment extends Fragment implements ImageAdapter.SelectionChangeListener {
    RecyclerView recycler;
    ArrayList<Image> images;
    ImageAdapter adapter;
    GridLayoutManager manager;
    TextView totalimages;
    MainActivity mainActivity;

    ConstraintLayout constraintLayoutImage;
    LinearLayout linearLayout;
    ArrayList<String> permissionsList;
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
                                } else if (!hasPermission(mainActivity , permissionsStr[i])) {
                                    ActivityCompat.requestPermissions(mainActivity, permissionsStr, permissionsCode);
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

    //AT: Add button multiSelectButton
    Button multiSelectButton;
    Button deleteButton;
    Button slideshowButton;
    boolean multiSelectMode = false;

    //AT

    String TAG = "Permission";
    //private ActivityResultLauncher<String> requestPermissionLauncher;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=(MainActivity) getActivity();
    }
    ClickListener clickListener =new ClickListener() {
        @Override
        public void click(int index) {
            // Toast.makeText(mainActivity,"clicked item index is "+index,Toast.LENGTH_LONG).show();
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        linearLayout =(LinearLayout) inflater.inflate(R.layout.fragment_image, container, false);
        recycler =linearLayout.findViewById(R.id.gallery_recycler);
        images = new ArrayList<>();

        adapter = new ImageAdapter(mainActivity, images , clickListener);
        manager = new GridLayoutManager(mainActivity, 3);
        totalimages = linearLayout.findViewById(R.id.gallery_total_images);
        recycler.setLayoutManager(manager);
        recycler.setAdapter(adapter);
        loadImages();
        recycler.getAdapter().notifyDataSetChanged();

        //AT
        // Initialize the button
        multiSelectButton = linearLayout.findViewById(R.id.multiSelectButton);
        deleteButton = linearLayout.findViewById(R.id.deleteButton);
        slideshowButton = linearLayout.findViewById(R.id.slideshowButton);

        multiSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (multiSelectMode) {
                    multiSelectMode = false;
                    adapter.setMultiSelectMode(multiSelectMode);
                    adapter.clearSelection();
                    multiSelectButton.setText("Multi select");

                    // Handle actions in multi-select mode
                } else {
                    // Enter multi-select mode
                    multiSelectMode = true;
                    adapter.setMultiSelectMode(multiSelectMode);
                    // Update UI, e.g., change button text
                    multiSelectButton.setText("Cancel"); // Optionally, you can change the button label
                }
                toggleButtonsOfMultiSelectMode(multiSelectMode);
            }

        });
        deleteButton.setEnabled(false);
        slideshowButton.setEnabled(false);
        // Set state for buttons when in multi-select mode
        adapter.setSelectionChangeListener(this);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialogDeleteImage();
//                manager = new GridLayoutManager(mainActivity, 3);
//                recycler.setLayoutManager(manager);
//                loadImages();

                    // Handle actions in multi-select mode
            }
        });



        slideshowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> selectedImages = adapter.getSelectedImages();
                if (!selectedImages.isEmpty()) {
                    // Call the method in MainActivity to start the SlideshowActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).startSlideshowActivity(selectedImages);
                    }
                }
            }
        });
        //AT



        return linearLayout;
    }
    //AT
    private void deleteImage(String imagePath) {
        File deleteImage = new File(imagePath);
        if (deleteImage.exists()) {
            if (deleteImage.delete()) {
                // ArrayList<String> newImageList= myAdapter.getImages_list();
                //newImageList.remove(imageTemp);
                //myAdapter.setImages_list(newImageList);
                //myAdapter.notifyItemRemoved(imagePosition);
                //myAdapter.notifyDataSetChanged();
                // change database
                String[] args = {imagePath};
                long rowID = MainActivity.db.delete("Image", "path = ?", args);
                long rowID2 = MainActivity.db.delete("Album_Contain_Images", "path = ?", args);

                if (rowID > 0 && rowID2 > 0) {
                    Toast.makeText(getContext(), "Delete success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                }


                // Sau khi xóa tệp tin, thông báo cho MediaScanner cập nhật thư viện ảnh
                MediaScannerConnection.scanFile(getActivity().getApplicationContext(), new String[]{imagePath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        //imageView = findViewById(R.id.imageFullScreen);
                        //Glide.with(context).load(nextImageTemp).into(imageView);
                    }
                });

                String PreviousActivity = null;

                if (getActivity() != null) {
                    Intent activityIntent = getActivity().getIntent();
                    if (activityIntent != null) {
                        PreviousActivity = activityIntent.getStringExtra("PreviousActivity");
                    }
                }
                if (Objects.equals(PreviousActivity, "AlbumInfoActivity")) {
                    // return to previous activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("ImageDeleted", imagePath);
                    getActivity().setResult(Activity.RESULT_OK, resultIntent);
                    getActivity().finish();
                } else {
                    Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa Activity Stack
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Tạo mới Task
                        startActivity(intent);
                    }
                }
            }
        }
    }

    public void createDialogDeleteImage() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete these images ?");

        // click yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "onClick: delete image");

                ArrayList<String> selectedImages = adapter.getSelectedImages();
                Log.d(TAG,"selectedImages size " + adapter.getSelectedImages().size() );
                // Define variables to track the number of successfully deleted images
                for (String imagePath : selectedImages) {
                    deleteImage(imagePath);
                }
                toggleButtonsOfMultiSelectMode(multiSelectMode);
                multiSelectMode = false;
                adapter.setMultiSelectMode(multiSelectMode);
                adapter.clearSelection();
                Log.d("selected images: ", adapter.getSelectedImages().toString());
                multiSelectButton.setText("Multi select");
            }
        });
        // click no
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    };
    //AT When click button Multi Select, it shows Delete Button
    private void toggleButtonsOfMultiSelectMode(Boolean isMultiSelectMode) {
        if (isMultiSelectMode) {
            deleteButton.setVisibility(View.VISIBLE);
            slideshowButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
            slideshowButton.setVisibility(View.INVISIBLE);
        }
    }
    // Method to update the delete button's state
    // Implement the onSelectionChanged method from the SelectionChangeListener interface
    @Override
    public void onSelectionChanged(boolean hasSelection) {
        if (hasSelection) {
            // At least one image is selected, enable the deleteButton
            slideshowButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } else {
            // No images are selected, disable the deleteButton
            slideshowButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }
    //AT

    public void loadImages() {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            //Log.d("test","6");
            ContentResolver contentResolver = requireActivity().getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, order);
            int count = cursor.getCount();
            totalimages.setText("Total items: " + count);

            Thread insertThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ContentValues rowValues = new ContentValues();
                    for (int i = 0; i < count; i++) {
                        cursor.moveToPosition(i);
                        int columnindex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        String path = cursor.getString(columnindex);
                        Image newImage=new Image(path,"",0);
                        images.add(newImage);

                        rowValues.clear();
                        rowValues.put("path", path);
                        rowValues.put("description", "");
                        rowValues.put("isFavored", 0);
                        MainActivity.db.insert("Image", null, rowValues);
                    }
                }
            });
            insertThread.start();
        }
    }
    private boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.SEND_SMS);
        int locationPermission = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mainActivity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }



    private void askForPermissions(ArrayList<String> permissionsList) {
        String[] newPermissionStr = new String[permissionsList.size()];
        for (int i = 0; i < newPermissionStr.length; i++) {
            newPermissionStr[i] = permissionsList.get(i);
        }
        if (newPermissionStr.length > 0) {
            permissionsLauncher.launch(newPermissionStr);

        } else {

            showPermissionDialog();
        }
    }

    AlertDialog alertDialog;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
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
                Toast.makeText(mainActivity, "You have denied the permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mainActivity)
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
        if (ContextCompat.checkSelfPermission(mainActivity, permissionsStr[0]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, permissionsStr[0] + " Granted");
            isStorageImagePermitted = true;

        } else {
            request_permission_launcher_storage_images.launch(permissionsStr[0]);

        }
    }
    //Just a comment to change message when commit
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
        new AlertDialog.Builder(mainActivity)
                .setTitle("Alert for permission")
                .setMessage("Go to settings for Permissions")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent rIntent = new Intent();
                        rIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri= Uri.fromParts("package",mainActivity.getPackageName(),null);
                        rIntent.setData(uri);
                        startActivity(rIntent);
                        alertDialog.dismiss();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mainActivity.finish();
                    }
                });
    }

}