package com.lucasgoudin.pictura;

import android.graphics.Bitmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

class Preview  {
    private Bitmap bmp;
    private RoundedBitmapDrawable bmp_d;
    private AppCompatActivity context;
    private FilterRS filterRS;

    Preview(Bitmap bmp, FilterRS filterRS, AppCompatActivity context) {
        this.bmp = scaleBitmap(bmp);
        this.context = context;
        this.filterRS = filterRS;

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
        filterRS.apply(bmp, context);
    }

    void update(Bitmap image) {
        this.bmp = scaleBitmap(image);
        applyFilter();
        bmp_d = RoundedBitmapDrawableFactory.create(context.getResources(), this.bmp);
        bmp_d.setCornerRadius(Settings.CORNER_RADIUS);
    }

    RoundedBitmapDrawable getPreview() {
        return bmp_d;
    }

    FilterRS getFilterRS() {
        return filterRS;
    }

}
