package com.example.module2_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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

import java.io.IOException;
import java.util.ArrayList;

public class BluetoothConnectActivity extends  AppCompatActivity {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothArrayAdaptor mPairedAdapter;
    private ArrayList<String> mPairedStringArray;
    private ArrayList<BluetoothDevice> mPairedDeviceArray;
    private BluetoothAdapter mBluetoothAdapter;

    public static ConnectThread myConnectThread;
    public static BluetoothSocket myBluetoothSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_bluetooth_connect_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mPairedStringArray = new ArrayList<>();
        mPairedDeviceArray = new ArrayList<>();

        mPairedAdapter = new BluetoothArrayAdaptor(this,
                android.R.layout.simple_list_item_1,
                mPairedStringArray);

        ListView PairedlistView = (ListView) findViewById(R.id.paired_list);
        PairedlistView.setAdapter(mPairedAdapter);

        // Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // TODO: device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        PairedlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                if (mPairedAdapter.getConnection(pos)) {
                    mPairedAdapter.setDisconnected(pos);
                    myConnectThread.cancel();
                }
                else {
                    mPairedAdapter.setConnected(pos);
                    myConnectThread = new ConnectThread(mPairedDeviceArray.get(pos));
                    myConnectThread.start();
                }
                mPairedAdapter.notifyDataSetChanged();
            }
        });

        if (myBluetoothSocket != null && !myBluetoothSocket.isConnected()) {
            refresh();
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bluetooth_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
*/
    public void buttonPress(View view) {
        switch(view.getId()) {
            case R.id.button_refresh:
                refresh();
        }
    }

    public void refresh() {
        MainActivity.toast.out("REFRESH");
        mPairedStringArray.clear();
        mPairedDeviceArray.clear();
        for (BluetoothDevice d : mBluetoothAdapter.getBondedDevices()) {
            mPairedStringArray.add(d.getAddress());
            mPairedDeviceArray.add(d);
        }
        mPairedAdapter.notifyDataSetChanged();
    }

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


