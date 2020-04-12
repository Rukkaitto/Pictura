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
 * Creation of stickers on the image
 */
public class MakeSticker {
    private AppCompatActivity context;
    Bitmap bmp, image, full_image;

    public static Bitmap img_fusion, img_full_fusion;

    public MakeSticker(AppCompatActivity context, Bitmap image, Bitmap full_image) {
        this.context = context;
        this.image = image;
        this.full_image = full_image;
    }

    /**
     * Create the preview for all the stickers in the application
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
     * Set the sticker to the image and to the full_image
     * @param nameSticker is the sticker to apply
     */
    public void ProcessingBitmap(String nameSticker){
        int x = 0, y = 0;

        // Make the image mutable
        android.graphics.Bitmap.Config bitmapConfig = image.getConfig();
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        image = image.copy(bitmapConfig, true);

        // Make the sticker mutable
        Bitmap sticker = selectSticker(nameSticker);

        android.graphics.Bitmap.Config bitmapConfigSticker = sticker.getConfig();
        if(bitmapConfigSticker == null) {
            bitmapConfigSticker = android.graphics.Bitmap.Config.ARGB_8888;
        }
        sticker = sticker.copy(bitmapConfigSticker, true);

        // Random size of the sticker
        if(sticker.getWidth() < sticker.getHeight()){
            int tmp = sticker.getHeight();
            y = ((int) (Math.random() * (sticker.getHeight() / 2)));
            x = ((sticker.getWidth() * y) / tmp);
        } else if(sticker.getWidth() > sticker.getHeight()){
            int tmp = sticker.getWidth();
            x = (int) (Math.random() * (sticker.getWidth() / 2));
            y = ((x * sticker.getHeight()) / tmp);
        }

        sticker = Bitmap.createScaledBitmap(sticker,x,y,false);

        // Fusion of the image with the sticker
        Bitmap fusion = Bitmap.createBitmap(image.getWidth(), image.getHeight(), bitmapConfig);
        Bitmap full_fusion = Bitmap.createBitmap(full_image.getWidth(), full_image.getHeight(), bitmapConfig);

        // Choose a random position for the sticker
        int img_sticker_x = (int) (Math.random() * (image.getWidth() - sticker.getWidth()));
        int img_sticker_y = (int)  (Math.random() * (image.getHeight() - sticker.getHeight()));

        Canvas canvas = new Canvas();
        canvas.setBitmap(fusion);
        canvas.drawBitmap(image, new Matrix(), null);
        canvas.drawBitmap(sticker, img_sticker_x, img_sticker_y, null);

        // Fusion of the full_image with the sticker

        // Resize the sticker for the full_image
        int full_size_sticker_x = (sticker.getWidth() * full_image.getWidth()) / image.getWidth();
        int full_size_sticker_y = (sticker.getHeight() * full_image.getHeight()) / image.getHeight();

        // Change the position of the sticker for the full_image
        int full_image_sticker_x = (img_sticker_x * full_image.getWidth()) / image.getWidth();
        int full_image_sticker_y = (img_sticker_y * full_image.getHeight()) / image.getHeight();

        sticker = Bitmap.createScaledBitmap(sticker, full_size_sticker_x, full_size_sticker_y, false);

        Canvas canvas2 = new Canvas();
        canvas2.setBitmap(full_fusion);
        canvas2.drawBitmap(full_image, new Matrix(), null);
        canvas2.drawBitmap(sticker, full_image_sticker_x, full_image_sticker_y, null);

        img_fusion = fusion;
        img_full_fusion = full_fusion;
    }
    /**
     * Search the sticker to apply
     * @param sticker is the name of the sticker
     * @return the sticker bitmap
     */
    public Bitmap selectSticker(String sticker) {
        Bitmap stickerBmp = Bitmap.createBitmap(image.getWidth() ,image.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        switch(sticker){
            case "leaf" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.leaf);
                break;
            case "cat1" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.cat1);
                break;
            case "cat2" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.cat2);
                break;
            case "cat3" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.cat3);
                break;
            case "cerise" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.cherry);
                break;
            case "clemenceau" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.clemenceau);
                break;
            case "cloud" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud);
                break;
            case "crown1" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.crown1);
                break;
            case "crown2" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.crown2);
                break;
            case "donut" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.donut);
                break;
            case "egg" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.egg);
                break;
            case "fraise" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.fraise);
                break;
            case "heart" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.heart);
                break;
            case "bunny" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.lapin);
                break;
            case "meli" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.meli);
                break;
            case "apple" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.pomme);
                break;
            case "chicken" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.poulet);
                break;
            case "octopus" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.poulpe);
                break;
            case "sun" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.soleil);
                break;
            case "sweet" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.sweet);
                break;
            case "pie" :
                stickerBmp =  BitmapFactory.decodeResource(context.getResources(), R.drawable.tarte);
                break;
            default :
                break;
        }
        return stickerBmp;
    }

}
