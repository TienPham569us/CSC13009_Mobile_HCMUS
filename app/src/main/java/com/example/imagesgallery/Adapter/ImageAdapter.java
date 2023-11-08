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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.imagesgallery.Activity.ClickListener;
import com.example.imagesgallery.Activity.ImageInfoActivity;
import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.R;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    MainActivity mainActivity;
    private Context context ;
    private ArrayList<String> images_list;

    //AT: check if there is selected mode
    private boolean isMultiSelectMode = false;
    private ArrayList<Integer> selectedPositions = new ArrayList<>();

    private ArrayList<String> selectedImages; // New list to track selected images
    //AT

    public void setImages_list(ArrayList<String> images_list) {
        this.images_list = images_list;
    }

    public ArrayList<String> getImages_list() {
        return images_list;
    }

    ClickListener listener;
    public static  int DELETE_REQUEST_CODE = 15;
    public int pos = 0;
    int getPos(){
        return this.pos;
    }

    //AT: set multi select mode
    public void setMultiSelectMode(boolean multiSelectMode) {
        this.isMultiSelectMode = multiSelectMode;
    }
    public ArrayList<String> getSelectedImages() {
        return selectedImages;
    }
    public ArrayList<Integer> getSelectedPositions() { return selectedPositions; }
    //AT

    public ImageAdapter(Context context, ArrayList<String> images_list, ClickListener listener) {
        this.context = context;
        this.images_list = images_list;
        this.listener=listener;
        this.selectedImages = new ArrayList<>(); //For multi select
    }


    //  AT Toggle the selection state of an item at the given position
    public void toggleSelection(int position) {
        String imagePath = images_list.get(position);
        if (selectedImages.contains(imagePath)) {
            selectedImages.remove(imagePath);
        } else {
            selectedImages.add(imagePath);
        }
        notifyDataSetChanged(); // Update the UI to reflect the selection
    }
    //AT Clear the selection
    public void clearSelection() {
        selectedImages.clear();
        selectedPositions.clear();
        notifyDataSetChanged(); // Update the UI to clear selection
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
        File image_file = new File(images_list.get(position));
        if (image_file.exists()) {
            Glide.with(context).load(image_file).into(holder.image);
//            Log.d("imgAdapter", "1 + " + String.valueOf(position));
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
                    // Notify the adapter that the data set has changed
                    notifyDataSetChanged();
                    toggleSelection(position);
                    Log.d("selected images array ", selectedImages.toString());
                    Log.d("Image list size: ", String.valueOf(getItemCount()));
                    // Show the checkbox only for the clicked image and set it to true
                }

                if (!isMultiSelectMode) {
                    // Pass the position to the listener
                    listener.click(position);
                    context = view.getContext();
                    // Create an intent to start the new activity
                    Intent intent = new Intent(context, ImageInfoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    intent.putExtras(bundle);
                    intent.putExtra("image_path", images_list.get(position));
                    intent.putExtra("next_image_path", images_list.get(position + 1));

                    // Pass the path to the image to the new activity
                    // Start the new activity
                    context.startActivity(intent);
                    //Log.d("newTest", "onClick: 2");
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
