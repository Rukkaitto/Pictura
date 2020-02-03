package com.lucasgoudin.pictura;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons
        TextView toGrayBtn = findViewById(R.id.toGrayBtn);
        TextView brightnessBtn = findViewById(R.id.brightnessBtn);
        TextView contrastBtn = findViewById(R.id.contrastBtn);
        TextView improveBtn = findViewById(R.id.improveBtn);
        TextView tintBtn = findViewById(R.id.tintBtn);
        TextView blurBtn = findViewById(R.id.blurBtn);

        final ArrayList<TextView> buttons = new ArrayList<TextView>();
        buttons.add(toGrayBtn);
        buttons.add(brightnessBtn);
        buttons.add(contrastBtn);
        buttons.add(improveBtn);
        buttons.add(tintBtn);
        buttons.add(blurBtn);

        // Slider
        final SeekBar sb = findViewById(R.id.seekBar);

        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);

        Bitmap preview = makePreview(image);

        RoundedBitmapDrawable image_d = RoundedBitmapDrawableFactory.create(this.getResources(), preview);
        image_d.setCornerRadius(Settings.CORNER_RADIUS);


        for(final TextView tv : buttons) {
            tv.setCompoundDrawablesRelativeWithIntrinsicBounds(null, image_d, null, null);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(final TextView tv2 : buttons) {
                        tv2.setTextColor(Color.parseColor("#C5C5C5"));
                    }
                    tv.setTextColor(Color.WHITE);
                    sb.setProgress(0);
                    sb.setVisibility(View.VISIBLE);
                }
            });
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

        return Bitmap.createScaledBitmap(result, Settings.PREVIEW_SIZE, Settings.PREVIEW_SIZE, true);
    }



}

