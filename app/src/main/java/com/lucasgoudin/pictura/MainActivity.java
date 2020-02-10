package com.lucasgoudin.pictura;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Bitmap image, base_image;
    ImageButton saveBtn, loadBtn, cameraBtn, resetBtn;
    PhotoView photoView;
    ArrayList<Filter> filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializePhotoView();
        initializeButtons();
        makeFilters();

        updatePreviews();

    }

    void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    void updateImage() {
        photoView.setImageBitmap(image);
    }

    void resetImage() {
        image = base_image.copy(base_image.getConfig(), true);
        updateImage();
    }

    void updatePreviews() {
        for(Filter f : filters) {
            f.getFilterPreview().update(image);
            f.getFilterBtn().setCompoundDrawablesRelativeWithIntrinsicBounds(null, f.getFilterPreview().getPreview(), null, null);
        }
    }

    void initializePhotoView() {
        photoView = findViewById(R.id.photo_view);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        base_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        updateImage();
    }

    void initializeButtons() {
        saveBtn = findViewById(R.id.saveBtn);
        loadBtn = findViewById(R.id.loadBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        resetBtn = findViewById(R.id.resetBtn);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetImage();
                for(Filter f : filters) {
                    f.getFilterBtn().setTextColor(Color.parseColor("#C5C5C5"));
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

    void makeFilters() {
        // Filter buttons
        TextView toGrayBtn = findViewById(R.id.toGrayBtn);
        TextView brightnessBtn = findViewById(R.id.brightnessBtn);
        TextView contrastBtn = findViewById(R.id.contrastBtn);
        TextView improveBtn = findViewById(R.id.improveBtn);
        TextView tintBtn = findViewById(R.id.tintBtn);
        TextView blurBtn = findViewById(R.id.blurBtn);

        // Filters
        Filter toGray = new Filter(toGrayBtn, new Preview(image, new FilterRS(FilterName.TOGRAY), this));
        Filter brightness = new Filter(brightnessBtn, new Preview(image, new FilterRS(FilterName.BRIGHTNESS),this));
        Filter contrast = new Filter(contrastBtn, new Preview(image, new FilterRS(FilterName.CONTRAST),this));
        Filter improve = new Filter(improveBtn, new Preview(image, new FilterRS(FilterName.IMPROVE),this));
        Filter tint = new Filter(tintBtn, new Preview(image, new FilterRS(FilterName.TINT),this));
        Filter blur = new Filter(blurBtn, new Preview(image, new FilterRS(FilterName.BLUR),this));

        filters = new ArrayList<>();
        filters.add(toGray);
        filters.add(brightness);
        filters.add(contrast);
        filters.add(improve);
        filters.add(tint);
        filters.add(blur);

        for(final Filter filter : filters) {
            final TextView filterBtn = filter.getFilterBtn();
            filterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(Filter f1 : filters) {
                        f1.getFilterBtn().setTextColor(Color.parseColor("#C5C5C5"));
                    }
                    resetImage();
                    filterBtn.setTextColor(Color.WHITE);
                    filter.apply(image, MainActivity.this);
                }
            });
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

