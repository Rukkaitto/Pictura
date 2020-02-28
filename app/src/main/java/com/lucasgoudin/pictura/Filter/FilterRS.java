package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.lucasgoudin.pictura.ScriptC_brightness;
import com.lucasgoudin.pictura.ScriptC_gray;
import com.lucasgoudin.pictura.ScriptC_isolate;
import com.lucasgoudin.pictura.ScriptC_tint;
import com.lucasgoudin.pictura.ScriptC_contrast;
import com.lucasgoudin.pictura.ScriptC_improve;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
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
     */
    void apply(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        switch (filterName) {
            case TOGRAY:
                ScriptC_gray grayScript = new ScriptC_gray(rs);
                grayScript.forEach_toGray(input, output);
                grayScript.destroy();
                output.copyTo(bmp);
                break;
            case BRIGHTNESS:
                ScriptC_brightness brightnessScript = new ScriptC_brightness(rs);
                brightnessScript.invoke_setBright(150.f);
                brightnessScript.forEach_brightness(input, output);
                brightnessScript.destroy();
                output.copyTo(bmp);
                break;
            case TINT:
                ScriptC_tint tintScript = new ScriptC_tint(rs);
                tintScript.set_hue(new Random().nextFloat() * 359);
                tintScript.forEach_tint(input, output);
                tintScript.destroy();
                output.copyTo(bmp);
                break;
            case ISOLATE:
                ScriptC_isolate isolateScript = new ScriptC_isolate(rs);
                isolateScript.set_hue(0.0f);
                isolateScript.forEach_isolate(input, output);
                isolateScript.destroy();
                output.copyTo(bmp);
                break;
            case CONTRAST:
                ScriptC_contrast mScript = new ScriptC_contrast(rs);
                mScript.invoke_setBright(50.f);
                mScript.forEach_contrast(input, output);
                mScript.destroy();
                output.copyTo(bmp);
                break;
            case IMPROVE:
                /*float[] histcumul = new float[256];
                histcumul = createHist(bmp);

                Allocation hist_alloc = Allocation.createSized( rs, Element.F32_4(rs), 256);
                hist_alloc.copyFrom(histcumul);*/

                ScriptC_improve improveScript = new ScriptC_improve(rs);
                improveScript.set_size(width*height);
                //improveScript.bind_hist_cumul(hist_alloc);
                improveScript.forEach_root(input, output);
                improveScript.invoke_createRemapArray();
                improveScript.forEach_remaptoRGB(output, input);
                improveScript.destroy();
                output.copyTo(bmp);
                break;
            case BLUR:
                Convolution.ApplyConvolution(bmp, CreateMask.gaussien(5), 5); //size -> variable
                break;
            case LAPLACE:
                Convolution.ApplyConvolution(bmp, CreateMask.laplace(), 3); //size -> do not modify
                break;
            case SOBEL:
                Convolution.ApplyConvolution(bmp, CreateMask.sobelX(), 3);
                Convolution.ApplyConvolution(bmp, CreateMask.sobelY(), 3);
                break;
            case AVERAGE:
                Convolution.ApplyConvolution(bmp, CreateMask.averaging(5), 5); //size -> variable
                break;
                //TODO: égalisation (avec valeurs par défaut pour les previews)
                //TODO: extension (avec valeurs par défaut pour les previews)
            default:
                return;
        }



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
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        switch (filterName) {
            case TOGRAY:
                ScriptC_gray grayScript = new ScriptC_gray(rs);
                grayScript.forEach_toGray(input, output);
                grayScript.destroy();
                output.copyTo(bmp);
                break;
            case BRIGHTNESS:
                ScriptC_brightness brightnessScript = new ScriptC_brightness(rs);
                brightnessScript.invoke_setBright(value);
                brightnessScript.forEach_brightness(input, output);
                brightnessScript.destroy();
                output.copyTo(bmp);
                break;
            case TINT:
                ScriptC_tint tintScript = new ScriptC_tint(rs);
                tintScript.set_hue(value);
                tintScript.forEach_tint(input, output);
                tintScript.destroy();
                output.copyTo(bmp);
                break;
            case ISOLATE:
                ScriptC_isolate isolateScript = new ScriptC_isolate(rs);
                isolateScript.set_hue(value);
                isolateScript.forEach_isolate(input, output);
                isolateScript.destroy();
                output.copyTo(bmp);
                break;
            case CONTRAST:
                ScriptC_contrast mScript = new ScriptC_contrast(rs);
                mScript.invoke_setBright(value);
                mScript.forEach_contrast(input, output);
                mScript.destroy();
                output.copyTo(bmp);
                break;

            case IMPROVE:
                /*float[] histcumul = new float[256];
                histcumul = createHist(bmp);

                Allocation hist_alloc = Allocation.createSized( rs, Element.F32_4(rs), 256);
                hist_alloc.copyFrom(histcumul);*/

                ScriptC_improve improveScript = new ScriptC_improve(rs);
                improveScript.set_size(width*height);
                //improveScript.bind_hist_cumul(hist_alloc);
                improveScript.forEach_root(input, output);
                improveScript.invoke_createRemapArray();
                improveScript.forEach_remaptoRGB(output, input);
                improveScript.destroy();
                output.copyTo(bmp);
                break;

            case BLUR:
                Convolution.ApplyConvolution(bmp, CreateMask.gaussien((int)value), 5);
                break;
            case LAPLACE:
                Convolution.ApplyConvolution(bmp, CreateMask.laplace(), 3);
                break;
            case SOBEL:
                Convolution.ApplyConvolution(bmp, CreateMask.sobelX(), 3);
                Convolution.ApplyConvolution(bmp, CreateMask.sobelY(), 3);
                break;
            case AVERAGE:
                Convolution.ApplyConvolution(bmp, CreateMask.averaging((int)value), 5);
                break;
                //TODO: égalisation
                //TODO: extension
                //TODO: convolution
            default:
                return;
        }



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
