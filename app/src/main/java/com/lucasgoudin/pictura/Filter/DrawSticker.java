package com.lucasgoudin.pictura.Filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.lucasgoudin.pictura.R;

public class DrawSticker extends View {

    Bitmap bmp, image;
    float x = 0;
    float y = 0;

    public DrawSticker(Context context, Bitmap image) {
        super(context);
        this.image = image;
        bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.leaf);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bmp, x, y, null);
    }
}
