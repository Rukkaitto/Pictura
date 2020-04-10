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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.mtp.MtpConstants;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

/**
 * The main Activity
 */
public class MainActivity extends AppCompatActivity {

    Bitmap image, base_image, full_image;
    ImageButton saveBtn, shareBtn, loadBtn, cameraBtn, resetBtn;
    PhotoView photoView;
    HorizontalScrollView scrollView;
    ArrayList<Filter> filters;
    Filter selectedFilter;
    SeekBar seekBar;
    String currentPhotoPath;
    TextView noPhotoMessage;
    LinearLayout tabsLayout;

    ArrayList<Canvas> canvasTab = new ArrayList<>();

    LinearLayout filtersTabContent, stickersTabContent, textTabContent, brushesTabContent;
    ArrayList<LinearLayout> tabsContent;

    enum tabId {
        FILTERS, STICKERS, TEXT, BRUSHES
    }
    Button filtersTab, stickersTab, textTab, brushTab;
    ArrayList<Button> tabs;

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

        scrollView = findViewById(R.id.filters);
        tabsLayout = findViewById(R.id.tabs);
        tabsLayout.setVisibility(View.INVISIBLE);
        noPhotoMessage = findViewById(R.id.noPhotoMessage);
        scrollView.setVisibility(View.INVISIBLE);
        photoView.setVisibility(View.INVISIBLE);

        initializeTabs();
        makeFilters();
        makeStickers();
        makeFrames();
        makeBrushes();
        updatePreviews();

    }

    private void initializeTabs() {
        tabsContent = new ArrayList<LinearLayout>();

        filtersTab = findViewById(R.id.filtersTab);
        setListener(filtersTab, tabId.FILTERS);
        stickersTab = findViewById(R.id.stickersTab);
        setListener(stickersTab, tabId.STICKERS);
        textTab = findViewById(R.id.textTab);
        setListener(textTab, tabId.TEXT);
        brushTab = findViewById(R.id.brushTab);
        setListener(brushTab, tabId.BRUSHES);

        tabs = new ArrayList<Button>();
        tabs.add(filtersTab);
        tabs.add(stickersTab);
        tabs.add(textTab);
        tabs.add(brushTab);

        ContextThemeWrapper layoutContext = new ContextThemeWrapper(this, R.style.button_layout);

        filtersTabContent = new LinearLayout(layoutContext);
        tabsContent.add(filtersTabContent);

        stickersTabContent = new LinearLayout(layoutContext);
        tabsContent.add(stickersTabContent);

        textTabContent = new LinearLayout(layoutContext);
        tabsContent.add(textTabContent);

        brushesTabContent = new LinearLayout(layoutContext);
        tabsContent.add(brushesTabContent);


        scrollView.addView(filtersTabContent);

        for(final Button otherTab : tabs) {
            otherTab.setTextColor(Color.parseColor("#C5C5C5"));
        }
        filtersTab.setTextColor(Color.WHITE);
    }

    private void setListener(final Button tab, final tabId id) {
        tab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(final Button otherTab : tabs) {
                    otherTab.setTextColor(Color.parseColor("#C5C5C5"));
                }
                tab.setTextColor(Color.WHITE);

                switch (id) {
                    case FILTERS:
                        scrollView.removeAllViews();
                        scrollView.addView(filtersTabContent);
                        break;
                    case STICKERS:
                        scrollView.removeAllViews();
                        scrollView.addView(stickersTabContent);
                        break;
                    case TEXT:
                        scrollView.removeAllViews();
                        scrollView.addView(textTabContent);
                        break;
                    case BRUSHES:
                        scrollView.removeAllViews();
                        scrollView.addView(brushesTabContent);
                        break;
                }
            }
        });
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
        Toast.makeText(getApplicationContext(), R.string.saving, Toast.LENGTH_LONG).show();
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
            if(selectedFilter != null) {
                selectedFilter.apply(full_image);
            }
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
                share();
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
                save();
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
        ContextThemeWrapper buttonContext = new ContextThemeWrapper(this, R.style.filterButtonStyle);

        TextView toGrayBtn = new TextView(buttonContext);
        toGrayBtn.setText(R.string.toGray);

        TextView brightnessBtn = new TextView(buttonContext);
        brightnessBtn.setText(R.string.brightness);

        TextView contrastBtn = new TextView(buttonContext);
        contrastBtn.setText(R.string.contrast);

        TextView improveBtn = new TextView(buttonContext);
        improveBtn.setText(R.string.improve);

        TextView tintBtn = new TextView(buttonContext);
        tintBtn.setText(R.string.tint);

        TextView isolateBtn = new TextView(buttonContext);
        isolateBtn.setText(R.string.isolate);

        TextView blurBtn = new TextView(buttonContext);
        blurBtn.setText(R.string.blur);

        TextView laplaceBtn = new TextView(buttonContext);
        laplaceBtn.setText(R.string.laplace);

        TextView sobelBtn = new TextView(buttonContext);
        sobelBtn.setText(R.string.sobel);

        TextView averageBtn = new TextView(buttonContext);
        averageBtn.setText(R.string.average);

        TextView drawingBtn = new TextView(buttonContext);
        drawingBtn.setText(R.string.drawing);

        TextView negativeBtn = new TextView(buttonContext);
        negativeBtn.setText(R.string.negative);


        filtersTabContent.addView(toGrayBtn);
        filtersTabContent.addView(brightnessBtn);
        filtersTabContent.addView(contrastBtn);
        filtersTabContent.addView(improveBtn);
        filtersTabContent.addView(tintBtn);
        filtersTabContent.addView(isolateBtn);
        filtersTabContent.addView(blurBtn);
        filtersTabContent.addView(laplaceBtn);
        filtersTabContent.addView(sobelBtn);
        filtersTabContent.addView(averageBtn);
        filtersTabContent.addView(drawingBtn);
        filtersTabContent.addView(negativeBtn);

        // Filters
        Filter toGray = new Filter(toGrayBtn, new FilterPreview(image, new FilterRS(FilterName.TOGRAY, this)));
        Filter brightness = new Filter(brightnessBtn, new FilterPreview(image, new FilterRS(FilterName.BRIGHTNESS, this)), -255.f, 254.f);
        Filter contrast = new Filter(contrastBtn, new FilterPreview(image, new FilterRS(FilterName.CONTRAST, this)), -100.f, 100.f);
        Filter improve = new Filter(improveBtn, new FilterPreview(image, new FilterRS(FilterName.IMPROVE, this)));
        Filter tint = new Filter(tintBtn, new FilterPreview(image, new FilterRS(FilterName.TINT, this)), 0, 359);
        Filter isolate = new Filter(isolateBtn, new FilterPreview(image, new FilterRS(FilterName.ISOLATE, this)), 0, 359);
        Filter blur = new Filter(blurBtn, new FilterPreview(image, new FilterRS(FilterName.BLUR, this)), 3, 11);
        Filter laplace = new Filter(laplaceBtn, new FilterPreview(image, new FilterRS(FilterName.LAPLACE, this)));
        Filter sobel = new Filter(sobelBtn, new FilterPreview(image, new FilterRS(FilterName.SOBEL, this)));
        Filter average = new Filter(averageBtn, new FilterPreview(image, new FilterRS(FilterName.AVERAGE, this)), 5, 8);
        Filter drawing = new Filter(drawingBtn, new FilterPreview(image, new FilterRS(FilterName.DRAWING, this)), 1.f, 10.f);
        Filter negative = new Filter(negativeBtn, new FilterPreview(image, new FilterRS(FilterName.NEGATIVE, this)));

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
        filters.add(drawing);
        filters.add(negative);

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

    private void makeStickers() {
        // Filter buttons
        ContextThemeWrapper buttonContext = new ContextThemeWrapper(this, R.style.filterButtonStyle);

        final TextView leafBtn = new TextView(buttonContext);
        leafBtn.setText("Feuille");

        final TextView cat1Btn = new TextView(buttonContext);
        cat1Btn.setText("Chat roux");

        TextView cat2Btn = new TextView(buttonContext);
        cat2Btn.setText("Chat gris");

        TextView cat3Btn = new TextView(buttonContext);
        cat3Btn.setText("Chatte grise");

        TextView ceriseBtn = new TextView(buttonContext);
        ceriseBtn.setText("Cerise");

        TextView clemenceauBtn = new TextView(buttonContext);
        clemenceauBtn.setText("Clemenceau");

        TextView cloudBtn = new TextView(buttonContext);
        cloudBtn.setText("Nuage");

        TextView crown1Btn = new TextView(buttonContext);
        crown1Btn.setText("Couronne");

        TextView crown2Btn = new TextView(buttonContext);
        crown2Btn.setText("Couronne grise");

        TextView donutBtn = new TextView(buttonContext);
        donutBtn.setText("Donut");

        TextView eggBtn = new TextView(buttonContext);
        eggBtn.setText("Oeuf");

        TextView fraiseBtn = new TextView(buttonContext);
        fraiseBtn.setText("Fraise");

        TextView heartBtn = new TextView(buttonContext);
        heartBtn.setText("Coeur");

        TextView bunnyBtn = new TextView(buttonContext);
        bunnyBtn.setText("Lapin");

        TextView meliBtn = new TextView(buttonContext);
        meliBtn.setText("Méli");

        TextView appleBtn = new TextView(buttonContext);
        appleBtn.setText("Pomme");

        TextView chickenBtn = new TextView(buttonContext);
        chickenBtn.setText("Poulet");

        TextView octopusBtn = new TextView(buttonContext);
        octopusBtn.setText("Poulpe");

        TextView sunBtn = new TextView(buttonContext);
        sunBtn.setText("Soleil");

        TextView sweetBtn = new TextView(buttonContext);
        sweetBtn.setText("Bonbon");

        TextView pieBtn = new TextView(buttonContext);
        pieBtn.setText("Tarte");


        stickersTabContent.addView(leafBtn);
        stickersTabContent.addView(cat1Btn);
        stickersTabContent.addView(cat2Btn);
        stickersTabContent.addView(cat3Btn);
        stickersTabContent.addView(ceriseBtn);
        stickersTabContent.addView(clemenceauBtn);
        stickersTabContent.addView(cloudBtn);
        stickersTabContent.addView(crown1Btn);
        stickersTabContent.addView(crown2Btn);
        stickersTabContent.addView(donutBtn);
        stickersTabContent.addView(eggBtn);
        stickersTabContent.addView(fraiseBtn);
        stickersTabContent.addView(heartBtn);
        stickersTabContent.addView(bunnyBtn);
        stickersTabContent.addView(meliBtn);
        stickersTabContent.addView(appleBtn);
        stickersTabContent.addView(chickenBtn);
        stickersTabContent.addView(octopusBtn);
        stickersTabContent.addView(sunBtn);
        stickersTabContent.addView(sweetBtn);
        stickersTabContent.addView(pieBtn);

        leafBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("leaf");
            }
        });

        cat1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("cat1");
            }
        });

        cat2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("cat2");
            }
        });

        cat3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("cat3");
            }
        });

        ceriseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("cerise");
            }
        });

        clemenceauBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("clemenceau");
            }
        });

        cloudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("cloud");
            }
        });

        crown1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("crown1");
            }
        });

        crown2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("crown2");
            }
        });

        donutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("donut");
            }
        });

        eggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("egg");
            }
        });

        fraiseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("fraise");
            }
        });

        heartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("heart");
            }
        });

        bunnyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("bunny");
            }
        });

        meliBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("meli");
            }
        });

        appleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("apple");
            }
        });

        chickenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("chicken");
            }
        });

        octopusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("octopus");
            }
        });

        sunBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("sun");
            }
        });

        sweetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("sweet");
            }
        });

        pieBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProcessingBitmap("pie");
            }
        });



    }

    private void ProcessingBitmap(String nameSticker) {
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
            y = ((int) (Math.random() * (sticker.getHeight())));
            x = ((sticker.getWidth() * y) / tmp);
        } else if(sticker.getWidth() > sticker.getHeight()){
            int tmp = sticker.getWidth();
            x = (int) (Math.random() * (sticker.getWidth()));
            y = ((x * sticker.getHeight()) / tmp);
        }

        sticker = Bitmap.createScaledBitmap(sticker,x,y,false);

        // Fusion of the image with the sticker
        Bitmap fusion = Bitmap.createBitmap(image.getWidth(), image.getHeight(), bitmapConfig);
        Bitmap full_fusion = Bitmap.createBitmap(full_image.getWidth(), full_image.getHeight(), bitmapConfig);

        Canvas canvas = new Canvas();
        canvas.setBitmap(fusion);
        canvas.drawBitmap(image, new Matrix(), null);
        canvas.drawBitmap(sticker, (int) (Math.random() * (image.getWidth() - sticker.getWidth())), (int)  (Math.random() * (image.getHeight() - sticker.getHeight())), null);

        Canvas canvas2 = new Canvas();
        canvas2.setBitmap(full_fusion);
        canvas2.drawBitmap(full_image, new Matrix(), null);
        canvas2.drawBitmap(sticker, (int) (Math.random() * (full_image.getWidth() - sticker.getWidth())), (int)  (Math.random() * (full_image.getHeight() - sticker.getHeight())), null);

        image = fusion;
        full_image = full_fusion;


        updateImage();

    }

    private Bitmap selectSticker(String sticker) {
        Bitmap stickerBmp = Bitmap.createBitmap(image.getWidth() ,image.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        switch(sticker){
            case "leaf" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.leaf);
                break;
            case "cat1" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.cat1);
                break;
            case "cat2" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.cat2);
                break;
            case "cat3" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.cat3);
                break;
            case "cerise" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.cerise);
                break;
            case "clemenceau" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.clemenceau);
                break;
            case "cloud" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.cloud);
                break;
            case "crown1" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.crown1);
                break;
            case "crown2" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.crown2);
                break;
            case "donut" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.donut);
                break;
            case "egg" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.egg);
                break;
            case "fraise" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.fraise);
                break;
            case "heart" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.heart);
                break;
            case "bunny" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.lapin);
                break;
            case "meli" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.meli);
                break;
            case "apple" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.pomme);
                break;
            case "chicken" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.poulet);
                break;
            case "octopus" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.poulpe);
                break;
            case "sun" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.soleil);
                break;
            case "sweet" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.sweet);
                break;
            case "pie" :
                stickerBmp =  BitmapFactory.decodeResource(this.getResources(), R.drawable.tarte);
                break;
            default :
                break;
        }
        return stickerBmp;
    }

    private void makeFrames() {
        // Filter buttons
        ContextThemeWrapper buttonContext = new ContextThemeWrapper(this, R.style.filterButtonStyle);

        TextView frame1Btn = new TextView(buttonContext);
        frame1Btn.setText("Cadre n°1");

        textTabContent.addView(frame1Btn);

        frame1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFrame("frame1");
            }
        });

    }

    private void setFrame(String nameFrame) {
        int x = 0, y = 0;

        // Make the image mutable
        android.graphics.Bitmap.Config bitmapConfig = image.getConfig();
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        image = image.copy(bitmapConfig, true);

        // Make the frame mutable
        Bitmap frame = selectFrame(nameFrame);

        android.graphics.Bitmap.Config bitmapConfigSticker = frame.getConfig();
        if(bitmapConfigSticker == null) {
            bitmapConfigSticker = android.graphics.Bitmap.Config.ARGB_8888;
        }
        frame = frame.copy(bitmapConfigSticker, true);

        frame = Bitmap.createScaledBitmap(frame,image.getWidth(),image.getHeight(),false);

        // Fusion of the image with the sticker
        Bitmap fusion = Bitmap.createBitmap(image.getWidth(), image.getHeight(), bitmapConfig);

        Canvas canvas = new Canvas();
        canvas.setBitmap(fusion);
        canvas.drawBitmap(image, new Matrix(), null);
        canvas.drawBitmap(frame, new Matrix(), null);
        image = fusion;

        updateImage();
    }

    private Bitmap selectFrame(String frame){
        Bitmap frameBmp = Bitmap.createBitmap(image.getWidth() ,image.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        switch(frame){
            case "frame1" :
                frameBmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.frame1);
                break;
            default :
                break;
        }
        return frameBmp;
    }

    private void makeBrushes() {
        // TODO : makeBrushes
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
            scrollView.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.VISIBLE);
            tabsLayout.setVisibility(View.VISIBLE);
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
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folderName);
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
                if(selectedFilter != null) {
                    selectedFilter.apply(full_image_result);
                }
                saveImage(full_image_result, getResources().getString(R.string.app_name));
                Toast.makeText(getApplicationContext(), R.string.savedMessage, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.noPermissions, Toast.LENGTH_LONG).show();
            }
        }
    }
}

