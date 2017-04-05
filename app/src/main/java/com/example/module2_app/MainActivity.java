package com.example.module2_app;
import com.example.module2_app.tasks.*;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.opencv.imgcodecs.Imgcodecs.imread;

public class MainActivity extends AppCompatActivity {
    //--------
    // FIELDS
    //----------------------------------------------------------------------------------------------
    public static final int X_MAX_ANGLE = 45;
    public static final int Y_MAX_ANGLE = 30;

    private static final int NUM_BUTTONS = 6;

    public static MainActivity ref;
    public static AppToast toast;
    private String toastMessage = "";

    private ExecuteModeTask.ModeType mCurrentMode;
    private SendCommandTask.CommandType mLastAccelCommand;
    private AsyncTask<ExecuteModeTask.ModeType, Void, Void> mExecuteModeTask;
    private AsyncTask<SendCommandTask.CommandType, Void, Void> mSendCommandTask;
    public static AtomicBoolean mCanSendCommands;
    private boolean mHoldingButton, mAccelMovement, mDetectedMotion;
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
    private Button mTrackingButton;
    // NOTE: 0-3: up down left right, 4-5: fire, camera
    private FloatingActionButton[] mButtonArray;
    private int mLastTab = 0;

    // Tracking Setup
    private boolean             mTrackingEnabled = false;
    // OpenCV Stuff
    private boolean              mIsColorSelected = false;
    private Mat mRgba;
    private Scalar mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private String               mTrackingImageViewFileName = "dtr_temp_file.png";
    private String               savedImageName = null;
    public static Point          mTrackedBlobCenter = null;

    // bluetooth communication handler
    private Handler mHandler;

    //---------
    // Open CV
    //----------------------------------------------------------------------------------------------
    // This method handles the loading of the OpenCV Library
    private BaseLoaderCallback mOpenCVCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    // TODO remove the hardcode!
                    mRgba = new Mat(240, 320, CvType.CV_8UC4);
                    mDetector = new ColorBlobDetector();
                    mSpectrum = new Mat();
                    mBlobColorRgba = new Scalar(255);
                    mBlobColorHsv = new Scalar(255);
                    SPECTRUM_SIZE = new Size(200, 64);
                    CONTOUR_COLOR = new Scalar(255,0,0,255);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
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

        ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
        vf.setDisplayedChild(vf.indexOfChild(findViewById(R.id.section_buttons)));

        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(myToolbar);


        ///////////////////////////
        // Communication handler //
        ///////////////////////////
        mHandler = new CommunicationHandler();

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
            }
        });

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mLastAccelCommand = SendCommandTask.CommandType.REST;

        // TODO: refactor the else if for switching between 2 directions
        mDirectionDetector = new DirectionDetector(new DirectionDetector.OnDirectionChangeListener() {
            @Override
            public void onRest() {
                Log.i("Direction", "rest");
                if (mAccelMovement) {
                    Log.i("Fire", "CANCEL_TASK");
                    toast.out("release");
                    mAccelMovement = false;
                    mLastAccelCommand = SendCommandTask.CommandType.REST;
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
                    mLastAccelCommand = SendCommandTask.CommandType.UP;
                    mSendCommandTask = new SendCommandTask();
                    mSendCommandTask.execute(SendCommandTask.CommandType.UP);
                }
                else if (mAccelMovement && mLastAccelCommand != SendCommandTask.CommandType.UP) {
                    if (mSendCommandTask != null) {
                        mSendCommandTask.cancel(false);
                    }
                    mLastAccelCommand = SendCommandTask.CommandType.UP;
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
                    mLastAccelCommand = SendCommandTask.CommandType.DOWN;
                    mSendCommandTask = new SendCommandTask();
                    mSendCommandTask.execute(SendCommandTask.CommandType.DOWN);
                }
                else if (mAccelMovement && mLastAccelCommand != SendCommandTask.CommandType.DOWN) {
                    if (mSendCommandTask != null) {
                        mSendCommandTask.cancel(false);
                    }
                    mLastAccelCommand = SendCommandTask.CommandType.DOWN;
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
                    mLastAccelCommand = SendCommandTask.CommandType.LEFT;
                    mSendCommandTask = new SendCommandTask();
                    mSendCommandTask.execute(SendCommandTask.CommandType.LEFT);
                }
                else if (mAccelMovement && mLastAccelCommand != SendCommandTask.CommandType.LEFT) {
                    if (mSendCommandTask != null) {
                        mSendCommandTask.cancel(false);
                    }
                    mLastAccelCommand = SendCommandTask.CommandType.LEFT;
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
                    mLastAccelCommand = SendCommandTask.CommandType.RIGHT;
                    mSendCommandTask = new SendCommandTask();
                    mSendCommandTask.execute(SendCommandTask.CommandType.RIGHT);
                }
                else if (mAccelMovement && mLastAccelCommand != SendCommandTask.CommandType.RIGHT) {
                    if (mSendCommandTask != null) {
                        mSendCommandTask.cancel(false);
                    }
                    mLastAccelCommand = SendCommandTask.CommandType.RIGHT;
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
        mTrackingButton = (Button) findViewById(R.id.button_tracking);
        mTrackingButton.setEnabled(false);

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
        mCurrentMode = ExecuteModeTask.ModeType.MANUAL;

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout_modes);
        mTabLayout.addOnTabSelectedListener(new ModeSelectedListener());
        mTabStrip = ((LinearLayout) mTabLayout.getChildAt(0));

        ////////////////////////////////
        // Picture Frame Movement Tap //
        ////////////////////////////////
        mPictureView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if ((event.getAction() != MotionEvent.ACTION_DOWN) || !mAllowActions || !mCanSendCommands.get())
                    return false;

                int x = (int) event.getX();
                int y = (int) event.getY();

                if (mCurrentMode != ExecuteModeTask.ModeType.TRACKING) {
                    processImageMoveTouch(x, y);
                    return true;
                }

                // Stop processing user inputs if tracking is disabled
                if (mTrackingEnabled)
                    return false;

                // TODO assert that we are in tracking mode tab!
                if (savedImageName == null) {
                    toast.out("Please take a picture first");
                    return false;
                }

                processImageTrackingTouch(x, y);
                // At this stage it makes sense to process the image and update it
                if (!processSavedImage()) {
                    toast.out("Error: Ensure I have storage permissions!");
                    return false;
                }

                displayImageFileName(mTrackingImageViewFileName);
                mTrackingButton.setEnabled(true);
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

        if (!OpenCVLoader.initDebug()) {
            Log.d("WOW", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mOpenCVCallback);
        } else {
            Log.d("WOW", "OpenCV library found inside package. Using it!");
            mOpenCVCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        ref = this;
        enableActions(false);
        enableAccelerometer(mAccelOnSwitch.isChecked());

        // TODO: review this part!
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
        ref = this;

        enableAccelerometer(false);

        if (mExecuteModeTask != null)
            mExecuteModeTask.cancel(false);

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
                Intent intent2 = new Intent(this, GalleryActivity.class);
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

    //----------------------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GalleryActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
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
            case R.id.button_tracking:
                toggleTracking();
                break;
            default:
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    private void enableButtonListners(boolean enable) {
        findViewById(R.id.button_up).setOnTouchListener(
                enable ? new ButtonOnHoldListener(SendCommandTask.CommandType.UP) : null
        );
        findViewById(R.id.button_down).setOnTouchListener(
                enable ? new ButtonOnHoldListener(SendCommandTask.CommandType.DOWN) : null
        );
        findViewById(R.id.button_left).setOnTouchListener(
                enable ? new ButtonOnHoldListener(SendCommandTask.CommandType.LEFT) : null
        );
        findViewById(R.id.button_right).setOnTouchListener(
                enable ? new ButtonOnHoldListener(SendCommandTask.CommandType.RIGHT) : null
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
                // TODO: if we want to take a picture, need to synchronize
                //       but we can't do this on the GUI thread
                // takePicture();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private void toggleTracking() {
        if (!mTrackingEnabled) {
            mExecuteModeTask.execute(ExecuteModeTask.ModeType.TRACKING);
            mTrackingButton.setText(getString(R.string.tracking_disable));
            mTrackingEnabled = true;
        }
        else {
            mTrackingButton.setText(getString(R.string.tracking_enable));
            mExecuteModeTask.cancel(false);
            mTrackingEnabled = false;
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
    private void displayImageByteArray(byte[] byteArray, int offset, int size) {
        showLoading(false);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray, offset, size);
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        // At this point if the image data is so corrupted that the markers are messed up the bimap
        // can be null
        if (bitmap == null)
            return;

        mPictureView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, mPictureView.getWidth(), mPictureView.getHeight(), false));
        if (!State.backup_switch_state)
            savedImageName = Util.saveImage(byteArray, offset, size);

        // Note: when switching from tracking to another mode, the last track will be off cause this
        // wont run!!!
        if (mCurrentMode == ExecuteModeTask.ModeType.TRACKING) {
            if (processSavedImage())
                displayImageFileName(mTrackingImageViewFileName);
        }


    }

    private void displayImageFileName(String filename) {
        showLoading(false);
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, filename);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        mPictureView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, mPictureView.getWidth(), mPictureView.getHeight(), false));
    }

    //----------------------------------------------------------------------------------------------
    private void enableAccelerometer(boolean enable) {
        if (enable) {
            mSensorManager.registerListener(mDirectionDetector, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mDirectionDetector, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {

            if (mSendCommandTask != null) {
                mSendCommandTask.cancel(false);
            }
            mAccelMovement = false;

            mSensorManager.unregisterListener(mDirectionDetector);
        }
    }

    //----------------------------------------------------------------------------------------------
    private void enableActions(boolean enable) {
        mAllowActions = enable;
        enableButtons(enable);
    }

    //----------------------------------------------------------------------------------------------
    private void enableButtons(boolean enable) {
        for (FloatingActionButton btn : mButtonArray) {
            btn.setAlpha(enable ? 1f : 0.3f);
            btn.setClickable(enable);
        }
        Log.i("ENABLE", String.valueOf(enable));
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
    public boolean getHoldingButton() { return mHoldingButton; }
    public boolean getAccelMovment() { return mAccelMovement; }
    public boolean getDetectedMotion() { return mDetectedMotion; }

    //----------------------------------------------------------------------------------------------
    private class ButtonOnHoldListener implements View.OnTouchListener {
        SendCommandTask.CommandType mCmd;

        public ButtonOnHoldListener(SendCommandTask.CommandType cmd) {
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

    //----------------------------------------------------------------------------------------------
    private class ModeSelectedListener implements TabLayout.OnTabSelectedListener {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if (mExecuteModeTask != null) {
                mExecuteModeTask.cancel(false);
            }

            toastMessage = tab.getText().toString();
            toast.out(toastMessage);
            Log.i("TAB_POSITION", String.valueOf(tab.getPosition()));

            mExecuteModeTask = new ExecuteModeTask(mHandler);
            ViewFlipper vf = (ViewFlipper) findViewById( R.id.viewFlipper );
            vf.setDisplayedChild(vf.indexOfChild(findViewById(R.id.section_buttons)));
            if ((mCurrentMode == ExecuteModeTask.ModeType.TRACKING) && mCanSendCommands.get()) {

                enableActions(true);
            }

            switch (tab.getPosition()) {
                case 0:
                    mCurrentMode = ExecuteModeTask.ModeType.MANUAL;
                    if (mAllowActions && mCanSendCommands.get())
                        enableButtons(true);
                    break;
                case 1:
                    enableButtons(false);
                    mCurrentMode = ExecuteModeTask.ModeType.AUTO;
                    break;
                case 2:
                    enableButtons(false);
                    mCurrentMode = ExecuteModeTask.ModeType.SECURITY;
                    break;
                case 3:
                    if (!State.backup_switch_state) {
                        // TODO STOP THE TAB FROM SWITCHING!!!!
                        toast.out("Please enable photo backup to enable this feature!");
                        break;
                    }

                    if (savedImageName == null) {
                        enableActions(false);
                        takePicture();
                    }
                    enableButtons(false);
                    mCurrentMode = ExecuteModeTask.ModeType.TRACKING;
                    vf.setDisplayedChild(vf.indexOfChild(findViewById(R.id.section_tracking)));
                    return;
                default:
                    break;
            }

            mLastTab = tab.getPosition();
            mExecuteModeTask.execute(mCurrentMode);
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}
        @Override
        public void onTabReselected(TabLayout.Tab tab) {}
    }

    // TODO: BUG if you switch manual -> auto -> manual really fast,
    // actions will be enabled
    //----------------------------------------------------------------------------------------------
    private static class CommunicationHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_NIOS_RESPONSE:
                    // TODO: Handle all types of messages
                    // TODO: Size check
                    byte[] receiveMessage = (byte[]) msg.obj;
                    if (Util.uByte(receiveMessage[0]) == MessageConstants.ID_RESPONSE) {
                        if (Util.uByte(receiveMessage[4]) == MessageConstants.RESPONSE_NO_ERROR) {
                            ref.toast.out("Command Successful!");
                        }
                        else if (Util.uByte(receiveMessage[4]) == MessageConstants.RESPONSE_NIOS_HANDSHAKE) {
                            ref.toast.out("Handshake!");
                            State.heartBeatTimmer.cancel();
                        }
                        else {
                            ref.toast.out("Command: " + receiveMessage[2] +
                                    " failed with code: " + receiveMessage[3]);
                        }
                    }
                    else if (Util.uByte(receiveMessage[0]) == MessageConstants.ID_MESG_IMAGE) {
                        ref.displayImageByteArray(receiveMessage, 3, (receiveMessage[1] << 8) + Util.uByte(receiveMessage[2]));
                    }
                    else if (Util.uByte(receiveMessage[0]) == MessageConstants.ID_MESG_MOTION) {
                        ref.mDetectedMotion = (receiveMessage[3] == 0x01);
                    }

                    // TODO Make this more robust as discussed
                    ref.enableActions(
                            (ref.mCurrentMode == ExecuteModeTask.ModeType.MANUAL ||
                             ref.mCurrentMode == ExecuteModeTask.ModeType.TRACKING) &&
                            !ref.mHoldingButton &&
                            !ref.mAccelMovement
                    );

                    mCanSendCommands.set(true);
                    break;

                case MessageConstants.MESSAGE_UI_UPDATE:
                    if (msg.arg1 == MessageConstants.UI_UPDATE_LOADING_IMAGE) {
                        ref.showLoading(msg.arg2 == MessageConstants.TRUE);
                    }
                    break;
            }
        }
    }

    //------------------
    // Image Processing
    //----------------------------------------------------------------------------------------------
    private void processImageMoveTouch (int x, int y) {
        int width = mPictureView.getWidth();
        int height = mPictureView.getHeight();
        int x_relative = x - width/2;
        // This is because y increases as you go lower on the screen
        int y_relative = (height - y) - height/2;
        int x_factor = width/2/X_MAX_ANGLE;
        int y_factor = height/2/Y_MAX_ANGLE;
        int x_angle = x_relative/x_factor;
        int y_angle = y_relative/y_factor;
        rotateTouch(x_angle, y_angle);
    }

    // TODO Move this to utils maybe
    //----------------------------------------------------------------------------------------------
    private boolean processImageTrackingTouch(int eventX, int eventY) {

        int cols = mRgba.cols();
        int rows = mRgba.rows();

        // Ratio
        float xRatio = ((float)cols)/((float)mPictureView.getWidth());
        float yRatio = ((float)rows)/((float)mPictureView.getHeight());

        int x = (int) (eventX * xRatio);
        int y = (int) (eventY * yRatio);


        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = Util.converScalarHsv2Rgba(mBlobColorHsv);

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return true;
    }

    //----------------------------------------------------------------------------------------------
    private boolean processSavedImage() {

        if (savedImageName == null)
            return false;

        if (!mIsColorSelected)
            return false;

        mRgba  = imread(savedImageName);
        mDetector.process(mRgba);
        List<MatOfPoint> contours = mDetector.getContours();
        if (contours.size() == 0)
            return false;

        // We now need to pick the largest contour
        List<MatOfPoint> pickedContour = new ArrayList();
        pickedContour.add(Util.largerCountour(contours));


        Imgproc.drawContours(mRgba, pickedContour, -1, CONTOUR_COLOR);

        // Get and save the centroid of the detected point
        mTrackedBlobCenter = Util.contourCenter(pickedContour.get(0));

        // This generates the color that we are tracking as a reference
        // TODO integrate this
        //Mat colorLabel = mRgba.submat(4, 68, 4, 68);
        //colorLabel.setTo(mBlobColorRgba);

        // TODO figure this out!
        //Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
        //mSpectrum.copyTo(spectrumLabel);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path, mTrackingImageViewFileName);
        // Check if this over writes the image.
        Boolean bool = Imgcodecs.imwrite(file.toString(), mRgba);


        return bool;
    }

    //----------------------------------------------------------------------------------------------
}

