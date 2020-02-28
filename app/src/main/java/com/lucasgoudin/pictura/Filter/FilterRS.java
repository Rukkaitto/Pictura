package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.lucasgoudin.pictura.ScriptC_brightness;
import com.lucasgoudin.pictura.ScriptC_convolve;
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
                ScriptC_improve improveScript = new ScriptC_improve(rs);
                improveScript.set_size(width*height);
                improveScript.forEach_root(input, output);
                improveScript.invoke_createRemapArray();
                improveScript.forEach_remaptoRGB(input, output);
                improveScript.destroy();
                output.copyTo(bmp);
                break;
            case BLUR:
                int filterSize = 25;

                ScriptC_convolve scriptC_convolve = new ScriptC_convolve(rs);

                scriptC_convolve.set_gIn(input);
                scriptC_convolve.set_gWidth(width);
                scriptC_convolve.set_gHeight(height);
                scriptC_convolve.set_gKernelSize(filterSize);

                float[] coeffs = gaussianMatrix(filterSize, filterSize);
                Allocation coeffs_alloc = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                coeffs_alloc.copyFrom(coeffs);

                scriptC_convolve.set_gCoeffs(coeffs_alloc);

                scriptC_convolve.forEach_root(output);
                scriptC_convolve.destroy();
                output.copyTo(bmp);
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
                ScriptC_improve improveScript = new ScriptC_improve(rs);
                improveScript.set_size(width*height);
                improveScript.forEach_root(input, output);
                improveScript.invoke_createRemapArray();
                improveScript.forEach_remaptoRGB(input, output);
                improveScript.destroy();
                output.copyTo(bmp);
                break;

            case BLUR:
                //Convolution.ApplyConvolution(bmp, CreateMask.gaussien((int)value), 5);
                int filterSize;
                if((int) value % 2 == 0) {
                    filterSize = (int) value + 1;
                } else {
                    filterSize = (int) value;
                }

                ScriptC_convolve scriptC_convolve = new ScriptC_convolve(rs);

                scriptC_convolve.set_gIn(input);
                scriptC_convolve.set_gWidth(width);
                scriptC_convolve.set_gHeight(height);
                scriptC_convolve.set_gKernelSize(filterSize);

                float[] coeffs = gaussianMatrix(filterSize, filterSize);
                Allocation coeffs_alloc = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                coeffs_alloc.copyFrom(coeffs);

                scriptC_convolve.set_gCoeffs(coeffs_alloc);

                scriptC_convolve.forEach_root(output);
                scriptC_convolve.destroy();
                output.copyTo(bmp);
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

    float[] gaussianMatrix(int size, float sigma) {
        float kernel[] = new float[size*size];
        float mean = size / 2;
        float sum = 0.0f; // For accumulating the kernel values
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                kernel[x + y * size] = (float)(Math.exp(-0.5 * (Math.pow((x - mean) / sigma, 2.0) + Math.pow((y - mean) / sigma, 2.0))))
                        / (float)(2 * Math.PI * sigma * sigma);

                // Accumulate the kernel values
                sum += kernel[x + y * size];
            }
        }

        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                kernel[x + y * size] /= sum;
            }
        }

        return kernel;
    }






    /**
     * @return the Activity's context
     */
    AppCompatActivity getContext() {
        return context;
    }
}
