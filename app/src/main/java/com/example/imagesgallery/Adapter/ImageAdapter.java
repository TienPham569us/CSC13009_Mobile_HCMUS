package com.example.imagesgallery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    public ImageAdapter(Context context, ArrayList<String> images_list, ClickListener listener) {
        this.context = context;
        this.images_list = images_list;
        this.listener=listener;
    }


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
            Log.d("imgAdapter", "1 + " + String.valueOf(position));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the position of the image

                int position = holder.getAdapterPosition();

                // Pass the position to the listener
                listener.click(position);
                context=view.getContext();
                // Create an intent to start the new activity
                Intent intent = new Intent(context,ImageInfoActivity.class);
                Bundle bundle= new Bundle();
                bundle.putInt("position",position);
                intent.putExtras(bundle);
                intent.putExtra("image_path", images_list.get(position));
                intent.putExtra("next_image_path",images_list.get(position+1));

                // Pass the path to the image to the new activity
                // Start the new activity
                context.startActivity(intent);
                //Log.d("newTest", "onClick: 2");
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.gallery_item);

        }
    }
}
