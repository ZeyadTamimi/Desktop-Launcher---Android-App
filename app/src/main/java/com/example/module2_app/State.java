package com.example.module2_app;


import android.bluetooth.BluetoothSocket;

public class State {

    public static boolean backup_switch_state = false;
    public static int turret_speed_bar_value = 2;

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
