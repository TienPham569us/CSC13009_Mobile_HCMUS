package csu.matos.handlepermission;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoadingThread  {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

    public static CompletableFuture<Bitmap> loadImage(Context context,  String path) {
        return CompletableFuture.supplyAsync(() -> {
            // Load the image from disk
            Bitmap image = null;
            try {
                image = Glide.with(context)
                        .asBitmap().load(path)
                        .into(-1, -1)
                        .get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return image;
        }, EXECUTOR_SERVICE);
    }
}
