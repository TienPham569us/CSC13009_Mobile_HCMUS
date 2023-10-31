package com.example.imagesgallery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.imagesgallery.Activity.AlbumInfoActivity;
import com.example.imagesgallery.Model.Album;
import com.example.imagesgallery.R;

import java.util.ArrayList;

public class AlbumAdapter extends BaseAdapter {
    Context context;
    ArrayList<Album> albumArrayList;

    public AlbumAdapter(Context context, ArrayList<Album> albumArrayList) {
        this.context = context;
        this.albumArrayList = albumArrayList;
    }

    @Override
    public int getCount() {
        return albumArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    private class ViewHolder {
        TextView name;
        ImageView image;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.album_item, null);
            viewHolder.name = (TextView) view.findViewById(R.id.nameAlbum);
            viewHolder.image = (ImageView) view.findViewById(R.id.imageCoverAlbum);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Album album = albumArrayList.get(i);
        viewHolder.image.setImageResource(album.getCover().getSource());
        viewHolder.name.setText(album.getName());

        return view;
    }
}
