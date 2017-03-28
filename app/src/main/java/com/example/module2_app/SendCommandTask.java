package com.example.module2_app;

import android.os.AsyncTask;

// TODO: Remove FIRE command, keeping it here for testing purposes
public class SendCommandTask extends AsyncTask<SendCommandTask.CommandType, Void, Void> {

    enum CommandType {
        UP, DOWN, LEFT, RIGHT, FIRE;
    }

    @Override
    protected Void doInBackground(CommandType... params) {
        while(!isCancelled()) {
            if (!MainActivity.mCanSendCommands.get())
                continue;

            // TODO: go through logic for multiple commands?
            for (int i = 0; i < params.length; i++) {
                while (!MainActivity.mCanSendCommands.get());
                switch (params[i]) {
                    case UP:
                        State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_UP, 0);
                        break;
                    case DOWN:
                        State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_DOWN, 0);
                        break;
                    case LEFT:
                        State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_LEFT, 50000);
                        break;
                    case RIGHT:
                        State.mmCommunicationThread.commandMoveTime(MessageConstants.MOVE_RIGHT, 50000);
                        break;
                    case FIRE:
                        State.mmCommunicationThread.commandFire();
                        break;
                }
                MainActivity.mCanSendCommands.set(false);
            }
        }
        return null;
    }

    @Override
    protected void onCancelled() {

    }

    @Override
    protected void onPostExecute(Void result) {

    }
}
