package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Filter {

    private TextView filterBtn;
    private FilterPreview filterPreview;
    private FilterRS filterRS;

    private float seekBarValue;
    private float seekBarMin;
    private float seekBarMax;
    private boolean hasSeekBar;

    public Filter(TextView filterBtn, FilterPreview filterPreview) {
        this.filterBtn = filterBtn;
        this.filterPreview = filterPreview;
        this.filterRS = filterPreview.getFilterRS();
        this.hasSeekBar = false;
    }

    public Filter(TextView filterBtn, FilterPreview filterPreview, float seekBarMin, float seekBarMax) {
        this.filterBtn = filterBtn;
        this.filterPreview = filterPreview;
        this.filterRS = filterPreview.getFilterRS();
        this.hasSeekBar = true;
        this.seekBarMin = seekBarMin;
        this.seekBarMax = seekBarMax;
    }

    public void apply(Bitmap image, AppCompatActivity context) {
        filterRS.apply(image, context, seekBarValue);
    }

    public TextView getFilterBtn() {
        return filterBtn;
    }

    public FilterPreview getFilterPreview() {
        return filterPreview;
    }

    public void setSeekBarValue(float sliderValue) {
        this.seekBarValue = sliderValue;
    }

    public float getSeekBarValue() {
        return seekBarValue;
    }

    public float getSeekBarMin() {
        return seekBarMin;
    }

    public float getSeekBarMax() {
        return seekBarMax;
    }

    public boolean hasSeekbar() {
        return hasSeekBar;
    }
}
