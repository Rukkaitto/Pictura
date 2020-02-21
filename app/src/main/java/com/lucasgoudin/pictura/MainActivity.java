package com.lucasgoudin.pictura;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Bitmap image, base_image;
    ImageButton saveBtn, loadBtn, cameraBtn, resetBtn;
    PhotoView photoView;
    ArrayList<Filter> filters;
    Filter selectedFilter;
    SeekBar seekBar;
    String currentPhotoPath;

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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.lucasgoudin.pictura.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 2);
            }
        }
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

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
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
        try {
            if (selectedFilter.getSeekBarMin() < 0) {
                seekBar.setProgress(seekBar.getMax() / 2);
            } else {
                seekBar.setProgress(0);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
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
        Filter brightness = new Filter(brightnessBtn, new FilterPreview(image, new FilterRS(FilterName.BRIGHTNESS),this), -0.005f, 0.005f);
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

    private Bitmap fixOrientation(Bitmap bmp, String filename) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = 0;
        if(exif != null) {
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        }

        Matrix matrix = new Matrix();

        switch (orientation) {
            case 2:
                matrix.setScale(-1, 1);
                break;
            case 3:
                matrix.setRotate(180);
                break;
            case 4:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case 5:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case 6:
                matrix.setRotate(90);
                break;
            case 7:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case 8:
                matrix.setRotate(-90);
                break;
            default:
                break;
        }

        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2 && resultCode == RESULT_OK){

            Bitmap rotatedBitmap = fixOrientation(BitmapFactory.decodeFile(currentPhotoPath), currentPhotoPath);
            this.image = rotatedBitmap.copy(rotatedBitmap.getConfig(), true);
            this.base_image = image.copy(image.getConfig(), true);
            updateImage();
            updatePreviews();
        }

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null ) {

            Uri imageUri = data.getData();
            String filename = data.getDataString();

            try {
                Bitmap rotatedBitmap = fixOrientation(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri), filename);
                this.image = rotatedBitmap.copy(rotatedBitmap.getConfig(), true);
                this.base_image = image.copy(image.getConfig(), true);
                updateImage();
                updatePreviews();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}

