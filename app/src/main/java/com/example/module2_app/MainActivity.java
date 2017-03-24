package com.example.module2_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final long WAIT_TIME = 2000;
    private static final int X_MAX_ANGLE = 45;
    private static final int Y_MAX_ANGLE = 30;

    public static AppToast toast;

    private String toastMessage = "MESSAGE";

    private AsyncTask<SendCommandTask.CommandType, Void, Void> mSendCommandTask;
    public static AtomicBoolean mCanSendCommands;
    private boolean mHoldingButton;
    // NOTE: this is modified through enableActions()
    //       read this value to see if we can click buttons / switch modes
    private boolean mAllowActions;

    // movement detection based on phone orientation
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mMagnetometer;
    private DirectionDetector mDirectionDetector;

    // ui elements
    private RelativeLayout mButtonsArea;
    private TabLayout mTabLayout;
    private LinearLayout mTabStrip;
    private ImageView mPictureView;
    private ProgressBar mPictureLoading;
    private TextView mTextNotConnected;

    // communication handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_READ: {
                    // TODO: Handle all types of messages
                    // TODO: Size check
                    byte[] receivevMessage = (byte[]) msg.obj;
                    if (Util.uByte(receivevMessage[0]) == MessageConstants.ID_RESPONSE) {
                        if (Util.uByte(receivevMessage[4]) == MessageConstants.RESPONSE_NO_ERROR) {
                            toastMessage = "Command Successful!";
                        }
                        else if (Util.uByte(receivevMessage[4]) == MessageConstants.RESPONSE_NIOS_HANDSHAKE) {
                            toastMessage = "Handshake!";
                            State.heartBeatTimmer.cancel();
                        }
                        else {
                            toastMessage = "Command: " + receivevMessage[2] + " failed with code: " + receivevMessage[3];
                        }
                        toast.out(toastMessage);
                    }
                    else if (Util.uByte(receivevMessage[0]) == MessageConstants.ID_MESG_IMAGE) {
                        displayImage(receivevMessage, 3, (receivevMessage[1] << 8) + Util.uByte(receivevMessage[2]));
                    }
                    // TODO: add this to enable on hold button clicks, then delete the next enableActions
                    /*
                    if (!mHoldingButton) {
                        enableActions(true);
                    }
                    */
                    enableActions(true);
                    mCanSendCommands.set(true);
                }
            }
        }
    };

    private FloatingActionButton fireBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(myToolbar);

        // on hold buttons
        mCanSendCommands = new AtomicBoolean(false);
        mHoldingButton = false;
        // TODO: add this to enable on hold button clicks
        // setUpButtonsHandler();

        // sensor initialization
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mDirectionDetector = new DirectionDetector(new DirectionDetector.OnDirectionChangeListener() {
            @Override
            public void onUp() {
                Log.i("Direction", "up");
            }

            @Override
            public void onDown() {
                Log.i("Direction", "down");
            }

            @Override
            public void onLeft() {
                Log.i("Direction", "left");
            }

            @Override
            public void onRight() {
                Log.i("Direction", "right");
            }
        });

        // initialize UI element references
        mButtonsArea = (RelativeLayout) findViewById(R.id.section_buttons);
        mPictureView = (ImageView) findViewById(R.id.iv_picture);
        mPictureLoading = (ProgressBar) findViewById(R.id.icon_loading_picture);
        mTextNotConnected = (TextView) findViewById(R.id.text_not_connected);

        toast = new AppToast(getApplicationContext());

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
        // sesnsor listeners
        mSensorManager.registerListener(mDirectionDetector, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mDirectionDetector, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        // communication thread
        if (State.btConnected()) {
            if (State.mmCommunicationThread == null) {
                State.mmCommunicationThread = new CommunicationThread(State.getBtSocket(), mHandler);
                State.mmCommunicationThread.start();

                if (State.heartBeatTimmer == null) {
                    State.heartBeatTimmer = new Timer();
                }

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
            }
            showNotConnected(false);
        }
        else {
            enableActions(false);
            showNotConnected(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mDirectionDetector);
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

    // TODO: remove this onClick callback to enable on hold button clicks
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


    private void rotateTouch(int x_angle, int y_angle){
        if (mAllowActions) {
            toastMessage = "x angle= "+ x_angle + " " + "y angle = " + y_angle;
            // toast.out(toastMessage);
            if (x_angle <= 127 && x_angle >= -128 && y_angle <= 127 && y_angle >= -128) {
                enableActions(false);
                State.mmCommunicationThread.commandMoveAngle(x_angle, y_angle);
                takePicture();
            }
        }
    }

    private void rotateUp() {
        toastMessage = "Up";
        State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_UP, 0);
    }
    private void rotateDown() {
        toastMessage = "Down";
        State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_DOWN, 0);
    }
    private void rotateRight() {
        toastMessage = "Right";
        State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_RIGHT, 50000);
    }
    private void rotateLeft() {
        toastMessage = "Left";
        State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_LEFT, 50000);
    }

    private void fire() {
        toastMessage = "Fire";
        State.mmCommunicationThread.commandFire();
    }

    public void takePicture() {
        showLoading(true);
        toastMessage = "Take Picture";
        State.mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_IMAGE);
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

    // TODO: maybe a better way to do this
    private void setUpButtonsHandler() {
        Map<Integer, SendCommandTask.CommandType> cmdToBtn = new HashMap<>(mButtonsArea.getChildCount());
        for (int i = 0; i < mButtonsArea.getChildCount(); i++) {
            switch (mButtonsArea.getChildAt(i).getId()) {
                case R.id.button_up:
                    cmdToBtn.put(i, SendCommandTask.CommandType.UP);
                    break;
                case R.id.button_down:
                    cmdToBtn.put(i, SendCommandTask.CommandType.DOWN);
                    break;
                case R.id.button_right:
                    cmdToBtn.put(i, SendCommandTask.CommandType.RIGHT);
                    break;
                case R.id.button_left:
                    cmdToBtn.put(i, SendCommandTask.CommandType.LEFT);
                    break;
                case R.id.button_fire:
                    cmdToBtn.put(i, SendCommandTask.CommandType.FIRE);
                    break;
            }
        }

        for (Integer i : cmdToBtn.keySet()) {
            final SendCommandTask.CommandType cmd = cmdToBtn.get(i);
            mButtonsArea.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (mAllowActions) {
                                enableActions(false);
                                mHoldingButton = true;
                                toast.out("fire");
                                mSendCommandTask = new SendCommandTask();
                                mSendCommandTask.execute(cmd);
                            }
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            if (mHoldingButton) {
                                toast.out("release");
                                mHoldingButton = false;
                                mSendCommandTask.cancel(true);
                            }
                            break;
                    }
                    return true;
                }
            });
        }

    }

    private void displayImage(byte[] byteArray, int offset, int size) {
        showLoading(false);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray, offset, size);
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        mPictureView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, mPictureView.getWidth(), mPictureView.getHeight(), false));
        if (State.backup_switch_state)
            Util.saveImage(byteArray, offset, size);
    }

    private void showLoading(boolean on) {
        mPictureLoading.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    private void showNotConnected(boolean on) {
        mTextNotConnected.setVisibility(on ? View.VISIBLE : View.GONE);
    }
}

