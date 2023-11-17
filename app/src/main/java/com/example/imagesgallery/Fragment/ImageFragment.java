package com.example.imagesgallery.Fragment;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.isExternalStorageManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentResolver;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private ActivityResultLauncher<Intent> launcher_for_camera;
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
    int REQUEST_IMAGE_CAPTURE=100;
    int permissionsCode = 42;
    boolean isStorageImagePermitted = false;
    boolean isStorageVideoPermitted = false;
    boolean isStorageAudioPermitted = false;

    //AT: Add button multiSelectButton
    Button multiSelectButton;
    Button deleteButton;
    Button slideshowButton;
    boolean multiSelectMode = false;

    private Uri imageUri;
    private String imageUrl;
    private Bitmap thumbnail;
    //AT

    ImageButton imageBtnCamera;

    String TAG = "Permission";
    //private ActivityResultLauncher<String> requestPermissionLauncher;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=(MainActivity) getActivity();
        launcher_for_camera =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode()==RESULT_OK) {
                                /*try {
                                    thumbnail = MediaStore.Images.Media.getBitmap(
                                            mainActivity.getContentResolver(),imageUri);
                                    imageUrl = getPathFromUri(mainActivity,imageUri);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }*/
                                    triggerMediaScan(imageUri);
                                }
                            }
                        }
                );
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
        imageBtnCamera =(ImageButton) linearLayout.findViewById(R.id.imageBtnCamera);
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


        imageBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(),"Open camera",Toast.LENGTH_SHORT).show();
                openCamera();
            }
        });


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
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(mainActivity, "Camera permission denied", Toast.LENGTH_SHORT).show();
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

   /* private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Save the captured image to external storage
            String imageFilePath = saveImageToExternalStorage(imageBitmap);
            if (imageFilePath != null) {
                Toast.makeText(getContext(), "Save image successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }*/
    /*private void openCamera() {
        mainActivity.requestPermissionManageExternalStorage();
        mainActivity.requestPermissionCamera();
        mainActivity.requestPermissionWriteExternalStorage();
        &&
                ContextCompat.checkSelfPermission(mainActivity, MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ) {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(mainActivity.getPackageManager()) != null)  {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Camera permission not approve", Toast.LENGTH_SHORT).show();
            requestCameraPermission();
        }

    }*/
   private void triggerMediaScan(Uri imageUri) {
       File tempFile = new File(imageUri.toString());
//       String[] filePaths = {imageUri.getPath()};
       String[] filePaths = {tempFile.getAbsolutePath()};
       MediaScannerConnection.scanFile(
               mainActivity,
               filePaths,
               null,
               new MediaScannerConnection.OnScanCompletedListener() {
                   @Override
                   public void onScanCompleted(String path, Uri uri) {


                   }
               }
       );
       Image newImage = new Image(imageUri.getPath(),"captured image",0);
       images.add(0,newImage);
       //adapter.addImage(newImage);
       adapter.notifyDataSetChanged();
       adapter.notifyItemInserted(0);
       adapter.notifyItemRangeInserted(0,10);
   }

    protected void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Custom album group 9");
        imageUri =mainActivity.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        launcher_for_camera.launch(cameraIntent);
        /*Image newImage = new Image(imageUri.getPath(),"captured image",0);
        images.add(0,newImage);
        //adapter.addImage(newImage);
        adapter.notifyDataSetChanged();
        adapter.notifyItemInserted(0);*/
    }


    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Save the captured image to external storage
           // String imageFilePath = saveImageToExternalStorage(imageBitmap);
            //String imageFilePath  = saveImageToMediaStore(imageBitmap);
            //String imageFilePath  = saveImageToMediaStore2(imageBitmap);
            String imageFilePath  = saveImageToMediaStore3(imageBitmap);
            if (imageFilePath != null) {
                // Image saved successfully, do something with the file path
                // ...
                Toast.makeText(getContext(), "Image path: " + imageFilePath, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private String saveImageToExternalStorage(Bitmap imageBitmap) {
        String imageFileName = "JAVA_ANDROID_ALBUM_GROUP_9_IMG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            Toast.makeText(getContext(), "Save image successfully", Toast.LENGTH_SHORT).show();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String saveImageToMediaStore(Bitmap imageBitmap)  {
        String imageFileName = "ALBUM_GROUP_9_IMG_" + System.currentTimeMillis() + ".jpg";
        /*ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        final ContentResolver contentResolver = getContext().getContentResolver();

        Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);*/

        // Get the directory for saving images in MediaStore
        File imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //File imagesDir = Environment.getExternalStorageDirectory();
        Toast.makeText(mainActivity, imagesDir.toString(), Toast.LENGTH_SHORT).show();
        File imageFile = new File(imagesDir, imageFileName);

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(imageFile);

            if (outputStream!=null) {
                imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                //Toast.makeText(mainActivity, "Image saved to MediaStore successfully", Toast.LENGTH_SHORT).show();
                final ContentResolver contentResolver = mainActivity.getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                MediaScannerConnection.scanFile(mainActivity,new String[]{imageFile.getAbsolutePath()},null,null);

            }
        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream!=null) {
                    outputStream.close();
                }
                return imageFileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

       return null;
    }
    public String saveImageToMediaStore2(Bitmap imageBitmap) {

        String imageFileName = "ALBUM_G9_IMG_" + System.currentTimeMillis()  + ".jpg";

        File storageDir = Environment.getExternalStorageDirectory();

        // Create the directory for the custom album
        File albumDir = new File(storageDir, "captured image");
        albumDir.mkdirs();

        // Create the image file within the album directory
        File imageFile = new File(albumDir, imageFileName);

        // Create the content values for the image file
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        // Get the content resolver
        ContentResolver resolver = mainActivity.getContentResolver();

        // Insert the image file into MediaStore
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Open an output stream for the image file
        OutputStream outputStream = null;
        try {
            outputStream = resolver.openOutputStream(imageUri);
            if (outputStream != null) {
                // Compress the bitmap and write it to the output stream
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the output stream
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Trigger media scanner to scan the newly added image file
        MediaScannerConnection.scanFile(mainActivity, new String[]{imageUri.getPath()}, null, null);
        return imageFileName;
    }
    public String saveImageToMediaStore3(Bitmap imageBitmap) {

        String imageFileName = "ALBUM_G9_IMG_" + System.currentTimeMillis()  + ".jpg";

        // Create the content values for the image file
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        // Get the content resolver
        ContentResolver resolver = mainActivity.getContentResolver();

        // Insert the image file into MediaStore
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Open an output stream for the image file
        OutputStream outputStream = null;
        try {
            outputStream = resolver.openOutputStream(imageUri);
            if (outputStream != null) {
                // Compress the bitmap and write it to the output stream
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                // Get the file path from the imageUri
                String imagePath = getPathFromUri(mainActivity, imageUri);

                // Move the image file to the SD card/Camera folder
                File imageFile = new File(imagePath);

                //File storageDir = new File(Environment.getExternalStorageDirectory(),"DCIM/Camera");
                File storageDir = mainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File albumDir = new File(storageDir, "Album Group 09/Captured Image");
                /*if (!albumDir.exists()) {
                    albumDir.mkdirs();
                }*/
                //albumDir.mkdirs();
                File destination = new File(storageDir, imageFileName);
                Toast.makeText(mainActivity, destination.toString(),Toast.LENGTH_SHORT).show();
                if (mainActivity.hasManageExternalStoragePermission()) {
                    Toast.makeText(mainActivity, "Can access external memory",Toast.LENGTH_SHORT).show();
                    if (imageFile.renameTo(destination)) {
                        // Update the imageUri with the new file path
                        values.put(MediaStore.Images.Media.DATA, destination.getAbsolutePath());
                        resolver.update(imageUri, values, null, null);
                        // Trigger media scanner to scan the newly added image file
                    }
                    MediaScannerConnection.scanFile(mainActivity, new String[]{destination.getAbsolutePath()}, null, null);

                } else {
                    Toast.makeText(mainActivity, "Can't access external memory",Toast.LENGTH_SHORT).show();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the output stream
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return imageFileName;
    }


    // Helper method to get the file path from Uri
    private String getPathFromUri(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

}