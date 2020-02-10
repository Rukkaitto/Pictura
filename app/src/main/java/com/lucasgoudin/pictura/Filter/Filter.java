package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Filter {

    private TextView filterBtn;
    private FilterPreview filterPreview;
    private FilterRS filterRS;


    public Filter(TextView filterBtn, FilterPreview filterPreview) {
        this.filterBtn = filterBtn;
        this.filterPreview = filterPreview;
        this.filterRS = filterPreview.getFilterRS();
    }

    public void apply(Bitmap image, AppCompatActivity context) {
        filterRS.apply(image, context);
    }

    public TextView getFilterBtn() {
        return filterBtn;
    }

    public FilterPreview getFilterPreview() {
        return filterPreview;
    }
}
