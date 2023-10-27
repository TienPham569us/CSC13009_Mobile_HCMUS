package csu.matos.handlepermission;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> images_list;
    ClickListener listener;

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
            //ImageLoadingThread imageLoadingThread = new ImageLoadingThread(holder.image);
           // imageLoadingThread.execute(image_file.getAbsolutePath());
            /*CompletableFuture<Bitmap> imageFuture = ImageLoadingThread.loadImage(context,image_file.getAbsolutePath());

            // Update the UI with the image once it is loaded
            imageFuture.whenComplete((image, error) -> {
                if (error != null) {
                    // Handle the error
                    error.printStackTrace();
                } else {
                    holder.image.setImageBitmap(image);
                }
            });*/
        }
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the position of the image
                int position = holder.getAdapterPosition();

                // Pass the position to the listener
                listener.click(position);
                // Create an intent to start the new activity
                Intent intent = new Intent(context, ShowSoloPicture.class);

                // Pass the path to the image to the new activity
                intent.putExtra("image_path", images_list.get(position));

                // Start the new activity
                context.startActivity(intent);

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
