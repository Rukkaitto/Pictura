package com.lucasgoudin.pictura;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons
        TextView toGrayBtn =  findViewById(R.id.toGrayBtn);
        TextView brightnessBtn =  findViewById(R.id.brightnessBtn);
        TextView contrastBtn =  findViewById(R.id.contrastBtn);
        TextView improveBtn =  findViewById(R.id.improveBtn);
        TextView tintBtn =  findViewById(R.id.tintBtn);
        TextView blurBtn =  findViewById(R.id.blurBtn);

        ArrayList<TextView> buttons = new ArrayList<TextView>();
        buttons.add(toGrayBtn);
        buttons.add(brightnessBtn);
        buttons.add(contrastBtn);
        buttons.add(improveBtn);
        buttons.add(tintBtn);
        buttons.add(blurBtn);



        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);

        Bitmap preview = makePreview(image);

        RoundedBitmapDrawable image_d = RoundedBitmapDrawableFactory.create(this.getResources(), preview);
        image_d.setCornerRadius(360);


        for(TextView tv : buttons) {
            tv.setCompoundDrawablesRelativeWithIntrinsicBounds(null, image_d, null, null);
        }



    }


    Bitmap makePreview(Bitmap image) {
        Bitmap result;
        int dimension, imageWidth, imageHeight;

        imageWidth = image.getWidth();
        imageHeight = image.getHeight();

        if(imageWidth > imageHeight) {
            dimension = imageHeight;
            int offset = (image.getWidth() - dimension) / 2;
            result = Bitmap.createBitmap(image, offset, 0, dimension, dimension);
        } else {
            dimension = imageWidth;
            int offset = (image.getHeight() - dimension) / 2;
            result = Bitmap.createBitmap(image, 0, offset, dimension, dimension);
        }

        return Bitmap.createScaledBitmap(image, 150, 150, true);
    }



}

