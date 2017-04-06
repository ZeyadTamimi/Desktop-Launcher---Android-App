package com.example.module2_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Switch;

/**
 * Created by deh on 3/13/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public Switch backup_switch;
    public SeekBar turret_speed_bar;
    public SeekBar image_resolution_bar;
    public boolean backup_switch_state;
    public int turret_speed_bar_value;
    public int image_resolution_bar_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("message","creating settings activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_settings_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        backup_switch = (Switch) findViewById(R.id.backup_switch);
        turret_speed_bar = (SeekBar) findViewById(R.id.turret_speed_bar);
        image_resolution_bar = (SeekBar) findViewById(R.id.resolution_bar);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        }


    }
    @Override
    public void onPause() {
        super.onPause();

        backup_switch_state = backup_switch.isChecked();
        turret_speed_bar_value = turret_speed_bar.getProgress() + 1;
        image_resolution_bar_value = image_resolution_bar.getProgress();

        State.backup_switch_state = backup_switch_state;
        State.turret_speed_bar_value = turret_speed_bar_value;
        State.image_resolution_bar_value = image_resolution_bar_value;
    }

    @Override
    public void onResume(){
        super.onResume();

        backup_switch_state = State.backup_switch_state;
        turret_speed_bar_value = State.turret_speed_bar_value - 1;
        image_resolution_bar_value = State.image_resolution_bar_value;

        backup_switch.setChecked(backup_switch_state);
        turret_speed_bar.setProgress(turret_speed_bar_value);
        image_resolution_bar.setProgress(image_resolution_bar_value);

    }


}