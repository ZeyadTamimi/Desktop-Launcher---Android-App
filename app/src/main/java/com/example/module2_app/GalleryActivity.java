package com.example.module2_app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    //----------------------------------------------------------------------------------------------
    // Constants
    //----------------------------------------------------------------------------------------------
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;

    //----------------------------------------------------------------------------------------------
    // Fields
    //----------------------------------------------------------------------------------------------
    public static GridView mGridView;
    public static ArrayList<File> mFileList;
    public static GridAdapter mGridAdapter;
    public static SparseBooleanArray mCheckArray;

    //----------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_gallery_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mFileList = null;
        mFileList = imageReader( Environment.getExternalStoragePublicDirectory("Pictures/DTR Photos"));

        mGridView = (GridView) findViewById(R.id.gridview);
        mCheckArray = mGridView.getCheckedItemPositions();
        mGridView.setChoiceMode(GridView.CHOICE_MODE_NONE);

        mGridAdapter = new GridAdapter(this);
        mGridView.setAdapter(mGridAdapter);

        mGridView.setOnItemLongClickListener(new OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if ( mGridView.getChoiceMode()==GridView.CHOICE_MODE_NONE){ //none are currently checked
                    mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);//enter check mode
                    mGridView.setItemChecked(position,true); //check current box
                    mCheckArray = mGridView.getCheckedItemPositions();
                    mGridAdapter.notifyDataSetChanged();
                    return true; //not on click
                }
                mGridAdapter.notifyDataSetChanged();
                return false; //on click

            }
        });

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if ( mGridView.getChoiceMode()==GridView.CHOICE_MODE_NONE) {
                    mGridView.setChoiceMode(GridView.CHOICE_MODE_NONE);
                    mGridAdapter.notifyDataSetChanged();
                    startActivity(new Intent(getApplicationContext(), ImageViewerActivity.class)
                            .putExtra("img", mFileList.get(position).toString())
                            .putExtra("pos", position));
                }
                else if(mGridView.getCheckedItemCount()==0){ //you just unchecked the last box
                    mGridView.setChoiceMode(GridView.CHOICE_MODE_NONE); //return to non check mode
                    mGridAdapter.notifyDataSetChanged();
                }
                mGridAdapter.notifyDataSetChanged();
            }
        });

    }

    private ArrayList<File> imageReader(File root) {

        ArrayList<File> a = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        if (root == null)
            return a;
        
        File[] files = root.listFiles();

        for(int i = 0; i<files.length; i++){
            if(files[i].isDirectory()) {
                a.addAll(imageReader(files[i]));
            }else {
                if(files[i].getName().endsWith(".jpg")){
                    a.add(files[i]);
                }
            }
        }
        return a;

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.images_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_delete:

                if(mGridView.getChoiceMode()==GridView.CHOICE_MODE_MULTIPLE) { //if multiple select

                    for (int i = 0; i < mFileList.size(); i++) {
                        if (mCheckArray.get(i)) {
                            mGridAdapter.removeImage(i);
                            i--;
                        }
                    }
                    mGridView.setChoiceMode(GridView.CHOICE_MODE_NONE);

                }
                break;

            case android.R.id.home:
                finish();
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------
    public class GridAdapter extends BaseAdapter {
        Context mContext;
        public GridAdapter(Context context)
        {
            mContext = context;
        }

        @Override
        public int getCount(){
            if (mFileList == null) {
                return 0;
            }

            return mFileList.size();

        }

        @Override
        public Object getItem(int position){
            return mFileList.get(position);
        }

        @Override public long getItemId(int position){
            return position;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            CheckBox checkBox;
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                convertView = layoutInflater.inflate(R.layout.image, parent, false);
            }
            imageView = (ImageView) convertView.findViewById(R.id.imageView);
            checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            if (mGridView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {

                checkBox.setChecked(false);
                checkBox.setAlpha(1);
                if (mCheckArray != null) {
                    if (mCheckArray.get(position)) {
                        checkBox.setChecked(true);
                    }
                }

            }else if(mGridView.getChoiceMode()==ListView.CHOICE_MODE_NONE){
                mGridView.clearChoices();
                mCheckArray = mGridView.getCheckedItemPositions();
                checkBox.setAlpha(0);
            }
            imageView.setImageURI(Uri.parse(getItem(position).toString()));
            return convertView;
        }

        public void removeImage(int position){
            File file = new File(mFileList.get(position).toString());
            boolean deleted = file.delete();
            if (!deleted) {
                MainActivity.toast.out("Couldn't delete photo!");
                return;
            }

            mFileList.remove(position);
            if(mCheckArray != null){
                for (int i = position; i < mFileList.size(); i++) {
                    mCheckArray.put(i, mCheckArray.get(i + 1));
                }
                mCheckArray.put(mFileList.size(), false);
            }

            mGridAdapter.notifyDataSetChanged();
        }
    }
}
