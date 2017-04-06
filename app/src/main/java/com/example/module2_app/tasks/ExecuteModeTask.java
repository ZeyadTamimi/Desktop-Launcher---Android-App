package com.example.module2_app.tasks;

import android.os.AsyncTask;
import android.os.Handler;

import com.example.module2_app.MainActivity;
import com.example.module2_app.MessageConstants;
import com.example.module2_app.State;

// TODO: refactor while(isCancelled()) to doInBackground
public class ExecuteModeTask extends AsyncTask<ExecuteModeTask.ModeType, Void, Void> {
    public enum ModeType {
        MANUAL, AUTO, SECURITY, TRACKING;
    }

    private Handler mHandler;

    public ExecuteModeTask(Handler handler) {
        mHandler = handler;
    }

    @Override
    protected Void doInBackground(ModeType... params) {
        switch (params[0]){
            case MANUAL: enterManualMode(); break;
            case AUTO: enterAutoMode(); break;
            case SECURITY: enterSecurityMode(); break;
            case TRACKING: enterTrackingMode(); break;
        }
        return null;
    }

    @Override
    protected void onCancelled() {

    }

    @Override
    protected void onPostExecute(Void result) {

    }

    private void enterManualMode() {

    }

    private void enterAutoMode() {
        while(!isCancelled()) {
            if (!MainActivity.mCanSendCommands.get())
                continue;

            // TODO: can modify movement here
            MainActivity.mCanSendCommands.set(false);
            State.mmCommunicationThread.commandMoveTimeSpeed(
                    MessageConstants.MOVE_RIGHT, State.turret_speed_bar_value + 1, 100000);

            while (!MainActivity.mCanSendCommands.get());
            MainActivity.mCanSendCommands.set(false);
            mHandler.sendMessage(mHandler.obtainMessage(
                    MessageConstants.MESSAGE_UI_UPDATE,
                    MessageConstants.UI_UPDATE_LOADING_IMAGE,
                    MessageConstants.TRUE));

            // May need synchronization here but requesting image is slow so it's fine

            State.mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_IMAGE);
        }
    }

    private void enterSecurityMode() {
        while(!isCancelled()) {
            if (!MainActivity.mCanSendCommands.get())
                continue;

            MainActivity.mCanSendCommands.set(false);
            State.mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_MOTION);
            while (!MainActivity.mCanSendCommands.get());
            if (MainActivity.ref.getDetectedMotion()) {
                MainActivity.mCanSendCommands.set(false);
                State.mmCommunicationThread.commandFire();

                while (!MainActivity.mCanSendCommands.get());
                MainActivity.mCanSendCommands.set(false);
                mHandler.sendMessage(mHandler.obtainMessage(
                        MessageConstants.MESSAGE_UI_UPDATE,
                        MessageConstants.UI_UPDATE_LOADING_IMAGE,
                        MessageConstants.TRUE));
                State.mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_IMAGE);
            }
        }
    }

    private void enterTrackingMode() {
        while(!isCancelled()) {
            if (!MainActivity.mTrackingEnabled.get())
                continue;

            if (!MainActivity.mCanSendCommands.get())
                continue;

            MainActivity.mCanSendCommands.set(false);
            mHandler.sendMessage(mHandler.obtainMessage(
                    MessageConstants.MESSAGE_UI_UPDATE,
                    MessageConstants.UI_UPDATE_LOADING_IMAGE,
                    MessageConstants.TRUE));
            // First we request that the NIOS II send us the image
            State.mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_IMAGE);

            while(!MainActivity.mCanSendCommands.get());
            // TODO Verify that we received the correct response

            if (MainActivity.mTrackedBlobCenter == null)
                continue;
            // Grab the point and calculate the angle to rotate
            int width = 320;
            int height = 240;
            int x_relative = (int) (MainActivity.mTrackedBlobCenter.x - width/2);
            int y_relative = (int) ((height - MainActivity.mTrackedBlobCenter.y) - height/2);

            int x_factor = width/2/MainActivity.X_MAX_ANGLE;
            int y_factor = height/2/MainActivity.Y_MAX_ANGLE;
            int x_angle = x_relative/x_factor;
            int y_angle = y_relative/y_factor;

            // Get the smallest angle to the point we wish to track.
            if (x_angle <= 127 && x_angle >= -128 && y_angle <= 127 && y_angle >= -128) {
                MainActivity.mCanSendCommands.set(false);
                State.mmCommunicationThread.commandMoveAngle(x_angle, y_angle);
            }

            MainActivity.mTrackedBlobCenter = null;
        }

    }
}

