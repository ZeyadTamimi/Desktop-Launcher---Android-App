package com.example.module2_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static GridView gridview;
    public static ArrayList<File> list;
    public static GridAdapter gridAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_gallery_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //list = new ArrayList<>(imageReader( Environment.getExternalStoragePublicDirectory("Pictures/DTR Photos")));
        list = imageReader( Environment.getExternalStoragePublicDirectory("Pictures/DTR Photos"));

        gridview = (GridView) findViewById(R.id.gridview);
        gridAdapter = new GridAdapter(this);
        gridview.setAdapter(gridAdapter);

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {


                startActivity( new  Intent(getApplicationContext(), ImageViewerActivity.class)
                        .putExtra("img",list.get(position).toString())
                        .putExtra("pos",position));


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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.images_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //gridview.getAdapter().notifyDataSetChanged();
        list.clear();
        Log.i("hello","size = " +list.size());
    }

    public class GridAdapter extends BaseAdapter {
        Context context;
        public GridAdapter(Context context)
        {
            this.context = context;
        }

        @Override
        public int getCount(){
            if (list == null) {
                return 0;
            }
            Log.i("hello", "list size = " +list.size());
            return list.size();

        }

        @Override
        public Object getItem(int position){
            return list.get(position);
        }

        @Override public long getItemId(int position){
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            ImageView myImageView;
            if (convertView != null)
                myImageView = (ImageView) convertView;
            else {
                myImageView = new ImageView(context);
                myImageView.setLayoutParams(new GridView.LayoutParams(350, 250));
                myImageView.setAdjustViewBounds(false);
                myImageView.setScaleType(ImageView.ScaleType.FIT_START);

            }
            myImageView.setImageURI(Uri.parse(getItem(position).toString()));
            return myImageView;
        }

        public void removeImage(int position){
            list.remove(position);

            notifyDataSetChanged();


        }


    }

}