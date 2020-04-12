package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.lucasgoudin.pictura.R;
import com.lucasgoudin.pictura.Settings;


/**
* Creation of borders on the image
 */
public class MakeBorder {
    private AppCompatActivity context;
    Bitmap bmp, image, full_image;

    public static Bitmap img_fusion, img_full_fusion;

    public MakeBorder(AppCompatActivity context, Bitmap image, Bitmap full_image) {
        this.context = context;
        this.image = image;
        this.full_image = full_image;
    }

    /**
     * Create the preview for all the borders in the application
     * @param btn set the preview with to the good button
     * @param img set the image to the TextView
     */
    public void stickerPreview(TextView btn, Bitmap img) {
        bmp = scaleBitmap(img);

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
        return Bitmap.createScaledBitmap(result, (int) (Settings.PREVIEW_SIZE * density), (int) (Settings.PREVIEW_SIZE * density), true);
    }

    /**
     * Set the border to the image and to the full_image
     * @param nameBorder is the border to apply
     */
    public void ProcessingBitmap(String nameBorder){
        // Make the image mutable
        android.graphics.Bitmap.Config bitmapConfig = image.getConfig();
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        image = image.copy(bitmapConfig, true);

        // Make the frame mutable
        Bitmap border = selectBorder(nameBorder);

        android.graphics.Bitmap.Config bitmapConfigSticker = border.getConfig();
        if(bitmapConfigSticker == null) {
            bitmapConfigSticker = android.graphics.Bitmap.Config.ARGB_8888;
        }
        border = border.copy(bitmapConfigSticker, true);

        border = Bitmap.createScaledBitmap(border,image.getWidth(),image.getHeight(),false);

        // Fusion of the image with the sticker
        Bitmap fusion = Bitmap.createBitmap(image.getWidth(), image.getHeight(), bitmapConfig);
        Bitmap full_fusion = Bitmap.createBitmap(full_image.getWidth(), full_image.getHeight(), bitmapConfig);

        Canvas canvas = new Canvas();
        canvas.setBitmap(fusion);
        canvas.drawBitmap(image, new Matrix(), null);
        canvas.drawBitmap(border, new Matrix(), null);

        // Resize the sticker for the full_image
        int full_size_sticker_x = (border.getWidth() * full_image.getWidth()) / image.getWidth();
        int full_size_sticker_y = (border.getHeight() * full_image.getHeight()) / image.getHeight();

        border = Bitmap.createScaledBitmap(border,full_size_sticker_x,full_size_sticker_y,false);

        Canvas canvas2 = new Canvas();
        canvas2.setBitmap(full_fusion);
        canvas2.drawBitmap(full_image, new Matrix(), null);
        canvas2.drawBitmap(border, new Matrix(), null);

        img_fusion = fusion;
        img_full_fusion = full_fusion;
    }

    /**
     * Search the border to apply
     * @param border is the name of the border
     * @return the border bitmap
     */
    private Bitmap selectBorder(String border){
        Bitmap borderBmp = Bitmap.createBitmap(image.getWidth() ,image.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        switch(border){
            case "border1" :
                borderBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.frame1);
                break;
            case "border2" :
                borderBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.victorian);
                break;
            case "border3" :
                borderBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.wood);
                break;
            case "border4" :
                borderBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.colors);
                break;
            case "border5" :
                borderBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.stone);
                break;
            case "border6" :
                borderBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.water);
                break;
            default :
                break;
        }
        return borderBmp;
    }
}
