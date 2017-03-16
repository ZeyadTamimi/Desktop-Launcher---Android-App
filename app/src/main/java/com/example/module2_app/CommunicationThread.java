package com.example.module2_app;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.module2_app.MessageConstants;

class CommunicationThread extends Thread {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler mHandler;

    private byte[] mmBuffer; // mmBuffer store for the stream

    public CommunicationThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        mHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        mmBuffer = new byte[65000];
        int numBytes;
        int messageSize;

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                numBytes = 0;
                messageSize = 0;


                // Try to read the message header
                while (numBytes < MessageConstants.SIZE_FIELD_HEADER)
                    numBytes += mmInStream.read(mmBuffer, numBytes, MessageConstants.SIZE_FIELD_HEADER - numBytes);

                // Calculate the next number of bytes to read
                messageSize = + (Util.uByte(mmBuffer[1]) << 8) + Util.uByte(mmBuffer[2]);

                // Read the rest of the message
                while (numBytes < messageSize + MessageConstants.SIZE_FIELD_HEADER)
                    numBytes += mmInStream.read(mmBuffer, numBytes, messageSize + MessageConstants.SIZE_FIELD_HEADER - numBytes);
                // Send the obtained bytes to the UI activity.
                Message readMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, messageSize, -1, mmBuffer);
                readMsg.sendToTarget();
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        try {
            // Write the message to the bluetooth socket.
            mmOutStream.write(bytes);

            // Share the sent message with the UI activity.
            Message writtenMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            mHandler.sendMessage(writeErrorMsg);
        }
    }

    public void commandMoveTime(int direction, long time) {
        byte[] moveCommand = new byte[MessageConstants.SIZE_FIELD_HEADER +
                MessageConstants.SIZE_COMMAND_MOVE_TIME];

        moveCommand[0] = (byte) MessageConstants.ID_COMMAND_MOVE_TIME;
        moveCommand[1] = (byte) (MessageConstants.SIZE_COMMAND_MOVE_TIME >> 8);
        moveCommand[2] = (byte) (MessageConstants.SIZE_COMMAND_MOVE_TIME & 0xFF);
        moveCommand[3] = (byte) (direction & 0xFF);
        moveCommand[4] = (byte) ((time >> 24) & 0xFF);
        moveCommand[5] = (byte) ((time >> 16) & 0xFF);
        moveCommand[6] = (byte) ((time >> 8) & 0xFF);
        moveCommand[7] = (byte) (time & 0xFF);
        write(moveCommand);
    }

    public void commandFire() {
        byte[] fireCommand = new byte[MessageConstants.SIZE_FIELD_HEADER];

        fireCommand[0] = (byte) MessageConstants.ID_COMMAND_FIRE;
        fireCommand[1] = (byte) (0);
        fireCommand[2] = (byte) (0);
        write(fireCommand);
    }

    public void requestMessage(int messageId) {
        byte[] message = new byte[MessageConstants.SIZE_FIELD_HEADER +
                MessageConstants.SIZE_REQUEST];
        message[0] = (byte) MessageConstants.ID_REQUEST;
        message[1] = (byte) (MessageConstants.SIZE_REQUEST >> 8);
        message[2] = (byte) (MessageConstants.SIZE_REQUEST & 0xFF);
        message[MessageConstants.SIZE_FIELD_HEADER] = (byte) messageId;
        write(message);
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
