package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;

import com.lucasgoudin.pictura.ScriptC_brightness;
import com.lucasgoudin.pictura.ScriptC_gray;
import com.lucasgoudin.pictura.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

public class FilterRS {
    private FilterName filterName;

    public FilterRS(FilterName filterName) {
        this.filterName = filterName;
    }

    void apply (Bitmap bmp, AppCompatActivity context) {
        RenderScript rs = RenderScript.create(context);

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        switch (filterName) {
            case TOGRAY:
                ScriptC_gray grayScript = new ScriptC_gray(rs);
                grayScript.forEach_toGray(input, output);
                grayScript.destroy();
                break;
            case BRIGHTNESS:
                ScriptC_brightness brightnessScript = new ScriptC_brightness(rs);
                brightnessScript.set_value(Settings.DEFAULT_BRIGHTNESS);
                brightnessScript.forEach_brightness(input, output);
                brightnessScript.destroy();
                break;
            default:
                return;
        }

        output.copyTo(bmp);

        input.destroy();
        output.destroy();
        rs.destroy();
    }

}
