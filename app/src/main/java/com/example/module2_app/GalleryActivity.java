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
import android.view.MotionEvent;
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
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    public static GridView gridview;
    public static ArrayList<File> list;
    public static boolean[] list_selection;
    public static GridAdapter gridAdapter;
    public static int picsSelected;
    public static SparseBooleanArray checkArray;

    //----------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_gallery_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);



        list = null;
        list = imageReader( Environment.getExternalStoragePublicDirectory("Pictures/DTR Photos"));
        list_selection = new boolean[list.size()];

        gridview = (GridView) findViewById(R.id.gridview);
        checkArray = gridview.getCheckedItemPositions();
        gridview.setChoiceMode(GridView.CHOICE_MODE_NONE);

        gridAdapter = new GridAdapter(this);
        gridview.setAdapter(gridAdapter);


        picsSelected = 0;

        gridview.setOnItemLongClickListener(new OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if ( gridview.getChoiceMode()==GridView.CHOICE_MODE_NONE){ //none are currently checked
                    gridview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);//enter check mode
                    gridview.setItemChecked(position,true); //check current box
                    checkArray = gridview.getCheckedItemPositions();
                    gridAdapter.notifyDataSetChanged();
                    return true; //not on click
                }
                gridAdapter.notifyDataSetChanged();
                return false; //on click

            }
        });


        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if ( gridview.getChoiceMode()==GridView.CHOICE_MODE_NONE) {
                    gridview.setChoiceMode(GridView.CHOICE_MODE_NONE);
                    gridAdapter.notifyDataSetChanged();
                    startActivity(new Intent(getApplicationContext(), ImageViewerActivity.class)
                            .putExtra("img", list.get(position).toString())
                            .putExtra("pos", position));
                }
                else if(gridview.getCheckedItemCount()==0){ //you just unchecked the last box
                    gridview.setChoiceMode(GridView.CHOICE_MODE_NONE); //return to non check mode
                    gridAdapter.notifyDataSetChanged();
                }
                gridAdapter.notifyDataSetChanged();


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

                if(gridview.getChoiceMode()==GridView.CHOICE_MODE_MULTIPLE) { //if multiple select

                    for (int i = 0; i < list.size(); i++) {
                        if (checkArray.get(i)) {
                            gridAdapter.removeImage(i);
                            i--;
                        }
                    }
                    gridview.setChoiceMode(GridView.CHOICE_MODE_NONE);

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
            if (list == null) {
                return 0;
            }

            return list.size();

        }

        @Override
        public Object getItem(int position){
            return list.get(position);
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

            if (gridview.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {

                checkBox.setChecked(false);
                checkBox.setAlpha(1);
                if (checkArray != null) {
                    if (checkArray.get(position)) {
                        checkBox.setChecked(true);
                    }
                }

            }else if(gridview.getChoiceMode()==ListView.CHOICE_MODE_NONE){
                gridview.clearChoices();
                checkArray = gridview.getCheckedItemPositions();
                checkBox.setAlpha(0);
            }
            imageView.setImageURI(Uri.parse(getItem(position).toString()));
            return convertView;
        }

        public void removeImage(int position){
            File dir = getFilesDir();
            File file = new File(list.get(position).toString());
            boolean deleted = file.delete();
            list.remove(position);

            if(checkArray != null){
                for (int i = position; i < list.size(); i++) {
                    checkArray.put(i, checkArray.get(i + 1));
                }
                checkArray.put(list.size(), false);
            }


            gridAdapter.notifyDataSetChanged();



        }
    }
}
