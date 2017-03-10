package com.example.module2_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    public static String EXTRA_MESSAGE = "MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {

            case R.id.action_bluetooth:
                Intent intent1 = new Intent(this, BluetoothConnectActivity.class);
                startActivity(intent1);
                return true;
            case R.id.action_images:
                Intent intent2 = new Intent(this, ImageViewActivity.class);
                startActivity(intent2);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void buttonPress (View view) {


        switch(view.getId()) {
            case R.id.button_up:
                rotateUp();
                break;
            case R.id.button_down:
                rotateDown();
                break;
            case R.id.button_right:
                rotateRight();
                break;
            case R.id.button_left:
                rotateLeft();
                break;
            case R.id.button_fire:
                fire();
                break;
            case R.id.button_camera:
                takePicture();
                break;
            default:
                break;
        }
    }

    private void rotateUp() {
        EXTRA_MESSAGE = "Rotating Up";
        recreate();
    }
    private void rotateDown() {
        EXTRA_MESSAGE = "Rotating Down";
        recreate();
    }
    private void rotateRight() {
        EXTRA_MESSAGE = "Rotating Right";
        recreate();
    }
    private void rotateLeft() {
        EXTRA_MESSAGE = "Rotating Left";
        recreate();
    }
    private void fire() {
        EXTRA_MESSAGE = "Firing";
        recreate();
    }
    private void takePicture() {
        EXTRA_MESSAGE = "Taking Picture";
        recreate();
    }


}

