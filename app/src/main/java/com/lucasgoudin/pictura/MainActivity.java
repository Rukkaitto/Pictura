package com.lucasgoudin.pictura;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


import com.github.chrisbanes.photoview.PhotoView;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Bitmap image;
    Bitmap base_image;
    PhotoView photoView;
    ArrayList<Preview> previews;
    ArrayList<TextView> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Image
        photoView = findViewById(R.id.photo_view);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        base_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        updateImage();


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

        buttons = new ArrayList<>();
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

        previews = new ArrayList<>();
        previews.add(toGrayPreview);
        previews.add(brightnessPreview);
        previews.add(contrastPreview);
        previews.add(improvePreview);
        previews.add(tintPreview);
        previews.add(blurPreview);


        updatePreviews();



        toGrayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(TextView tv : buttons) {
                    tv.setTextColor(Color.parseColor("#C5C5C5"));
                }
                resetImage();
                toGrayBtn.setTextColor(Color.WHITE);
                FiltersRS.toGrayRS(image, MainActivity.this);
                updateImage();
            }
        });

        brightnessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(TextView tv : buttons) {
                    tv.setTextColor(Color.parseColor("#C5C5C5"));
                }
                resetImage();
                brightnessBtn.setTextColor(Color.WHITE);
                FiltersRS.brightnessRS(image, MainActivity.this, 0.001f);
                updateImage();
            }
        });



        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetImage();
                for(TextView tv : buttons) {
                    tv.setTextColor(Color.parseColor("#C5C5C5"));
                }
            }
        });


        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

    }

    void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    void updateImage() {
        photoView.setImageBitmap(image);
    }

    Bitmap getImage() {
        return image;
    }

    void resetImage() {
        image = base_image.copy(base_image.getConfig(), true);
        updateImage();
    }

    void updatePreviews() {
        int i = 0;
        for(Preview p : previews) {
            p.update(image);
        }
        for(TextView tv : buttons) {
            tv.setCompoundDrawablesRelativeWithIntrinsicBounds(null, previews.get(i).getPreview(), null, null);
            i++;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                this.image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                this.base_image = image.copy(image.getConfig(), true);
                updateImage();
                updatePreviews();
            } catch (Exception e) {
                System.out.println("File not found!");
            }
        }
    }

}

