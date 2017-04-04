package com.example.module2_app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


public class ImageViewerActivity extends AppCompatActivity {

    long picItem, picPrevItem, picNextItem;
    int picPosition;
    int size;
    ImageView iv;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.image_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        size = GalleryActivity.list.size()-1;
        String f = getIntent().getStringExtra("img");
        picPosition = getIntent().getIntExtra("pos",-1);

        iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageURI(Uri.parse(f));

        iv.setOnTouchListener(new OnSwipeTouchListener(ImageViewerActivity.this) {

        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.images_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_delete:
                GalleryActivity.gridAdapter.removeImage(picPosition);
                finish();
                break;
        }
        return true;
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;
        Context context;
        public OnSwipeTouchListener (Context context){
            this.context = context;
            gestureDetector = new GestureDetector(context, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
            if (--picPosition < 0) picPosition = size;
            Log.i("test","position = "+picPosition);
            iv.setImageURI(Uri.parse(GalleryActivity.list.get(picPosition).toString()));

        }

        public void onSwipeLeft() {
            if (++picPosition > size) picPosition = 0;
            Log.i("test","position = "+picPosition);
            iv.setImageURI(Uri.parse(GalleryActivity.list.get(picPosition).toString()));

        }

        public void onSwipeTop() {

        }

        public void onSwipeBottom() {
        }
    }
}
