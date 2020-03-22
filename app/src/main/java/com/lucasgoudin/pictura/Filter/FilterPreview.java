package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;

import com.lucasgoudin.pictura.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

/**
 * A class for the filters' previews
 */
public class FilterPreview {
    private Bitmap bmp;
    private RoundedBitmapDrawable bmp_d;
    private FilterRS filterRS;
    private AppCompatActivity context;

    /**
     * Creates a preview and associates its Renderscript filter
     * @param bmp the bitmap that will be used to generate the preview
     * @param filterRS a Renderscript filter
     */
    public FilterPreview(Bitmap bmp, FilterRS filterRS) {
        this.filterRS = filterRS;
        this.context = filterRS.getContext();

        makeRoundedPreview(bmp);
    }

    /**
     * Scales the bitmap to the preview's dimensions
     * @param image the image to scale
     * @return the scaled bitmap
     */
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

    /**
     * Applies the Renderscript filter to the preview image
     */
    private void applyFilter() {
        filterRS.apply(bmp,0, true);
    }

    /**
     * Creates a rounded version of the preview and applies the filter to it
     * @param image the original square preview
     */
    public void makeRoundedPreview(Bitmap image) {
        this.bmp = scaleBitmap(image);
        applyFilter();
        bmp_d = RoundedBitmapDrawableFactory.create(context.getResources(), this.bmp);
        int radius = 360;
        bmp_d.setCornerRadius(radius);
    }

    /**
     * @return the rounded and filtered preview
     */
    public RoundedBitmapDrawable getPreview() {
        return bmp_d;
    }

    /**
     * @return the Renderscript filter
     */
    FilterRS getFilterRS() {
        return filterRS;
    }

}
