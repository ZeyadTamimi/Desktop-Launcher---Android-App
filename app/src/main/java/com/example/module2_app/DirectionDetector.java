package com.example.module2_app;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DirectionDetector implements SensorEventListener {
    //----------------------------------------------------------------------------------------------
    public interface OnDirectionChangeListener {
        void onRest();
        void onUp();
        void onDown();
        void onLeft();
        void onRight();
    }

    private OnDirectionChangeListener mListener;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float R[] = new float[9];
    private float I[] = new float[9];
    private float orientation[] = new float[3];


    //----------------------------------------------------------------------------------------------
    public DirectionDetector(OnDirectionChangeListener listener) {
        this.mListener = listener;
    }
    public void setOnTiltListener(OnDirectionChangeListener listener) {
        this.mListener = listener;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {
                SensorManager.getOrientation(R, orientation);
                // https://developer.android.com/guide/topics/sensors/sensors_position.html#sensors-pos-orient
                // orientation array: 0=azimuth, 1=pitch, 2=roll
                /*
                Log.i("orientation: ", String.format("azimuth: %f, pitch: %f, roll: %f",
                        orientation[0], orientation[1], orientation[2]));
                */
                if (mListener != null) {
                    if (orientation[1] > -0.25f) mListener.onDown(); // screen facing sky
                    else if (orientation[1] < -1.04f) mListener.onUp(); // screen facing user
                    else if (orientation[2] < -0.2f) mListener.onLeft(); // tilt left or roll left
                    else if (orientation[2] > 0.5f) mListener.onRight(); // tilt right or roll right
                    else mListener.onRest();
                }
            }
        }
    }

}
