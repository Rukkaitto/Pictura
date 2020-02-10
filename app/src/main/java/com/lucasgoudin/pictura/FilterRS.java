package com.lucasgoudin.pictura;

import android.graphics.Bitmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

class FilterRS {
    private FilterName filterName;

    FilterRS(FilterName filterName) {
        this.filterName = filterName;
    }

    void apply(Bitmap bmp, AppCompatActivity context) {
        switch (filterName) {
            case TOGRAY:
                toGrayRS(bmp, context);
                break;
            case BRIGHTNESS:
                brightnessRS(bmp, context);
                break;
            default:
                break;
        }
    }

    private void toGrayRS (Bitmap bmp, AppCompatActivity context) {
        RenderScript rs = RenderScript.create(context);

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_gray grayScript = new ScriptC_gray(rs);

        grayScript.forEach_toGray(input, output);

        output.copyTo(bmp);

        input.destroy();
        output.destroy();
        grayScript.destroy();
        rs.destroy();
    }

    private void brightnessRS (Bitmap bmp, AppCompatActivity context) {
        float brightness = 0.001f;
        RenderScript rs = RenderScript.create(context);

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_brightness grayScript = new ScriptC_brightness(rs);

        grayScript.set_value(brightness);
        grayScript.forEach_brightness(input, output);

        output.copyTo(bmp);

        input.destroy();
        output.destroy();
        grayScript.destroy();
        rs.destroy();
    }
}
