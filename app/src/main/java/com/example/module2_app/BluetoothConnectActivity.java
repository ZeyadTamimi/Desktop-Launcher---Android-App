package com.example.module2_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class BluetoothConnectActivity extends  AppCompatActivity {
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothArrayAdaptor mPairedAdapter;
    private ArrayList<String> mPairedStringArray;
    private ArrayList<BluetoothDevice> mPairedDeviceArray;
    private BluetoothAdapter mBluetoothAdapter;

    private TextView mTextConnected;
    private static ConnectThread btConnectThread;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setConnected(msg.arg1);
        }
    };

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

        mTextConnected = (TextView) findViewById(R.id.text_connected_status);

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
                    btConnectThread.cancel();
                    setConnected(pos);
                }
                else {
                    btConnectThread = new ConnectThread(mPairedDeviceArray.get(pos), mHandler, pos);
                    btConnectThread.start();
                }
            }
        });

        if (!State.btConnected()) {
            refresh();
        }
    }

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
            if (d.getName().startsWith("DTL")) {
                mPairedStringArray.add(d.getName());
                mPairedDeviceArray.add(d);
            }
        }
        mPairedAdapter.notifyDataSetChanged();
    }

    public void setConnected(int pos) {
        if (State.btConnected()) {
            mTextConnected.setText("Connected");
            mPairedAdapter.setConnected(pos);
        }
        else {
            mTextConnected.setText("Not Connected");
            mPairedAdapter.setDisconnected(pos);
        }
        mPairedAdapter.notifyDataSetChanged();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final Handler mmHanlder;
        private final int mPos;
        public ConnectThread(BluetoothDevice device, Handler handler, int pos) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            mmHanlder = handler;
            mPos = pos;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        @Override
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
            com.example.module2_app.State.setBtSocket(mmSocket);
            mHandler.obtainMessage(MessageConstants.MESSAGE_READ, mPos).sendToTarget();
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


