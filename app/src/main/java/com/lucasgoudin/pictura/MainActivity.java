package com.lucasgoudin.pictura;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.icu.util.Output;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.lucasgoudin.pictura.Filter.Filter;
import com.lucasgoudin.pictura.Filter.FilterName;
import com.lucasgoudin.pictura.Filter.FilterRS;
import com.lucasgoudin.pictura.Filter.FilterPreview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * The main Activity
 */
public class MainActivity extends AppCompatActivity {

    Bitmap image, base_image, full_image;
    ImageButton saveBtn, shareBtn, loadBtn, cameraBtn, resetBtn;
    PhotoView photoView;
    HorizontalScrollView filterScrollView;
    ArrayList<Filter> filters;
    Filter selectedFilter;
    SeekBar seekBar;
    String currentPhotoPath;
    TextView noPhotoMessage;

    int OPEN_GALLERY = 1;
    int OPEN_CAMERA = 2;
    int SAVE_IMAGE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializePhotoView();
        initializeButtons();
        initializeSeekBar();

        makeFilters();
        updatePreviews();

        filterScrollView = findViewById(R.id.filters);
        noPhotoMessage = findViewById(R.id.noPhotoMessage);
        filterScrollView.setVisibility(View.INVISIBLE);
        photoView.setVisibility(View.INVISIBLE);

    }

    /**
     * Creates a File reference to where the image file for the photo is saved (taken from the camera)
     * @return a File
     * @throws IOException if File.createTempFile fails
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
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

    /**
     * Launches the camera app and creates an intent
     */
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
                startActivityForResult(takePictureIntent, OPEN_CAMERA);
            }
        }
    }

    /**
     * Launches the gallery and creates an intent
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, OPEN_GALLERY);
    }

    private void save() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SAVE_IMAGE);
    }

    /**
     * Launches the Android Sharesheet intent
     */
    private void share() {
        // Creates a file for the picture
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(photoFile != null) {
            // Converts the full resolution bitmap to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            selectedFilter.apply(full_image);
            // JPEG compression
            full_image.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            byte[] bitmapData = bos.toByteArray();

            try {
                // Writes the bytes to the file
                FileOutputStream fos = new FileOutputStream(photoFile);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Converts the File to a Uri
            Uri uri = FileProvider.getUriForFile(this, "com.lucasgoudin.pictura.fileprovider", photoFile);

            // Creates the intent
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/jpeg");

            // Starts the Sharesheet
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share)));
        }
    }

    /**
     * Updates the PhotoView content to be the current image bitmap
     */
    private void updateImage() {
        photoView.setImageBitmap(image);
    }

    /**
     * Replaces the image with the base image to reset it
     */
    private void resetImage() {
        image = base_image.copy(base_image.getConfig(), true);
        updateImage();
    }

    /**
     * Generates the previews and sets it to their respective compound drawables
     */
    private void updatePreviews() {
        // For each filter, makeRoundedPreview the preview
        for(Filter f : filters) {
            f.getFilterPreview().makeRoundedPreview(image);
            f.getFilterBtn().setCompoundDrawablesRelativeWithIntrinsicBounds(null, f.getFilterPreview().getPreview(), null, null);
        }
    }

    /**
     * Initializes the PhotoView with a default image
     */
    private void initializePhotoView() {
        // Creates the photoview and sets the default image
        photoView = findViewById(R.id.photo_view);
        image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        base_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        full_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
        updateImage();
    }

    /**
     * Sets the buttons' on click functionality (save, load, and reset)
     */
    private void initializeButtons() {
        saveBtn = findViewById(R.id.saveBtn);
        shareBtn = findViewById(R.id.shareBtn);
        loadBtn = findViewById(R.id.loadBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        resetBtn = findViewById(R.id.resetBtn);

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFilter != null) {
                    share();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.shareNoFilter, Toast.LENGTH_LONG).show();
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Resets the seekbar to its initial value
                resetSeekBar();
                // Resets the image
                resetImage();
                // Hides the seekbar
                seekBar.setVisibility(View.INVISIBLE);
                // For each filter, resets the text to its default color
                for(Filter f : filters) {
                    f.getFilterBtn().setTextColor(Color.parseColor("#C5C5C5"));
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedFilter != null) {
                    save();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.saveNoFilter, Toast.LENGTH_LONG).show();
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

    /**
     * Takes a value in an interval and interpolates it to another interval ; useful for the seekbar logic
     * @param x the value to interpolate
     * @param minA the minimum of the first interval
     * @param maxA the maximum of the first interval
     * @param minB the minimum of the second interval
     * @param maxB the maximum of the second interval
     * @return the interpolated value
     */
    private float map(float x, float minA, float maxA, float minB, float maxB) {
        return ((x - minA) / maxA) * (maxB - minB) + minB;
    }

    /**
     * Initializes the seekbar
     */
    private void initializeSeekBar() {
        seekBar = findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // The value of the seekbar, which is between 0 and 100, is mapped to the minimum and the maximum values of the selected filter
                float value = map((float) progress, 0f, seekBar.getMax(),selectedFilter.getSeekBarMin(), selectedFilter.getSeekBarMax());
                selectedFilter.setSeekBarValue(value);
                resetImage();
                selectedFilter.apply(image);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Resets the seekBar to its default value
     */
    private void resetSeekBar() {
        // Ensures the app doesn't crash when no photo is chosen
        if(selectedFilter != null) {
            // If the seekbar allows negative values, it is reset to half of the bar, otherwise it is reset to zero
            if (selectedFilter.getSeekBarMin() < 0) {
                seekBar.setProgress(seekBar.getMax() / 2);
            } else {
                seekBar.setProgress((int)selectedFilter.getSeekBarMin());
            }
        }
    }

    /**
     * Creates the filters, with their invividual seekbar limits and their associated buttons
     */
    private void makeFilters() {
        // Filter buttons
        TextView toGrayBtn = findViewById(R.id.toGrayBtn);
        TextView brightnessBtn = findViewById(R.id.brightnessBtn);
        TextView contrastBtn = findViewById(R.id.contrastBtn);
        TextView improveBtn = findViewById(R.id.improveBtn);
        TextView tintBtn = findViewById(R.id.tintBtn);
        TextView isolateBtn = findViewById(R.id.isolateBtn);
        TextView blurBtn = findViewById(R.id.blurBtn);
        TextView laplaceBtn = findViewById(R.id.laplaceBtn);
        TextView sobelBtn = findViewById(R.id.sobelBtn);
        TextView averageBtn = findViewById(R.id.averageBtn);

        // Filters
        Filter toGray = new Filter(toGrayBtn, new FilterPreview(image, new FilterRS(FilterName.TOGRAY, this)));
        Filter brightness = new Filter(brightnessBtn, new FilterPreview(image, new FilterRS(FilterName.BRIGHTNESS, this)), -255.f, 254.f);
        Filter contrast = new Filter(contrastBtn, new FilterPreview(image, new FilterRS(FilterName.CONTRAST, this)), -100.f, 100.f);
        Filter improve = new Filter(improveBtn, new FilterPreview(image, new FilterRS(FilterName.IMPROVE, this)));
        Filter tint = new Filter(tintBtn, new FilterPreview(image, new FilterRS(FilterName.TINT, this)), 0, 359);
        Filter isolate = new Filter(isolateBtn, new FilterPreview(image, new FilterRS(FilterName.ISOLATE, this)), 0, 359);
        Filter blur = new Filter(blurBtn, new FilterPreview(image, new FilterRS(FilterName.BLUR, this)), 5, 8);
        Filter laplace = new Filter(laplaceBtn, new FilterPreview(image, new FilterRS(FilterName.LAPLACE, this)));
        Filter sobel = new Filter(sobelBtn, new FilterPreview(image, new FilterRS(FilterName.SOBEL, this)));
        Filter average = new Filter(averageBtn, new FilterPreview(image, new FilterRS(FilterName.AVERAGE, this)), 5, 8);

        // Adds all the filters to the list
        filters = new ArrayList<>();
        filters.add(toGray);
        filters.add(brightness);
        filters.add(contrast);
        filters.add(improve);
        filters.add(isolate);
        filters.add(tint);
        filters.add(blur);
        filters.add(laplace);
        filters.add(sobel);
        filters.add(average);

        for(final Filter filter : filters) {
            final TextView filterBtn = filter.getFilterBtn();
            // Sets the filter buttons's functionality
            filterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Resets the title colors
                    for(Filter f1 : filters) {
                        f1.getFilterBtn().setTextColor(Color.parseColor("#C5C5C5"));
                    }
                    resetImage();
                    selectedFilter = filter;
                    // If the filter has a seekbar, make it visible and reset it, otherwise hide it
                    if(filter.hasSeekbar()) {
                        seekBar.setVisibility(View.VISIBLE);
                        resetSeekBar();
                    } else {
                        seekBar.setVisibility(View.INVISIBLE);
                    }
                    // Highlights the filter's title text
                    filterBtn.setTextColor(Color.WHITE);
                    // Apply the filter to the image
                    filter.apply(image);
                }
            });
        }
    }

    /**
     * Gets the exif data from the bitmap and rotates the image with a matrix depending on the photo's orientation
     * @param bmp the bitmap to retreive the exif data from
     * @param stream the stream corresponding to the photo's file
     * @return the rotated bitmap
     */
    private Bitmap fixOrientation(Bitmap bmp, InputStream stream) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = 0;
        if(exif != null) {
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        }

        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                break;
        }

        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    /**
     * Downscales the bitmap to save resources
     * @param bmp the bitmap to downscale
     * @return the downscaled bitmap
     */
    private Bitmap resizeBitmap(Bitmap bmp) {
        // Gets the dimensions of the PhotoView
        float targetW = photoView.getWidth();
        float targetH = photoView.getHeight();

        float photoW = bmp.getWidth();
        float photoH = bmp.getHeight();


        float scaleFactor = Math.min(photoW/targetW, photoH/targetH) * 2;

        // Returns the downscaled bitmap
        return Bitmap.createScaledBitmap(bmp, (int)(photoW / scaleFactor), (int)(photoH / scaleFactor), true);
    }

    /**
     * Processes the picture, taken either from the gallery or the camera and sets it on the PhotoView
     * @param imageUri the Uri of the picture
     * @param bmp the bitmap of the picture
     */
    private void handlePhoto(Uri imageUri, Bitmap bmp) {
        InputStream stream = null;
        try {
            stream = getContentResolver().openInputStream(imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap rotatedBitmap = fixOrientation(bmp, stream);
        Bitmap resizedBitmap = resizeBitmap(rotatedBitmap);
        this.image = resizedBitmap.copy(rotatedBitmap.getConfig(), true);
        this.base_image = image.copy(image.getConfig(), true);
        this.full_image = rotatedBitmap.copy(rotatedBitmap.getConfig(), true);
        updateImage();
        updatePreviews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == OPEN_CAMERA && resultCode == RESULT_OK){
            handlePhoto(Uri.fromFile(new File(currentPhotoPath)), BitmapFactory.decodeFile(currentPhotoPath));
        }

        if(requestCode == OPEN_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null ) {
            try {
                handlePhoto(data.getData(), MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if((requestCode == OPEN_GALLERY || requestCode == OPEN_CAMERA) && resultCode == RESULT_OK) {
            filterScrollView.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.VISIBLE);
            noPhotoMessage.setVisibility(View.INVISIBLE);
        }
    }

    private ContentValues contentValues()  {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        return values;
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImage(Bitmap bitmap, String folderName) {
        if(Build.VERSION.SDK_INT >= 29) {
            ContentValues values = contentValues();
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + "Pictura");
            values.put(MediaStore.Images.Media.IS_PENDING, true);

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if(uri != null) {
                try {
                    saveImageToStream(bitmap, getContentResolver().openOutputStream(uri));
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    getContentResolver().update(uri, values, null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File directory = new File(Environment.getExternalStorageDirectory().toString() + File.separator + folderName);
            if(!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);
            try {
                saveImageToStream(bitmap, new FileOutputStream(file));
                ContentValues values = contentValues();
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == SAVE_IMAGE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Bitmap full_image_result = full_image.copy(full_image.getConfig(), true);
                selectedFilter.apply(full_image_result);
                saveImage(full_image_result, getResources().getString(R.string.app_name));
                Toast.makeText(getApplicationContext(), R.string.savedMessage, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.noPermissions, Toast.LENGTH_LONG).show();
            }
        }
    }
}

