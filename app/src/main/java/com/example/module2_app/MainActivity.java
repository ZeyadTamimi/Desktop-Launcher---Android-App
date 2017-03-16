package com.example.module2_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;

public class MainActivity extends AppCompatActivity {
    private static final long WAIT_TIME = 2000;

    public static AppToast toast;

    private long mTimeLastMovement;
    private boolean mAllowActions;
    private String toastMessage = "MESSAGE";

    private RelativeLayout mButtonsArea;
    private TabLayout mTabLayout;
    private LinearLayout mTabStrip;
    private ImageView mPictureView;
    private ProgressBar mPictureLoading;
    private TextView mTextNotConnected;
    private CommunicationThread mmCommunicationThread;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ: {
                    // TODO: Handle all types of messages
                    // TODO: Size check
                    byte[] receivevMessage = (byte[]) msg.obj;
                    if (Util.uByte(receivevMessage[0]) == MessageConstants.ID_RESPONSE) {
                        if (Util.uByte(receivevMessage[4]) == MessageConstants.RESPONSE_NO_ERROR) {
                            toast.out("Command Successfull!");
                        }
                        else {
                            toast.out("Command: " + receivevMessage[2] + " failed with code: " + receivevMessage[3]);
                        }
                    }
                    else if (Util.uByte(receivevMessage[0]) == MessageConstants.ID_MESG_IMAGE) {
                        displayImage(receivevMessage, 3, (receivevMessage[1] << 8) + Util.uByte(receivevMessage[2]));
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

        mButtonsArea = (RelativeLayout) findViewById(R.id.section_buttons);
        mPictureView = (ImageView) findViewById(R.id.iv_picture);
        mPictureLoading = (ProgressBar) findViewById(R.id.icon_loading_picture);
        mTextNotConnected = (TextView) findViewById(R.id.text_not_connected);

        toast = new AppToast(getApplicationContext());

        // tabs: our modes
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout_modes);
        mTabStrip = ((LinearLayout) mTabLayout.getChildAt(0));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                toastMessage = tab.getText().toString();
                toast.out(toastMessage);
                if (mAllowActions) {
                    // TODO: hardcoded here
                    enableButtons(toastMessage.equals("MANUAL"));
                }
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
        if (State.getBtSocket() != null && State.getBtSocket().isConnected()) {
            mmCommunicationThread = new CommunicationThread(State.getBtSocket(), mHandler);
            mmCommunicationThread.start();
            enableActions(true);
            showNotConnected(false);
        }
        else {
            enableActions(false);
            showNotConnected(true);
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
        enableActions(false);
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
        showLoading(true);
        toastMessage = "Take Picture";
        mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_IMAGE);
    }

    private void takePictureDelayed() {
        mTimeLastMovement = System.currentTimeMillis();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= mTimeLastMovement + WAIT_TIME) {
                    takePicture();
                }
            }
        }, WAIT_TIME);
    }

    private void enableActions(boolean enable) {
        mAllowActions = enable;
        enableButtons(enable);

        for(int i = 0; i < mTabStrip.getChildCount(); i++) {
            mTabStrip.getChildAt(i).setAlpha(enable ? 1f : 0.3f);
            mTabStrip.getChildAt(i).setClickable(enable);
        }
    }

    private void enableButtons(boolean enable) {
        for (int i = 0; i < mButtonsArea.getChildCount(); i++) {
            mButtonsArea.getChildAt(i).setAlpha(enable ? 1f : 0.3f);
            mButtonsArea.getChildAt(i).setClickable(enable);
        }
    }

    private void displayImage(byte[] byteArray, int offset, int size) {
        showLoading(false);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray, offset, size);
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        mPictureView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, mPictureView.getWidth(), mPictureView.getHeight(), false));
    }

    private void showLoading(boolean on) {
        mPictureLoading.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    private void showNotConnected(boolean on) {
        mTextNotConnected.setVisibility(on ? View.VISIBLE : View.GONE);
    }
}

