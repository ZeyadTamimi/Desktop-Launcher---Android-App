package com.example.module2_app;
import com.example.module2_app.tasks.*;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: fix positioning of accel switch
// TODO: fix logic of when to display buttons when to not display buttons
public class MainActivity extends AppCompatActivity {
    //--------
    // FIELDS
    //----------------------------------------------------------------------------------------------
    private static final int X_MAX_ANGLE = 45;
    private static final int Y_MAX_ANGLE = 30;
    private static final int NUM_BUTTONS = 6;

    public static MainActivity reference;
    public static AppToast toast;
    private String toastMessage = "";

    private AsyncTask<SendCommandTask.CommandType, Void, Void> mSendCommandTask;
    public static AtomicBoolean mCanSendCommands;
    private boolean mHoldingButton, mAccelMovement;
    // NOTE: this is modified through enableActions()
    //       read this value to see if we can click buttons / switch modes
    private boolean mAllowActions;

    // movement detection based on phone orientation
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mMagnetometer;
    private DirectionDetector mDirectionDetector;

    // ui elements
    private Switch mAccelOnSwitch;
    private RelativeLayout mButtonsArea;
    private TabLayout mTabLayout;
    private LinearLayout mTabStrip;
    private ImageView mPictureView;
    private ProgressBar mPictureLoading;
    private TextView mTextNotConnected;
    // NOTE: 0-3: up down left right, 4-5: fire, camera
    private FloatingActionButton[] mButtonArray;

    // bluetooth communication handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ: {
                    // TODO: Handle all types of messages
                    // TODO: Size check
                    byte[] receiveMessage = (byte[]) msg.obj;
                    if (Util.uByte(receiveMessage[0]) == MessageConstants.ID_RESPONSE) {
                        if (Util.uByte(receiveMessage[4]) == MessageConstants.RESPONSE_NO_ERROR) {
                            toastMessage = "Command Successful!";
                        }
                        else if (Util.uByte(receiveMessage[4]) == MessageConstants.RESPONSE_NIOS_HANDSHAKE) {
                            toastMessage = "Handshake!";
                            State.heartBeatTimmer.cancel();
                        }
                        else {
                            toastMessage = "Command: " + receiveMessage[2] + " failed with code: " + receiveMessage[3];
                        }
                        toast.out(toastMessage);
                    }
                    else if (Util.uByte(receiveMessage[0]) == MessageConstants.ID_MESG_IMAGE) {
                        displayImage(receiveMessage, 3, (receiveMessage[1] << 8) + Util.uByte(receiveMessage[2]));
                    }

                    enableActions(!mHoldingButton && !mAccelMovement);
                    mCanSendCommands.set(true);
                }
            }
        }
    };

    //---------------
    // STATE CHANGES
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(myToolbar);

        ///////////
        // Flags //
        ///////////
        mCanSendCommands = new AtomicBoolean(false);
        mHoldingButton = false;
        mAccelMovement = false;

        //////////////////////////////
        // Sesnsors (Accelerometer) //
        //////////////////////////////
        mAccelOnSwitch = ((Switch) findViewById(R.id.switch_accelerometer));
        mAccelOnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableAccelerometer(isChecked);
                enableButtons(mAllowActions);
            }
        });

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        // TODO: FIX THIS - if user skips onRest, mAccelMovement doesn't get released
        mDirectionDetector = new DirectionDetector(new DirectionDetector.OnDirectionChangeListener() {
            @Override
            public void onRest() {
                Log.i("Direction", "rest");
                if (mAccelMovement) {
                    Log.i("Fire", "CANCEL_TASK");
                    toast.out("release");
                    mAccelMovement = false;
                    if (mSendCommandTask != null) {
                        mSendCommandTask.cancel(false);
                    }
                }
            }

            @Override
            public void onUp() {
                Log.i("Direction", "up");
                if (mAllowActions && (mSendCommandTask == null || mSendCommandTask.getStatus() == AsyncTask.Status.FINISHED)) {
                    Log.i("START_TASK", "UP");
                    enableActions(false);
                    mAccelMovement = true;
                    mSendCommandTask = new SendCommandTask();
                    mSendCommandTask.execute(SendCommandTask.CommandType.UP);
                }
            }

            @Override
            public void onDown() {
                Log.i("Direction", "down");
                if (mAllowActions && (mSendCommandTask == null || mSendCommandTask.getStatus() == AsyncTask.Status.FINISHED)) {
                    Log.i("START_TASK", "DOWN");
                    enableActions(false);
                    mAccelMovement = true;
                    mSendCommandTask = new SendCommandTask();
                    mSendCommandTask.execute(SendCommandTask.CommandType.DOWN);
                }
            }

            @Override
            public void onLeft() {
                Log.i("Direction", "left");
                if (mAllowActions && (mSendCommandTask == null || mSendCommandTask.getStatus() == AsyncTask.Status.FINISHED)) {
                    Log.i("START_TASK", "LEFT");
                    enableActions(false);
                    mAccelMovement = true;
                    mSendCommandTask = new SendCommandTask();
                    mSendCommandTask.execute(SendCommandTask.CommandType.LEFT);
                }
            }

            @Override
            public void onRight() {
                Log.i("Direction", "right");
                if (mAllowActions && (mSendCommandTask == null || mSendCommandTask.getStatus() == AsyncTask.Status.FINISHED)) {
                    Log.i("START_TASK", "RIGHT");
                    enableActions(false);
                    mAccelMovement = true;
                    mSendCommandTask = new SendCommandTask();
                    mSendCommandTask.execute(SendCommandTask.CommandType.RIGHT);
                }
            }
        });
        ///////////////////////////
        // UI Element References //
        ///////////////////////////
        mButtonsArea = (RelativeLayout) findViewById(R.id.section_buttons);
        mPictureView = (ImageView) findViewById(R.id.iv_picture);
        mPictureLoading = (ProgressBar) findViewById(R.id.icon_loading_picture);
        mTextNotConnected = (TextView) findViewById(R.id.text_not_connected);

        mButtonArray = new FloatingActionButton[NUM_BUTTONS];
        mButtonArray[0] =  (FloatingActionButton) findViewById(R.id.button_up);
        mButtonArray[1] =  (FloatingActionButton) findViewById(R.id.button_down);
        mButtonArray[2] =  (FloatingActionButton) findViewById(R.id.button_left);
        mButtonArray[3] =  (FloatingActionButton) findViewById(R.id.button_right);
        mButtonArray[4] =  (FloatingActionButton) findViewById(R.id.button_fire);
        mButtonArray[5] =  (FloatingActionButton) findViewById(R.id.button_camera);

        toast = new AppToast(getApplicationContext());
        /////////////////////
        // Button Handlers //
        /////////////////////
        enableButtonListners(true);

        ////////////////////
        // Mode Switching //
        ////////////////////
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
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        ////////////////////////////////
        // Picture Frame Movement Tap //
        ////////////////////////////////
        mPictureView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        //TODO clean up
                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        int width = mPictureView.getWidth();
                        int height = mPictureView.getHeight();
                        int x_relative = x - width/2;
                        int y_relative = y - height/2;
                        int x_factor = width/2/X_MAX_ANGLE;
                        int y_factor = height/2/Y_MAX_ANGLE;
                        int x_angle = x_relative/x_factor;
                        int y_angle = y_relative/y_factor;
                        rotateTouch(x_angle, y_angle);
                        /*
                        Log.i("view size","width= "+width);
                        Log.i("view size","height= "+height);
                        Log.i("coordinate","x= "+x);
                        Log.i("coordinate","y= "+y);
                        Log.i("relative","x= "+x_relative);
                        Log.i("relative","y= "+y_relative);
                        Log.i("angle","x= "+x_angle);
                        Log.i("angle","y= "+y_angle);
                        */
                        break;
                    }
                }
                return true;
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onResume() {
        super.onResume();
        reference = this;
        enableActions(false);
        enableAccelerometer(mAccelOnSwitch.isChecked());

        // communication thread
        if (State.btConnected()) {
            if (State.mmCommunicationThread == null) {
                State.mmCommunicationThread = new CommunicationThread(State.getBtSocket(), mHandler);
                State.mmCommunicationThread.start();
            }

            if (State.heartBeatTimmer != null) {
                State.heartBeatTimmer.cancel();
            }

            State.heartBeatTimmer = new Timer();
            // cancel heartbeat timer and restart it
            final Handler handler = new Handler();
            TimerTask handshake = new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            Log.i("info", "timer handshake");
                            State.mmCommunicationThread.commandHandshake();
                        }
                    });
                }
            };
            State.heartBeatTimmer.schedule(handshake, 0, 1000);
            showNotConnected(false);
            return;
        }

        showNotConnected(true);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onPause() {
        super.onPause();
        reference = this;

        enableAccelerometer(false);
        if (mSendCommandTask != null)
            mSendCommandTask.cancel(false);

        mCanSendCommands.set(false);
        mHoldingButton = false;
        mAccelMovement = false;
    }

    //----------------------------------------------------------------------------------------------
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
    //-----------------
    // Button Handlers
    //----------------------------------------------------------------------------------------------
    public void buttonPress(View view) {
        enableActions(false);
        switch(view.getId()) {
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

    //----------------------------------------------------------------------------------------------
    private void enableButtonListners(boolean enable) {
        findViewById(R.id.button_up).setOnTouchListener(
                enable ? new ButtonOnHoldListner(SendCommandTask.CommandType.UP) : null
        );
        findViewById(R.id.button_down).setOnTouchListener(
                enable ? new ButtonOnHoldListner(SendCommandTask.CommandType.DOWN) : null
        );
        findViewById(R.id.button_left).setOnTouchListener(
                enable ? new ButtonOnHoldListner(SendCommandTask.CommandType.LEFT) : null
        );
        findViewById(R.id.button_right).setOnTouchListener(
                enable ? new ButtonOnHoldListner(SendCommandTask.CommandType.RIGHT) : null
        );
    }

    //---------
    // Actions
    //----------------------------------------------------------------------------------------------
    private void rotateTouch(int x_angle, int y_angle){
        if (mAllowActions) {
            toastMessage = "x angle= "+ x_angle + " " + "y angle = " + y_angle;
            // toast.out(toastMessage);
            if (x_angle <= 127 && x_angle >= -128 && y_angle <= 127 && y_angle >= -128) {
                enableActions(false);
                State.mmCommunicationThread.commandMoveAngle(x_angle, y_angle);
                // TODO: add this back after calibrating angles
                // takePicture();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private void fire() {
        toast.out("FIRE");
        State.mmCommunicationThread.commandFire();
    }

    //----------------------------------------------------------------------------------------------
    private void takePicture() {
        toast.out("PICTURE");
        showLoading(true);
        State.mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_IMAGE);
    }

    //----------------------------------------------------------------------------------------------
    private void displayImage(byte[] byteArray, int offset, int size) {
        showLoading(false);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray, offset, size);
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        mPictureView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, mPictureView.getWidth(), mPictureView.getHeight(), false));
        if (State.backup_switch_state)
            Util.saveImage(byteArray, offset, size);
    }

    //----------------------------------------------------------------------------------------------
    private void enableAccelerometer(boolean enable) {
        if (enable) {
            mSensorManager.registerListener(mDirectionDetector, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mDirectionDetector, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            mSensorManager.unregisterListener(mDirectionDetector);
        }
    }
    //----------------------------------------------------------------------------------------------
    public void enableActions(boolean enable) {
        mAllowActions = enable;
        enableButtons(enable);

        for(int i = 0; i < mTabStrip.getChildCount(); i++) {
            mTabStrip.getChildAt(i).setAlpha(enable ? 1f : 0.3f);
            mTabStrip.getChildAt(i).setClickable(enable);
        }
    }

    //----------------------------------------------------------------------------------------------
    public void enableButtons(boolean enable) {
        for (FloatingActionButton btn : mButtonArray) {
            btn.setAlpha(enable ? 1f : 0.3f);
            btn.setClickable(enable);
        }
    }

    //----------------------------------------------------------------------------------------------
    private void showLoading(boolean on) {
        mPictureLoading.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    //----------------------------------------------------------------------------------------------
    private void showNotConnected(boolean on) {
        mTextNotConnected.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    //----------------------------------------------------------------------------------------------
    private class ButtonOnHoldListner implements View.OnTouchListener {
        SendCommandTask.CommandType mCmd;

        public ButtonOnHoldListner(SendCommandTask.CommandType cmd) {
            mCmd = cmd;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mAllowActions) {
                        enableActions(false);
                        mHoldingButton = true;
                        toast.out(mCmd.name());
                        mSendCommandTask = new SendCommandTask();
                        mSendCommandTask.execute(mCmd);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (mHoldingButton) {
                        toast.out("release");
                        mHoldingButton = false;
                        mSendCommandTask.cancel(false);
                    }
                    break;
            }
            return true;
        }
    }
}

