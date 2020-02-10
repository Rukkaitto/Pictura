package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;

import com.lucasgoudin.pictura.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

public class FilterPreview {
    private Bitmap bmp;
    private RoundedBitmapDrawable bmp_d;
    private AppCompatActivity context;
    private FilterRS filterRS;

    public FilterPreview(Bitmap bmp, FilterRS filterRS, AppCompatActivity context) {
        this.context = context;
        this.filterRS = filterRS;

        update(bmp);
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

        float density = context.getResources().getDisplayMetrics().density;
        return Bitmap.createScaledBitmap(result, (int) (Settings.PREVIEW_SIZE * density), (int) (Settings.PREVIEW_SIZE * density), true);
    }

    private void applyFilter() {
        filterRS.apply(bmp, context);
    }

    public void update(Bitmap image) {
        this.bmp = scaleBitmap(image);
        applyFilter();
        bmp_d = RoundedBitmapDrawableFactory.create(context.getResources(), this.bmp);
        bmp_d.setCornerRadius(Settings.CORNER_RADIUS);
    }

    public RoundedBitmapDrawable getPreview() {
        return bmp_d;
    }

    FilterRS getFilterRS() {
        return filterRS;
    }

}
