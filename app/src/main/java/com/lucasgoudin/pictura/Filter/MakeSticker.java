package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.lucasgoudin.pictura.R;
import com.lucasgoudin.pictura.Settings;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MakeSticker {
    private AppCompatActivity context;
    Bitmap bmp, image, full_image;

    public MakeSticker(AppCompatActivity context, Bitmap image, Bitmap full_image) {
        this.context = context;
        this.image = image;
        this.full_image = full_image;
    }

    public void stickerPreview(TextView btn, Bitmap img) {
        bmp = scaleBitmap(img);

        RoundedBitmapDrawable drawable;
        drawable = RoundedBitmapDrawableFactory.create(context.getResources(), bmp);
        btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, drawable, null, null);
    }

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
