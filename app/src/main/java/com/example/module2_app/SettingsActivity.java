package com.example.module2_app;

import android.os.Bundle;
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


    public Switch backup_switch;
    public SeekBar turret_speed_bar;
    public boolean backup_switch_state;
    public int turret_speed_bar_value;

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


    }
    @Override
    public void onPause() {
        super.onPause();
        Log.i("message","pausing settings");
        backup_switch_state = backup_switch.isChecked();
        turret_speed_bar_value = turret_speed_bar.getProgress();
        State.backup_switch_state = backup_switch_state;
        State.turret_speed_bar_value = turret_speed_bar_value;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i("message","Resuming");
        backup_switch_state = State.backup_switch_state;
        turret_speed_bar_value = State.turret_speed_bar_value;
        backup_switch.setChecked(backup_switch_state);
        turret_speed_bar.setProgress(turret_speed_bar_value);

    }


}