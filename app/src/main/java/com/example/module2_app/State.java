package com.example.module2_app;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;

public class State {

    public static boolean backup_switch_state = false;
    public static int turret_speed_bar_value = 2;
    public static long lastPhotoNumber = 0;

    public static int lastConnectedPos = 0;
    public static ArrayList<String> mPairedStringArray;
    public static ArrayList<BluetoothDevice> mPairedDeviceArray;
    public static BluetoothArrayAdaptor mPairedAdapter;

    public static CommunicationThread mmCommunicationThread;

    private static BluetoothSocket btSocket;

    public static void setBtSocket(BluetoothSocket socket) {
        btSocket = socket;
    }

    public static BluetoothSocket getBtSocket() {
        return btSocket;
    }

    public static boolean btConnected() {
        return btSocket != null && btSocket.isConnected();
    }


}
