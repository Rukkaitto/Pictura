package com.lucasgoudin.pictura.Filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;


public class DrawingView extends View {
    private AppCompatActivity context;
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private Canvas drawCanvas;
    private int paintColor = 0xFF660000; //temp
    private Bitmap canvasBitmap;
    public static Bitmap full_image;

    /**
     * Initialise the drawingView
     * @param context
     * @param attrs
     */
    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    /**
     * Initialise the drawingView
     * @param context
     */
    public DrawingView(Context context){
        super(context);
    }

    /**
     * Initialise all the variables and parameters
     */
    public void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(15.0f);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    /**
     * Initialise the variable context. Used for the creation of the icons
     * @param context
     */
    public void addContext(AppCompatActivity context){
        this.context = context;
    }

    /**
     * Initialise the canvas with the current Picture
     * @param full_bitmap
     */
    public void initBitmap( Bitmap full_bitmap){
        full_image = full_bitmap;
        canvasBitmap = Bitmap.createBitmap(full_image.getWidth(), full_image.getHeight(), Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawBitmap(full_image, new Matrix(),null);
    }

    /**
     * Add the path done by the user with his finger on the screen and draw it on the canvas
     * @param event
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        //respond to down, move and up events
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        //redraw
        invalidate();
        return true;
    }

    /**
     * Draw the paint on the bitmap
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    /**
     * Save the drawing
     */
    public void save(){
        full_image = canvasBitmap;
    }

    /**
     * Cancel all the current drawing
     */
    public void clear(){
        drawCanvas.drawBitmap(full_image, new Matrix(), null);
        drawPath.reset();
    }

    /**
     * Change the color of the Brush
     * This function receive a float between 0 and 360.
     * @param progress
     */
    public void modifColor(float progress){
        int[] rgb = rgb360(progress);
        drawPaint.setARGB(255, rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Creates a int[] -> {r,g,b}
     * realise the conversion HSV to rgb.
     * @param progress
     * @return the 3 values r,g,b
     */
    public int[] rgb360(float progress){
        float c = (float) (1*0.8);
        float x = c * (1-Math.abs((progress/60)%2 -1));
        float m = (float) (0.8 - c);

        int[] rgb = new int[3];

        float r = 0, g = 0, b = 0;
        if(progress >= 0f && progress < 60f){
            r = c;
            g = x;
            b = 0;
        }
        else if(progress >= 60f && progress < 120f){
            r = x;
            g = c;
            b = 0;
        }
        else if(progress >= 120f && progress < 180f){
            r = 0;
            g = c;
            b = x;
        }
        else if(progress >= 180f && progress < 240f){
            r = 0;
            g = x;
            b = c;
        }
        else if(progress >= 240f && progress < 300f){
            r = x;
            g = 0;
            b = c;
        }
        else if(progress >= 300f && progress < 360f){
            r = c;
            g = 0;
            b = x;
        }
        rgb[0] = (int)((r+m)*255);
        rgb[1] = (int)((g+m)*255);
        rgb[2] = (int)((b+m)*255);
        return rgb;
    }

    /**
     * Create the preview for all the drawing button in the application
     * @param btn set the preview with to the good button
     * @param img set the image to the TextView
     */
    public void drawingPreview(TextView btn, Bitmap img) {
        Bitmap bmp = scaleBitmap(img);

        RoundedBitmapDrawable drawable;
        drawable = RoundedBitmapDrawableFactory.create(context.getResources(), bmp);
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
    }

    /**
     * Scales the bitmap to the preview's dimensions
     * @param image the image to scale
     * @return the scaled bitmap
     */
    private Bitmap scaleBitmap(Bitmap image) {
        Bitmap result;
        int dimension, imageWidth, imageHeight;

        imageWidth = image.getWidth();
        imageHeight = image.getHeight();

        if(imageWidth > imageHeight) {
            dimension = imageHeight;
            int offset = (image.getWidth() - dimension) / 3;
            result = Bitmap.createBitmap(image, offset, 0, dimension, dimension);
        } else {
            dimension = imageWidth;
            int offset = (image.getHeight() - dimension) / 3;
            result = Bitmap.createBitmap(image, 0, offset, dimension, dimension);
        }

        float density = context.getResources().getDisplayMetrics().density;
        return Bitmap.createScaledBitmap(result, (int) (50 * density), (int) (50 * density), true);
    }

}
