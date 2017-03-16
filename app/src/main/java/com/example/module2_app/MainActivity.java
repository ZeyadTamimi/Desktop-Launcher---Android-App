package com.example.module2_app;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.ByteArrayInputStream;

public class MainActivity extends AppCompatActivity {
    private static final long WAIT_TIME = 2000;

    public static AppToast toast;

    private long timeLastMovement;
    private String toastMessage = "MESSAGE";

    private RelativeLayout buttonsArea;
    private TabLayout tabLayout;
    private LinearLayout tabStrip;
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
                    if (Util.uByte(receivevMessage[0])== MessageConstants.ID_RESPONSE) {
                        if (Util.uByte(receivevMessage[4]) == MessageConstants.RESPONSE_NO_ERROR) {
                            toast.out("Command Successfull!");
                        }
                        else
                            toast.out("Command: " + receivevMessage[2] +" failed with code: " + receivevMessage[3]);
                    }
                    else if (Util.uByte(receivevMessage[0]) == MessageConstants.ID_MESG_IMAGE) {
                        displayImage(receivevMessage, 3, (receivevMessage[1] << 8) + Util.uByte(receivevMessage[2]));
                    }
                    enableOnClicks(true);
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
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_modes);
        tabStrip = ((LinearLayout) tabLayout.getChildAt(0));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                toastMessage = tab.getText().toString();
                toast.out(toastMessage);
                // TODO: hardcoded here
                enableOnClicks(toastMessage.equals("MANUAL"));
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
            enableOnClicks(true);
        }
        else {
            enableOnClicks(false);
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
        enableOnClicks(false);
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
        toastMessage = "Up";
        mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_UP, 100);
    }
    private void rotateDown() {
        toastMessage = "Down";
        mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_DOWN, 100);
    }
    private void rotateRight() {
        toastMessage = "Right";
        mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_RIGHT, 500000);
    }
    private void rotateLeft() {
        toastMessage = "Left";
        mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_LEFT, 500000);
    }

    private void fire() {
        toastMessage = "Fire";
        mmCommunicationThread.commandFire();
    }

    public void takePicture() {
        toastMessage = "Take Picture";
        mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_IMAGE);
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
    private void enableOnClicks(boolean enable) {
        for (int i = 0; i < buttonsArea.getChildCount(); i++) {
            buttonsArea.getChildAt(i).setAlpha(enable ? 1f : 0.3f);
            buttonsArea.getChildAt(i).setClickable(enable);
        }

        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setAlpha(enable ? 1f : 0.3f);
            tabStrip.getChildAt(i).setClickable(enable);
        }
    }

    private void displayImage(byte[] byteArray, int offset, int size) {
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray, offset, size);
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        picture.setImageBitmap(Bitmap.createScaledBitmap(bitmap, picture.getWidth(), picture.getHeight(), false));
    }
}

