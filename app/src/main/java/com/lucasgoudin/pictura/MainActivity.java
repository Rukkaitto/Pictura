package com.lucasgoudin.pictura;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Bitmap image;
    Bitmap base_image;
    PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Image
        photoView = findViewById(R.id.photo_view);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        base_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        updateImage(image);


        //Buttons
        final ImageButton saveBtn = findViewById(R.id.saveBtn);
        final ImageButton loadBtn = findViewById(R.id.loadBtn);
        final ImageButton cameraBtn = findViewById(R.id.cameraBtn);
        final ImageButton resetBtn = findViewById(R.id.resetBtn);



        // Filter buttons
        final TextView toGrayBtn = findViewById(R.id.toGrayBtn);
        final TextView brightnessBtn = findViewById(R.id.brightnessBtn);
        final TextView contrastBtn = findViewById(R.id.contrastBtn);
        final TextView improveBtn = findViewById(R.id.improveBtn);
        final TextView tintBtn = findViewById(R.id.tintBtn);
        final TextView blurBtn = findViewById(R.id.blurBtn);

        final ArrayList<TextView> buttons = new ArrayList<TextView>();
        buttons.add(toGrayBtn);
        buttons.add(brightnessBtn);
        buttons.add(contrastBtn);
        buttons.add(improveBtn);
        buttons.add(tintBtn);
        buttons.add(blurBtn);

        // Slider
        final SeekBar sb = findViewById(R.id.seekBar);
        sb.setMax(200);
        sb.setProgress(100);


        //Previews
        Preview toGrayPreview = new Preview(image, Filter.TOGRAY, this);
        Preview brightnessPreview = new Preview(image, Filter.BRIGHTNESS, this);
        Preview contrastPreview = new Preview(image, Filter.NOFILTER, this);
        Preview improvePreview = new Preview(image, Filter.NOFILTER, this);
        Preview tintPreview = new Preview(image, Filter.NOFILTER, this);
        Preview blurPreview = new Preview(image, Filter.NOFILTER, this);

        toGrayBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, toGrayPreview.getPreview(), null, null);
        brightnessBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, brightnessPreview.getPreview(), null, null);
        contrastBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, contrastPreview.getPreview(), null, null);
        improveBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, improvePreview.getPreview(), null, null);
        tintBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, tintPreview.getPreview(), null, null);
        blurBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(null, blurPreview.getPreview(), null, null);





        toGrayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(TextView tv : buttons) {
                    tv.setTextColor(Color.parseColor("#C5C5C5"));
                }
                toGrayBtn.setTextColor(Color.WHITE);
                resetImage(image);
                FiltersRS.toGrayRS(image, MainActivity.this);
                updateImage(image);
            }
        });

        brightnessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(TextView tv : buttons) {
                    tv.setTextColor(Color.parseColor("#C5C5C5"));
                }
                brightnessBtn.setTextColor(Color.WHITE);
                resetImage(image);
                FiltersRS.brightnessRS(image, MainActivity.this, 0.001f);
                updateImage(image);
            }
        });



        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImage(base_image);
                for(TextView tv : buttons) {
                    tv.setTextColor(Color.parseColor("#C5C5C5"));
                }
            }
        });


    }

    void updateImage(Bitmap bmp) {
        photoView.setImageBitmap(bmp);
    }
    Bitmap getImage() {
        return image;
    }
    void resetImage(Bitmap bmp) {
        image = base_image;
    }



}

