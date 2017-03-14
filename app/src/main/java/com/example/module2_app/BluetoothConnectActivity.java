package com.example.module2_app;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

public class BluetoothConnectActivity extends  AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_bluetooth_connect_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
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
    }

    public void cancel() {
        MainActivity.toast.out("CANCEL");
    }
}
