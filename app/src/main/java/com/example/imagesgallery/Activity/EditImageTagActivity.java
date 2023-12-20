package  com.example.imagesgallery.Activity;


import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagesgallery.Adapter.TagAdapter;
import com.example.imagesgallery.R;

import java.util.ArrayList;

public class EditImageTagActivity extends AppCompatActivity {
    Button btnAddTag;
    Button btnAutoAddTag;

    ListView listView;
    MainActivity mainActivity;
    TagAdapter tagAdapter;
    ArrayList<String> arrayListTag;
    String imagePath ="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image_tag);

        imagePath = getIntent().getStringExtra("ImagePath");

        btnAddTag = (Button) findViewById(R.id.btnAddTag);
        btnAutoAddTag = (Button) findViewById(R.id.btnAutoAddTag);



        btnAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTagDialog();
            }
        });

        btnAutoAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        listView = (ListView) findViewById(R.id.listViewTags);
        arrayListTag = new ArrayList<>();
        getAllTagOfImage();
        tagAdapter = new TagAdapter(EditImageTagActivity.this,R.layout.custom_tag_view,arrayListTag, imagePath);
        listView.setAdapter(tagAdapter);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (tagAdapter!=null) {
            tagAdapter.notifyDataSetChanged();
        }

    }


    private void getAllTagOfImage() {
        if (arrayListTag==null) {
            return;
        }
        arrayListTag.clear();
        String[] args = {imagePath};
        Cursor cursor = mainActivity.db.rawQuery("SELECT * FROM Image_Tag WHERE Image_Path = ?",args);
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
                int tagColumnIndex = cursor.getColumnIndex("Tag");
                String tagValue = cursor.getString(tagColumnIndex);
                arrayListTag.add(tagValue);
        }
    }

    Dialog addTagDialog;
    Button btnAddTagDialog;
    Button btnCancelAddTagDialog;
    EditText editTextAddTag;

    private void showAddTagDialog() {
        addTagDialog = new Dialog(EditImageTagActivity.this);
        addTagDialog.setContentView(R.layout.add_tag_dialog);

        addTagDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addTagDialog.getWindow().setGravity(Gravity.CENTER);

        btnAddTagDialog = (Button) addTagDialog.findViewById(R.id.buttonAddTagDialog);
        btnCancelAddTagDialog = (Button) addTagDialog.findViewById(R.id.buttonCancelAddTagDialog);
        editTextAddTag = (EditText) addTagDialog.findViewById(R.id.editTextAddTag);

        btnAddTagDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tagValue = editTextAddTag.getText().toString();
                addTag(tagValue);
            }
        });

        btnCancelAddTagDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTagDialog.dismiss();
            }
        });

        addTagDialog.show();

    }

    private void addTag(String tagValue) {
        ContentValues values = new ContentValues();
        values.put("Image_Path",imagePath);
        values.put("Tag",tagValue);
        mainActivity.db.insert("Image_Tag",null,values);
        arrayListTag.add(tagValue);
        tagAdapter.notifyDataSetChanged();
        addTagDialog.dismiss();

    }
    private void objectDetection() {

    }
}
