package com.lucasgoudin.pictura;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.lucasgoudin.pictura.Filter.Filter;
import com.lucasgoudin.pictura.Filter.FilterName;
import com.lucasgoudin.pictura.Filter.FilterRS;
import com.lucasgoudin.pictura.Filter.FilterPreview;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Bitmap image, base_image;
    ImageButton saveBtn, loadBtn, cameraBtn, resetBtn;
    PhotoView photoView;
    ArrayList<Filter> filters;
    Filter selectedFilter;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializePhotoView();
        initializeButtons();
        initializeSeekBar();

        makeFilters();
        updatePreviews();

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    private void updateImage() {
        photoView.setImageBitmap(image);
    }

    private void resetImage() {
        image = base_image.copy(base_image.getConfig(), true);
        updateImage();
    }

    private void updatePreviews() {
        for(Filter f : filters) {
            f.getFilterPreview().update(image);
            f.getFilterBtn().setCompoundDrawablesRelativeWithIntrinsicBounds(null, f.getFilterPreview().getPreview(), null, null);
        }
    }

    private void initializePhotoView() {
        photoView = findViewById(R.id.photo_view);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        base_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        updateImage();
    }

    private void initializeButtons() {
        saveBtn = findViewById(R.id.saveBtn);
        loadBtn = findViewById(R.id.loadBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        resetBtn = findViewById(R.id.resetBtn);

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSeekBar();
                resetImage();
                seekBar.setVisibility(View.INVISIBLE);
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

    private float map(float x, float minA, float maxA, float minB, float maxB) {
        return ((x - minA) / maxA) * (maxB - minB) + minB;
    }

    private void initializeSeekBar() {
        seekBar = findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = map((float) progress, 0f, seekBar.getMax(),selectedFilter.getSeekBarMin(), selectedFilter.getSeekBarMax());
                selectedFilter.setSeekBarValue(value);
                resetImage();
                selectedFilter.apply(image, MainActivity.this);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void resetSeekBar() {
        if(selectedFilter.getSeekBarMin() < 0) {
            seekBar.setProgress(seekBar.getMax() / 2);
        } else {
            seekBar.setProgress(0);
        }
    }

    private void makeFilters() {
        // Filter buttons
        TextView toGrayBtn = findViewById(R.id.toGrayBtn);
        TextView brightnessBtn = findViewById(R.id.brightnessBtn);
        TextView contrastBtn = findViewById(R.id.contrastBtn);
        TextView improveBtn = findViewById(R.id.improveBtn);
        TextView tintBtn = findViewById(R.id.tintBtn);
        TextView isolateBtn = findViewById(R.id.isolateBtn);
        TextView blurBtn = findViewById(R.id.blurBtn);

        // Filters
        Filter toGray = new Filter(toGrayBtn, new FilterPreview(image, new FilterRS(FilterName.TOGRAY), this));
        Filter brightness = new Filter(brightnessBtn, new FilterPreview(image, new FilterRS(FilterName.BRIGHTNESS),this), -0.001f, 0.001f);
        Filter contrast = new Filter(contrastBtn, new FilterPreview(image, new FilterRS(FilterName.CONTRAST),this));
        Filter improve = new Filter(improveBtn, new FilterPreview(image, new FilterRS(FilterName.IMPROVE),this));
        Filter tint = new Filter(tintBtn, new FilterPreview(image, new FilterRS(FilterName.TINT),this), 0, 359);
        Filter isolate = new Filter(isolateBtn, new FilterPreview(image, new FilterRS(FilterName.ISOLATE),this), 0, 359);
        Filter blur = new Filter(blurBtn, new FilterPreview(image, new FilterRS(FilterName.BLUR),this), 0, 1);

        filters = new ArrayList<>();
        filters.add(toGray);
        filters.add(brightness);
        filters.add(contrast);
        filters.add(improve);
        filters.add(isolate);
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
                    selectedFilter = filter;
                    if(filter.hasSeekbar()) {
                        seekBar.setVisibility(View.VISIBLE);
                        resetSeekBar();
                    } else {
                        seekBar.setVisibility(View.INVISIBLE);
                    }
                    filterBtn.setTextColor(Color.WHITE);
                    filter.apply(image, MainActivity.this);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_FIRST_USER && resultCode == RESULT_OK && data != null && data.getData() != null) {
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

