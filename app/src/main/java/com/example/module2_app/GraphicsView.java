package com.example.module2_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GraphicsView extends View{

    public GraphicsView(Context context) {
        super(context);
    }

    public GraphicsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GraphicsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw (Canvas canvas) {
        canvas.drawColor( Color.LTGRAY );
        Paint paint = new Paint(); // create a new “paint brush” to draw on canvas
        int max_x = getWidth() - 1; // get width and height of the view in pixel
        int max_y = getHeight() - 1;

        paint.setColor(Color.DKGRAY);
        paint.setTextSize(100.0f);
        paint.setAlpha(255);
        canvas.drawCircle(max_x / 2, max_y / 2, 10, paint);
    }


}
