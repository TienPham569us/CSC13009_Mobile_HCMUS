package com.example.imagesgallery.Activity;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Adapter.ImageAdapter;
import com.example.imagesgallery.Fragment.ImageFragment;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.example.imagesgallery.Utility.FileUtility;

public class ImageInfoActivity extends AppCompatActivity {
    public String imageTemp;
    public String nextImageTemp;
    private Context context;
    ImageView imageView;
    ImageAdapter myAdapter;
    int imagePosition = 0;
    TextView totalImages;
    ArrayList<String> images;
    ImageFragment imageFragment;
    String imagePath = "";
    Image image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_image);

        imageFragment = new ImageFragment();
        // Get the path to the image from the intent
        imagePath = getIntent().getStringExtra("image_path");
        String nextImagePath = getIntent().getStringExtra("next_image_path");
        imagePosition = getIntent().getExtras().getInt("position");
        image = (Image) getIntent().getSerializableExtra("image");
        if (image != null) {
            imagePath = image.getPath();
        }
        imageTemp = imagePath;
        nextImageTemp = nextImagePath;
        // Get the ImageView element
        imageView = findViewById(R.id.imageFullScreen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Toast.makeText(getApplicationContext(), "image position:" + imagePosition, Toast.LENGTH_SHORT).show();
        // Load the image into the ImageView element
        Glide.with(this).load(imagePath).into(imageView);
        //Log.d("onResult","First: "+new File(imagePath).getAbsolutePath());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check whether user go to this activity from AlbumInfoActivity or not
                String PreviousActivity = getIntent().getStringExtra("PreviousActivity");
                if (PreviousActivity != null && PreviousActivity.equals("AlbumInfoActivity")) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("isFavored", image.getIsFavored());
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    finish();
                }
            }
        });


        //Glide.with(this).load(nextImageTemp).into(imageView);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_info, menu);
        MenuItem disabledMenuItem = menu.findItem(R.id.RemoveImage);
        // Disable item if user go to this activity from ImageTab
        String PreviousActivity = getIntent().getStringExtra("PreviousActivity");
        if (disabledMenuItem != null && (PreviousActivity == null || !(PreviousActivity.equals("AlbumInfoActivity")))) {
            menu.findItem(R.id.RemoveImage).setVisible(false);
        }

        int isFavored = image.getIsFavored();
        if (isFavored == 1) {
            menu.findItem(R.id.removeImageFromFavorites).setVisible(true);
            menu.findItem(R.id.addImageToFavorites).setVisible(false);
        } else if (isFavored == 0) {
            menu.findItem(R.id.removeImageFromFavorites).setVisible(false);
            menu.findItem(R.id.addImageToFavorites).setVisible(true);
        }
        boolean isTrash = image.isTrash();
        if (isTrash == true) {
            menu.findItem(R.id.deleteImage).setVisible(false);
            menu.findItem(R.id.recoverImage).setVisible(true);

            //trash cannot hidden
            menu.findItem(R.id.addImageToHidden).setVisible(false);
            menu.findItem(R.id.removeImageFromHidden).setVisible(false);

        } else {
            menu.findItem(R.id.deleteImage).setVisible(true);
            menu.findItem(R.id.recoverImage).setVisible(false);

            boolean isHidden = image.isHidden();
            if (isHidden == false) {
                menu.findItem(R.id.addImageToHidden).setVisible(true);
                menu.findItem(R.id.removeImageFromHidden).setVisible(false);
            } else {
                menu.findItem(R.id.addImageToHidden).setVisible(false);
                menu.findItem(R.id.removeImageFromHidden).setVisible(true);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    int EDIT_IMAGE_REQUEST_CODE = 69;
    int EDIT_HIDDEN_IMAGE_REQUEST_CODE = 70;
    int EDIT_TRASH_IMAGE_REQUEST_CODE = 71;

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.addImage) {
            Toast.makeText(this, "Them hinh", Toast.LENGTH_SHORT).show();

        } else if (itemID == R.id.deleteImagePermanently) {
            createDialogDeleteImage();

            //Glide.with(this).load(nextImageTemp).into(imageView);
        } else if (itemID == R.id.infomation) {
            Intent intent = new Intent(ImageInfoActivity.this, DetailImageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("position", imagePosition);
            intent.putExtras(bundle);
            ImageInfoActivity.this.startActivity(intent);
        } else if (itemID == R.id.RemoveImage) {
            RemoveImageFromAlbum();
        } else if (itemID == R.id.setAsWallpaper) {
            setAsWallpaper();
        } else if (itemID == R.id.shareImage) {
            shareImage();
        } else if (itemID == R.id.addImageToFavorites) {
            addImageToFavorites();
            invalidateOptionsMenu();
        } else if (itemID == R.id.removeImageFromFavorites) {
            removeAlbumFromFavorites();
            invalidateOptionsMenu();
        } else if (itemID == R.id.seeDescription) {
            seeDescriptionImage();
        } else if (itemID == R.id.editImage) {
            editImage();
        } else if (itemID == R.id.addImageToHidden) {
            addImageToHiddenFolder();
            invalidateOptionsMenu();
        } else if (itemID == R.id.removeImageFromHidden) {
            removeImageFromHiddenFolder();
            invalidateOptionsMenu();
        } else if (itemID == R.id.deleteImage) {
            addImageToTrashFolder();
            invalidateOptionsMenu();
        } else if (itemID == R.id.recoverImage) {
            removeImageFromTrashFolder();
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    public void addImageToHiddenFolder() {
        File sourceImage = new File(imagePath);
        String hiddenFolderPath = Environment.getExternalStorageDirectory() + File.separator + ".hidden_image_folder";
        FileUtility.moveImageToFolder(sourceImage, hiddenFolderPath);
        Intent intent = new Intent(ImageInfoActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void removeImageFromHiddenFolder() {
        File sourceImage = new File(imagePath);
        String picturesFolder = Environment.getExternalStorageDirectory() + "/Pictures";
        FileUtility.moveImageToFolder(sourceImage, picturesFolder);
        Intent intent = new Intent(ImageInfoActivity.this, MainActivity.class);
        startActivity(intent);
        finishAndRemoveTask();
    }

    public void addImageToTrashFolder() {
        File sourceImage = new File(imagePath);
        String trashFolderPath = Environment.getExternalStorageDirectory()+File.separator+".trash_image_folder";
        FileUtility.moveImageToFolder(sourceImage, trashFolderPath);
        Intent intent = new Intent(ImageInfoActivity.this,MainActivity.class);
        startActivity(intent);
    }

    public void removeImageFromTrashFolder() {
        File sourceImage = new File(imagePath);
        String picturesFolder = Environment.getExternalStorageDirectory()+"/Pictures";
        FileUtility.moveImageToFolder(sourceImage, picturesFolder);
        Intent intent = new Intent(ImageInfoActivity.this,MainActivity.class);
        startActivity(intent);
        finishAndRemoveTask();
    }



    private void editImage() {
        Intent editIntent = new Intent(ImageInfoActivity.this, DsPhotoEditorActivity.class);
        // Set data
        editIntent.setData(Uri.fromFile(new File(imagePath)));
        // Set output directory
        //String location = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures";
        editIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "");//"Images Gallery"
        // Set toolbar color
        editIntent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR, Color.parseColor("#FF000000"));
        // Set background color
        editIntent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR, Color.parseColor("#FF000000"));

        //editIntent.putExtra(MediaStore.Images.Media.DATE_TAKEN,System.currentTimeMillis());
        /*ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Custom album group 9");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        Uri tempImageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        editIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri);*/

        /*editIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE,new int[]{
                DsPhotoEditorActivity.TOOL_WARMTH,
                DsPhotoEditorActivity.TOOL_PIXELATE});*/

        boolean isTrash = image.isTrash();
        boolean isHidden = image.isHidden();

        //Log.d("onResult","Trash: "+isTrash+" - Hidden: "+isHidden);
        // Start activity
        if (isTrash==true) {
            startActivityForResult(editIntent,EDIT_TRASH_IMAGE_REQUEST_CODE );
        } else if (isHidden==true) {
            startActivityForResult(editIntent,EDIT_HIDDEN_IMAGE_REQUEST_CODE );
        } else {

            startActivityForResult(editIntent, EDIT_IMAGE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Log.d("onResult", String.valueOf(requestCode));
        if (requestCode == EDIT_IMAGE_REQUEST_CODE) {
            // Handle the result of the edit image activity here
            if (resultCode == RESULT_OK) {

                Uri editedImageUri = data.getData();
                setDateTimeOriginal(editedImageUri);

                // update database
                ContentValues contentValues = new ContentValues();
                contentValues.put("path", editedImageUri.getPath());
                contentValues.put("description", "");
                contentValues.put("isFavored", 0);
                MainActivity.db.insert("Image", null, contentValues);

            } else if (resultCode == RESULT_CANCELED) {
                // The edit image activity was cancelled
                // Handle cancellation if needed
            }
        } else if (requestCode == EDIT_TRASH_IMAGE_REQUEST_CODE) {
            // Handle the result of the edit image activity here
            if (resultCode == RESULT_OK) {

                Uri editedImageUri = data.getData();
                setDateTimeOriginal(editedImageUri);

                //Log.d("onResult","This is trash image");

                String filePath = getFilePathFromFileURI(editedImageUri);
                if (filePath!=null) {
                    File newFile = new File(filePath);
                    //Log.d("onResult", "Before: " + newFile.getAbsolutePath());

                    String trashFolder = Environment.getExternalStorageDirectory()+File.separator+".trash_image_folder";
                    FileUtility.moveImageToFolder(newFile,trashFolder);

                    //Log.d("onResult", "After:" + newFile.getAbsolutePath());
                    // update database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("path", editedImageUri.getPath());
                    contentValues.put("description", "");
                    contentValues.put("isFavored", 0);
                    MainActivity.db.insert("Image", null, contentValues);
                } else {
                    Log.d("onResult", "File path is null");
                }



            }
        } else if (requestCode == EDIT_HIDDEN_IMAGE_REQUEST_CODE) {
            // Handle the result of the edit image activity here
            if (resultCode == RESULT_OK) {

                Uri editedImageUri = data.getData();
                setDateTimeOriginal(editedImageUri);

                //Log.d("onResult","This is hidden image");

                String filePath = getFilePathFromFileURI(editedImageUri);
                if (filePath!=null) {
                    File newFile = new File(filePath);
                    //Log.d("onResult", "Before: " + newFile.getAbsolutePath());

                    String hiddenFolder = Environment.getExternalStorageDirectory() + File.separator + ".hidden_image_folder";
                    FileUtility.moveImageToFolder(newFile, hiddenFolder);
                    //Log.d("onResult", "After:" + newFile.getAbsolutePath());
                    // update database
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("path", editedImageUri.getPath());
                    contentValues.put("description", "");
                    contentValues.put("isFavored", 0);
                    MainActivity.db.insert("Image", null, contentValues);
                } else {
                    Log.d("onResult", "File path is null");
                }

            }
        }
    }

    public String getFilePathFromFileURI(Uri uri ) {
        // Resolve the Uri to obtain the file path
        String filePath = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            filePath = uri.getPath();
        }
        return filePath;

        /*// Create the File object from the file path
        if (filePath != null) {
            File file = new File(filePath);
            // Use the file object as needed
        } else {
            // Handle the case where the file path is null or cannot be resolved
        }*/
    }
    private void setDateTimeOriginal(@NonNull Uri imageUri) {
        try {
            File newImageFile = new File(imageUri.getPath());

            // Create an ExifInterface for the new image file
            ExifInterface exifInterface = new ExifInterface(newImageFile.getAbsolutePath());

            // Get the current date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
            String currentDateTime = dateFormat.format(new Date());

            // Set the "Date Taken" attribute
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME, currentDateTime);
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, currentDateTime);

            // Update TAG_OFFSET_TIME_ORIGINAL
            TimeZone timeZone = TimeZone.getDefault();
            String offsetTime = timeZone.getOffset(Calendar.getInstance().getTimeInMillis()) / 60000 + "0"; // Offset in minutes
            exifInterface.setAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL, offsetTime);

            // Save the modified EXIF attributes
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle any exceptions
        }
    }
    private void seeDescriptionImage() {
        Intent intent = new Intent(ImageInfoActivity.this, DescriptionActivity.class);
        intent.putExtra("image", (Serializable) image);
        startIntentSeeeDescription.launch(intent);
    }

    private void addImageToFavorites() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("isFavored", 1);
        String[] args = {image.getPath()};
        long rowID = MainActivity.db.update("Image", contentValues, "path = ?", args);
        if (rowID > 0) {
            image.setIsFavored(1);
        }
    }

    private void removeAlbumFromFavorites() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("isFavored", 0);
        String[] args = {image.getPath()};
        long rowID = MainActivity.db.update("Image", contentValues, "path = ?", args);
        if (rowID > 0) {
            image.setIsFavored(0);
        }
    }

    private void shareImage() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        shareImageAndText(bitmap);
    }

    private void shareImageAndText(Bitmap bitmap) {
        Uri uri = getImageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, "Image Text");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Image Subject");
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private Uri getImageToShare(Bitmap bitmap) {
        File folder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            folder.mkdirs();
            File file = new File(folder, "image.ipg");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            uri = FileProvider.getUriForFile(this, "com.example.imagesgallery.Activity", file);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return uri;
    }

    //AT
    private void setAsWallpaper() {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            // Load the image from the file
            Uri imageUri = Uri.fromFile(new File(imagePath));
            wallpaperManager.setStream(getContentResolver().openInputStream(imageUri));
            Toast.makeText(this, "Wallpaper set successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("Set Wallpaper", "Error setting wallpaper", e);
            Toast.makeText(this, "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
        }
    }
    //Update set as wallpaper


    public void createDialogDeleteImage() {
//        setTheme(android.R.style.Theme_Material_Dialog);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete these images ?");

        // click yes
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteImage();
            }
        });
        // click no
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button noButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button yesButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                yesButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lavender));
                noButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lavender)); // Change to your desired color resource
            }
        });
        dialog.show();
    }

    private void deleteImage() {
        File deleteImage = new File(imageTemp);
        if (deleteImage.exists()) {
            if (deleteImage.delete()) {
                // ArrayList<String> newImageList= myAdapter.getImages_list();
                //newImageList.remove(imageTemp);
                //myAdapter.setImages_list(newImageList);
                //myAdapter.notifyItemRemoved(imagePosition);
                //myAdapter.notifyDataSetChanged();
                // change database
                String[] args = {imageTemp};
                long rowID = MainActivity.db.delete("Image", "path = ?", args);
                long rowID2 = MainActivity.db.delete("Album_Contain_Images", "path = ?", args);

                if (rowID > 0 && rowID2 > 0) {
                    Toast.makeText(this, "Delete success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                }


                // Sau khi xóa tệp tin, thông báo cho MediaScanner cập nhật thư viện ảnh
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{imageTemp}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        //imageView = findViewById(R.id.imageFullScreen);
                        //Glide.with(context).load(nextImageTemp).into(imageView);
                    }
                });

                String PreviousActivity = getIntent().getStringExtra("PreviousActivity");
                if (Objects.equals(PreviousActivity, "AlbumInfoActivity")) {
                    // return to previous activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("ImageDeleted", imageTemp);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Xóa Activity Stack
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Tạo mới Task
                        startActivity(intent);
                    }
                }
            }
        }
    }

    private void RemoveImageFromAlbum() {
        int id_album = getIntent().getIntExtra("id_album", -1);

        String[] args = {String.valueOf(id_album), image.getPath()};
        String sql = "DELETE FROM Album_Contain_Images WHERE id_album = ? AND path = ?";
        MainActivity.db.execSQL(sql, args);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("ImageRemoved", image.getPath());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    // when click button back in toolbar or in smartphone to finish DescriptionActivity
    ActivityResultLauncher<Intent> startIntentSeeeDescription = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        // get result from DescriptionActivity and change description
                        String description = data.getStringExtra("description");
                        image.setDescription(description);
                    }
                }
            }
    );
}

