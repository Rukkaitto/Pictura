package com.lucasgoudin.pictura;

import android.graphics.Bitmap;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Filter {

    private TextView filterBtn;
    private Preview filterPreview;
    private FilterRS filterRS;

    public Filter(TextView filterBtn, Preview filterPreview) {
        this.filterBtn = filterBtn;
        this.filterPreview = filterPreview;
        this.filterRS = filterPreview.getFilterRS();
    }

    void apply(Bitmap image, AppCompatActivity context) {
        filterRS.apply(image, context);
    }

    TextView getFilterBtn() {
        return filterBtn;
    }

    Preview getFilterPreview() {
        return filterPreview;
    }
}
