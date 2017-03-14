package com.example.module2_app;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class AppToast {
    private static final long WAIT_TIME = 2000;

    private Toast toast;
    private Context context;

    private String delayMessage;
    private long lastTime;

    public AppToast(Context context) {
        this.context = context;
        toast = new Toast(context);
    }

    public void out(String message) {
        toast.cancel();
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 250);
    }

    public void outWithDelayed(String message) {
        out(message);
        delayMessage = message;
        lastTime = System.currentTimeMillis();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= lastTime + WAIT_TIME) {
                    
                }
            }
        }, WAIT_TIME);
    }
}
