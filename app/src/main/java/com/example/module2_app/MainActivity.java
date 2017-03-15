package com.example.module2_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final long WAIT_TIME = 2000;

    public static AppToast toast;

    private long timeLastMovement;
    private String toastMessage = "MESSAGE";

    private RelativeLayout buttonsArea;
    private ImageView picture;

    private CommunicationThread mmCommunicationThread;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ: {
                    // TODO: Handle all types of messages
                    // TODO: Size check
                    byte[] receivevMessage = (byte[]) msg.obj;
                    if (receivevMessage[0] == MessageConstants.RESPONSE_ID) {
                        if (receivevMessage[3] == MessageConstants.RESPONSE_NO_ERROR)
                            toast.out("Command Successfull!");
                        else
                            toast.out("Command: " + receivevMessage[2] +" failed with code: " + receivevMessage[3]);
                    }
                    enableButtons(true);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(myToolbar);

        buttonsArea = (RelativeLayout) findViewById(R.id.section_buttons);
        picture = (ImageView) findViewById(R.id.iv_picture);

        toast = new AppToast(getApplicationContext());

        // tabs: our modes
        TabLayout myModes = (TabLayout) findViewById(R.id.tab_layout_modes);
        myModes.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                toastMessage = tab.getText().toString();
                toast.out(toastMessage);
                // TODO: hardcoded here
                enableButtons(toastMessage.equals("MANUAL"));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BluetoothConnectActivity.myBluetoothSocket != null) {
            mmCommunicationThread = new CommunicationThread(BluetoothConnectActivity.myBluetoothSocket, mHandler);
            mmCommunicationThread.start();
            enableButtons(true);
        }
        else {
            enableButtons(false);
        }
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
            case R.id.action_settings:
                Intent intent3 = new Intent(this, SettingsActivity.class);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void buttonPress(View view) {
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
        toast.out(toastMessage);
    }

    private void rotateUp() {
        toastMessage = "Rotating Up";
        if (mmCommunicationThread != null) {
            mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_UP, 100);
        }
    }
    private void rotateDown() {
        toastMessage = "Rotating Down";
        if (mmCommunicationThread != null) {
            mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_DOWN, 100);
        }
    }
    private void rotateRight() {
        toastMessage = "Rotating Right";
        if (mmCommunicationThread != null) {
            mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_RIGHT, 500000);
        }
    }
    private void rotateLeft() {
        toastMessage = "Rotating Left";
        if (mmCommunicationThread != null) {
            mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_LEFT, 500000);
        }
        enableButtons(false);
    }

    private void fire() {
        toastMessage = "Firing";
    }

    public void takePicture() {
        toastMessage = "Taking Picture";
        // TODO: display the actual image here
        displayImage(new byte[1]);
    }

    private void takePictureDelayed() {
        timeLastMovement = System.currentTimeMillis();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= timeLastMovement + WAIT_TIME) {
                    takePicture();
                }
            }
        }, WAIT_TIME);
    }

    // TODO: can do a slow fade
    private void enableButtons(boolean enable) {
        for (int i = 0; i < buttonsArea.getChildCount(); i++) {
            FloatingActionButton btn = (FloatingActionButton) buttonsArea.getChildAt(i);
            btn.setAlpha(enable ? 1f : 0.3f);
            btn.setClickable(enable);
        }
    }

    // TODO: what format am I getting the image in
    private void displayImage(byte[] byteArray) {
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
        // Bitmap bitmap = BitmapFactory.decodeStream(in);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.space);
        picture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, picture.getWidth(), picture.getHeight(), false));
    }
}

