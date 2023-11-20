package com.example.imagesgallery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Activity.AlbumInfoActivity;
import com.example.imagesgallery.Activity.ClickListener;
import com.example.imagesgallery.Activity.ImageInfoActivity;
import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.Fragment.ImageFragment;
import com.example.imagesgallery.Model.Image;
import com.example.imagesgallery.R;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    MainActivity mainActivity;
    ImageFragment imageFragment = new ImageFragment();
    private Context context;
    private ActivityResultLauncher<Intent> activityLauncher;
    private ArrayList<Image> images_list;

    //AT: check if there is selected mode
    private boolean isMultiSelectMode = false;
    private ArrayList<Integer> selectedPositions = new ArrayList<>();

    private ArrayList<String> selectedImages; // New list to track selected images

    public interface SelectionChangeListener {
        void onSelectionChanged(boolean hasSelection);
    }

    private SelectionChangeListener selectionChangeListener;

    public void setSelectionChangeListener(SelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }
    //AT


    public ArrayList<Image> getImages_list() {
        return images_list;
    }

    public void setImages_list(ArrayList<Image> images_list) {
        this.images_list = images_list;
    }

    ClickListener listener;
    public static int DELETE_REQUEST_CODE = 15;
    public int pos = 0;

    int getPos() {
        return this.pos;
    }

    //AT: set multi select mode
    public void setMultiSelectMode(boolean multiSelectMode) {
        this.isMultiSelectMode = multiSelectMode;
    }

    public ArrayList<String> getSelectedImages() {
        return selectedImages;
    }

    public ArrayList<Integer> getSelectedPositions() {
        return selectedPositions;
    }
    //AT

    public ImageAdapter(Context context, ArrayList<Image> images_list, ClickListener listener) {
        this.context = context;
        this.images_list = images_list;
        this.listener = listener;
        this.selectedImages = new ArrayList<>(); //For multi select
    }

    public ImageAdapter(Context context, ActivityResultLauncher<Intent> activityLauncher, ArrayList<Image> images_list, ClickListener listener) {
        this.context = context;
        this.activityLauncher = activityLauncher;
        this.images_list = images_list;
        this.listener = listener;
        this.selectedImages = new ArrayList<>(); //For multi select
    }

    public void addImage(Image image){
        this.images_list.add(image);
    }
    //  AT Toggle the selection state of an item at the given position
    public void toggleSelection(int position) {
        String imagePath = images_list.get(position).getPath();
        if (selectedImages.contains(imagePath)) {
            selectedImages.remove(imagePath);
        } else {
            selectedImages.add(imagePath);
        }
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(!selectedImages.isEmpty());
        }
        notifyDataSetChanged(); // Update the UI to reflect the selection
    }

    //AT Clear the selection
    public void clearSelection() {
        selectedImages.clear();
        selectedPositions.clear();
        notifyDataSetChanged(); // Update the UI to clear selection
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(false);
        }
    }
    //AT

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File image_file = new File(images_list.get(position).getPath());
        if (image_file.exists()) {
            Glide.with(context).load(image_file).into(holder.image);
//            Log.d("imgAdapter",  String.valueOf(position));
        }

        //AT
        // Check if the item is selected and update its appearance
        boolean isSelected = selectedImages.contains(images_list.get(position));
        holder.itemView.setSelected(isSelected);
        // Initially set the checkbox visibility to GONE
  /*      if (isMultiSelectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(true);
        }*/
        if (selectedPositions.contains(position)) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }
        //AT

//        imageFragment.updateDeleteButtonState(selectedImages.size() > 0);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the position of the image
                int position = holder.getAdapterPosition();
                if (isMultiSelectMode) {
                    if (selectedPositions.contains(position)) {
                        selectedPositions.remove(Integer.valueOf(position));
                    } else {
                        selectedPositions.add(position);
                    }
                    toggleSelection(position);

                    // Notify the adapter that the data set has changed
                    notifyDataSetChanged();
                    Log.d("selected images array ", selectedImages.toString());
                    Log.d("Image list size: ", String.valueOf(getItemCount()));
                    // Show the checkbox only for the clicked image and set it to true
                }

                if (!isMultiSelectMode) {
                    // Pass the position to the listener
                    listener.click(position);
                    if (!(context instanceof AlbumInfoActivity)) {
                        context = view.getContext();
                        // Create an intent to start the new activity
                        Intent intent = new Intent(context, ImageInfoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("position", position);
                        intent.putExtras(bundle);
                        intent.putExtra("image_path", images_list.get(position).getPath());
                        //intent.putExtra("next_image_path", images_list.get(position + 1));

                        // Pass the path to the image to the new activity
                        // Start the new activity
                        context.startActivity(intent);
                        //Log.d("newTest", "onClick: 2");
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });


    }

    @Override
    public int getItemCount() {
        return images_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;

        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            image = itemView.findViewById(R.id.gallery_item);
        }
    }
}
