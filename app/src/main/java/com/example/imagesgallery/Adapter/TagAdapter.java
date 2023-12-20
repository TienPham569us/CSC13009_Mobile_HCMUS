package com.example.imagesgallery.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.imagesgallery.Activity.MainActivity;
import com.example.imagesgallery.R;

import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends ArrayAdapter<String> {
    Context context;
    ArrayList<String> items;

    String imagePath;
    MainActivity mainActivity;



    public TagAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> items, @NonNull String imagePath) {
        super(context, R.layout.custom_tag_view,items );
        this.context = context;
        this.items = items;
        this.imagePath = imagePath;
    }

    public class ViewHolder {
        TextView txtViewTag;
        ImageButton imageButtonEditTag;
        ImageButton imageButtonRemoveTag;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        ViewHolder viewHolder;
       if (convertView==null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_tag_view, null);

            viewHolder.txtViewTag = convertView.findViewById(R.id.txtViewTag);
            viewHolder.imageButtonRemoveTag = convertView.findViewById(R.id.imageBtnRemoveTag);
            viewHolder.imageButtonEditTag = convertView.findViewById(R.id.imageBtnEditTag);
            viewHolder.imageButtonRemoveTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //String tagValue = viewHolder.txtViewTag.getText().toString();
                   /* String tagValue = items.get(position);
                    Toast.makeText(context, "tag: "+tagValue, Toast.LENGTH_SHORT).show();*/
                    deleteTag(position);
                }
            });
            viewHolder.imageButtonEditTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEditTagDialog(items.get(position),position);
                }
            });
            convertView.setTag(viewHolder);
       } else {
           viewHolder = (ViewHolder) convertView.getTag();
       }

       viewHolder.txtViewTag.setText(items.get(position));

       return convertView;

    }

    private void deleteTag(int position) {
        String condition = "Image_Path = ? and Tag= ?";
        String[] args = {imagePath,items.get(position)};
        mainActivity.db.delete("Image_Tag",condition,args);
        items.remove(position);
        this.notifyDataSetChanged();
    }

    Dialog editTagDialog;
    Button btnEditTagDialog;
    Button btnCancelEditTagDialog;
    EditText editTextEditTag;
    private void showEditTagDialog(String currentTag, int position) {
        editTagDialog = new Dialog(getContext());
        editTagDialog.setContentView(R.layout.edit_tag_dialog);

        editTagDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editTagDialog.getWindow().setGravity(Gravity.CENTER);

        btnEditTagDialog = (Button) editTagDialog.findViewById(R.id.buttonEditTagDialog);
        btnCancelEditTagDialog = (Button) editTextEditTag.findViewById(R.id.buttonCancelEditTagDialog);
        editTextEditTag = (EditText) editTextEditTag.findViewById(R.id.editTextEditTag);

        btnEditTagDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tagValue = editTextEditTag.getText().toString();
                editTag(position, tagValue);
            }
        });

        btnCancelEditTagDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTagDialog.dismiss();
            }
        });

        editTagDialog.show();
    }


    private void editTag(int position, String newTagValue) {

        editTagDialog.dismiss();
    }
}
