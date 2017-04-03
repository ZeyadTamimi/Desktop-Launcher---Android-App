package com.example.module2_app.tasks;

import android.os.AsyncTask;

import com.example.module2_app.MainActivity;
import com.example.module2_app.MessageConstants;
import com.example.module2_app.State;

// TODO: auto, security, tracking
public class ExecuteModeTask extends AsyncTask<ExecuteModeTask.ModeType, Void, Void> {

    public enum ModeType {
        MANUAL, AUTO, SECURITY, TRACKING;
    }

    @Override
    protected Void doInBackground(ModeType... params) {
        switch (params[0]){
            case MANUAL: break;
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

    private void enterAutoMode() {
        while(!isCancelled()) {
            if (!MainActivity.mCanSendCommands.get())
                continue;

            // TODO: use MainActivity reference to do commands?
            MainActivity.mCanSendCommands.set(false);
            State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_RIGHT, 200000);
            while (!MainActivity.mCanSendCommands.get());
            State.mmCommunicationThread.requestMessage(MessageConstants.ID_MESG_IMAGE);
        }
    }

    private void enterSecurityMode() {
        while(!isCancelled()) {
            if (!MainActivity.mCanSendCommands.get())
                continue;

            MainActivity.mCanSendCommands.set(false);
        }
    }

    private void enterTrackingMode() {
        while(!isCancelled()) {
            if (!MainActivity.mCanSendCommands.get())
                continue;

            MainActivity.mCanSendCommands.set(false);
        }

    }
}

