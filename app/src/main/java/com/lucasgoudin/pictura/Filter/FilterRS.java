package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;

import com.lucasgoudin.pictura.ScriptC_brightness;
import com.lucasgoudin.pictura.ScriptC_gray;
import com.lucasgoudin.pictura.ScriptC_isolate;
import com.lucasgoudin.pictura.ScriptC_tint;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

/**
 * A class the manages the Renderscript side of the application
 */
public class FilterRS {
    private FilterName filterName;
    private AppCompatActivity context;
    private RenderScript rs;

    /**
     * Creates a Renderscript filter
     * @param filterName the name of the filter
     * @param context the Activity's context
     */
    public FilterRS(FilterName filterName, AppCompatActivity context) {
        this.filterName = filterName;
        this.context = context;
        this.rs = RenderScript.create(context);
    }

    /**
     * Applies the filter to a bitmap with default values (used for previews)
     * @param bmp the bitmap to apply the filter to
     * @param context the Activity's context
     */
    void apply(Bitmap bmp, AppCompatActivity context) {
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
                brightnessScript.set_value(0.001f);
                brightnessScript.forEach_brightness(input, output);
                brightnessScript.destroy();
                break;
            case TINT:
                ScriptC_tint tintScript = new ScriptC_tint(rs);
                tintScript.set_hue(new Random().nextFloat() * 359);
                tintScript.forEach_tint(input, output);
                tintScript.destroy();
                break;
            case ISOLATE:
                ScriptC_isolate isolateScript = new ScriptC_isolate(rs);
                isolateScript.set_hue(0.0f);
                isolateScript.forEach_isolate(input, output);
                isolateScript.destroy();
                break;
                //TODO: égalisation (avec valeurs par défaut pour les previews)
                //TODO: extension (avec valeurs par défaut pour les previews)
                //TODO: convolution (avec valeurs par défaut pour les previews)
            default:
                return;
        }

        output.copyTo(bmp);

        input.destroy();
        output.destroy();
        rs.destroy();
    }

    /**
     * Applies the filter to a bitmap with a given value (used for the actual photo)
     * @param bmp the bitmap to apply the filter to
     * @param value the slider value which will be passed to the script
     */
    void apply(Bitmap bmp, float value) {
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
                brightnessScript.set_value(value);
                brightnessScript.forEach_brightness(input, output);
                brightnessScript.destroy();
                break;
            case TINT:
                ScriptC_tint tintScript = new ScriptC_tint(rs);
                tintScript.set_hue(value);
                tintScript.forEach_tint(input, output);
                tintScript.destroy();
                break;
            case ISOLATE:
                ScriptC_isolate isolateScript = new ScriptC_isolate(rs);
                isolateScript.set_hue(value);
                isolateScript.forEach_isolate(input, output);
                isolateScript.destroy();
                break;
                //TODO: égalisation
                //TODO: extension
                //TODO: convolution
            default:
                return;
        }

        output.copyTo(bmp);

        input.destroy();
        output.destroy();
        rs.destroy();
    }

    /**
     * @return the Activity's context
     */
    AppCompatActivity getContext() {
        return context;
    }
}
