package com.example.module2_app.tasks;

import android.os.AsyncTask;
import android.os.Handler;

import com.example.module2_app.MainActivity;
import com.example.module2_app.MessageConstants;
import com.example.module2_app.State;

// TODO: when updating UI, disable buttons
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

            // TODO: movement
            MainActivity.mCanSendCommands.set(false);
            State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_RIGHT, 200000);
            while (!MainActivity.mCanSendCommands.get());
            MainActivity.mCanSendCommands.set(false);
            // TODO: take picture and send message to MainActivity  to update UI
            // MainActivity.ref.takePicture(); // this line will crash
            State.mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_IMAGE);
        }
    }

    private void enterSecurityMode() {
        while(!isCancelled()) {
            if (!MainActivity.mCanSendCommands.get())
                continue;

            MainActivity.mCanSendCommands.set(false);
            // TODO: finish
            // if (State.mmCommunicationThread.commandPollMovement()) {
                // MainActivity.ref.fire();
                // while (!MainActivity.mCanSendCommands.get());
                // MainActivity.ref.takePicture();
            // }
        }
    }

    private void enterTrackingMode() {
        while(!isCancelled()) {
            if (!MainActivity.mCanSendCommands.get())
                continue;

            MainActivity.mCanSendCommands.set(false);
            // TODO: do tracking stuff
        }

    }
}

