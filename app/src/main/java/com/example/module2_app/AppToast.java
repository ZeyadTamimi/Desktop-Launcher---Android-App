package com.example.module2_app;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class AppToast {
    private Toast toast;
    private Context context;

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
}
