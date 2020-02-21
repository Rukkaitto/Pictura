package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.widget.TextView;

/**
 * A class that manages everything related to the filters
 */
public class Filter {

    private TextView filterBtn;
    private FilterPreview filterPreview;
    private FilterRS filterRS;

    private float seekBarValue;
    private float seekBarMin;
    private float seekBarMax;
    private boolean hasSeekBar;

    /**
     * Creates a filter with no seekbar
     * @param filterBtn the filter's button
     * @param filterPreview the filter's preview
     */
    public Filter(TextView filterBtn, FilterPreview filterPreview) {
        this.filterBtn = filterBtn;
        this.filterPreview = filterPreview;
        this.filterRS = filterPreview.getFilterRS();
        this.hasSeekBar = false;
    }

    /**
     * Creates a filter with a seekbar
     * @param filterBtn the filter's button
     * @param filterPreview the filter's preview
     * @param seekBarMin the minimum value of the seekbar
     * @param seekBarMax the maximum value of the seekbar
     */
    public Filter(TextView filterBtn, FilterPreview filterPreview, float seekBarMin, float seekBarMax) {
        this.filterBtn = filterBtn;
        this.filterPreview = filterPreview;
        this.filterRS = filterPreview.getFilterRS();
        this.hasSeekBar = true;
        this.seekBarMin = seekBarMin;
        this.seekBarMax = seekBarMax;
    }

    /**
     * Applies the filter to the image
     * @param image the bitmap to apply the filter to
     */
    public void apply(Bitmap image) {
        filterRS.apply(image, seekBarValue);
    }

    /**
     * @return the filter's button
     */
    public TextView getFilterBtn() {
        return filterBtn;
    }

    /**
     * @return the filter's preview
     */
    public FilterPreview getFilterPreview() {
        return filterPreview;
    }

    /**
     * @param seekBarValue the value to set the seekbar to
     */
    public void setSeekBarValue(float seekBarValue) {
        this.seekBarValue = seekBarValue;
    }

    /**
     * @return the seekbar's minimum value
     */
    public float getSeekBarMin() {
        return seekBarMin;
    }

    /**
     * @return the seekbar's maximum value
     */
    public float getSeekBarMax() {
        return seekBarMax;
    }

    /**
     * @return true if the filter has a seekbar ; false otherwise
     */
    public boolean hasSeekbar() {
        return hasSeekBar;
    }
}
