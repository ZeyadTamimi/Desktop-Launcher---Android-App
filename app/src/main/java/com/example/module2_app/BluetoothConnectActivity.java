package com.example.module2_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothConnectActivity extends  AppCompatActivity {

    // two instances of our new custom array adaptor
    private BluetoothArrayAdaptor myPairedArrayAdapter;
    // two dynamic arrays of strings (populate at run time)
    private ArrayList<String> myPairedDevicesStringArray = new ArrayList<String>();
    private ArrayList<BluetoothDevice> myPairedDevicesArray = new ArrayList<BluetoothDevice>();


    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    public static BluetoothSocket myBluetoothSocket;
    private static final String TAG = "MY_APP_DEBUG_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_bluetooth_connect_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        myPairedArrayAdapter = new BluetoothArrayAdaptor(this,
                android.R.layout.simple_list_item_1, myPairedDevicesStringArray);
        // get handles to the two list views in the Activity main layout
        ListView PairedlistView = (ListView) findViewById(R.id.paired_list);
        // set the adaptor view for both list views above
        PairedlistView.setAdapter (myPairedArrayAdapter);
        // Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // TODO: device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        PairedlistView.setOnItemClickListener (mPairedClickedHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bluetooth_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void buttonPress(View view) {
        switch(view.getId()) {
            case R.id.button_connect:
                connect();
                break;
            case R.id.button_cancel:
                cancel();
                break;
        }
    }

    public void connect() {
        MainActivity.toast.out("CONNECT");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                myPairedDevicesStringArray.add(deviceHardwareAddress);
            }

            myPairedArrayAdapter.notifyDataSetChanged();
        }


    }

    public void cancel() {
        MainActivity.toast.out("CANCEL");
    }

    private AdapterView.OnItemClickListener mPairedClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            String macAddress = myPairedDevicesStringArray.get(position);
            MainActivity.toast.out(macAddress);

            for (BluetoothDevice d : mBluetoothAdapter.getBondedDevices()) {
                if (d.getAddress().equals(macAddress)) {
                    new ConnectThread(d).run();
                }
            }
        }
    };

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            myBluetoothSocket = mmSocket;
            MainActivity.toast.out("Should be connected :)");
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
}


