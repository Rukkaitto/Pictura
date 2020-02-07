package com.lucasgoudin.pictura;

import android.graphics.Bitmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class Preview  {
    private Bitmap bmp;
    private RoundedBitmapDrawable bmp_d;
    private Filter filter;
    private AppCompatActivity context;

    public Preview(Bitmap bmp, Filter filter, AppCompatActivity context) {
        this.bmp = scaleBitmap(bmp);
        this.filter = filter;
        this.context = context;

        applyFilter();

        bmp_d = RoundedBitmapDrawableFactory.create(context.getResources(), this.bmp);
        bmp_d.setCornerRadius(Settings.CORNER_RADIUS);
    }

    private Bitmap scaleBitmap(Bitmap image) {
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

    private void applyFilter() {
        switch(this.filter) {
            case TOGRAY:
                FiltersRS.toGrayRS(bmp, context);
                break;
            case BRIGHTNESS:
                FiltersRS.brightnessRS(bmp, context, 0.001f);
                break;
        }
    }

    public RoundedBitmapDrawable getPreview() {
        return bmp_d;
    }

}
