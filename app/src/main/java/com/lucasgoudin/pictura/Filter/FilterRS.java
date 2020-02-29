package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.lucasgoudin.pictura.ScriptC_Convolution;
import com.lucasgoudin.pictura.ScriptC_brightness;
import com.lucasgoudin.pictura.ScriptC_drawing;
import com.lucasgoudin.pictura.ScriptC_gray;
import com.lucasgoudin.pictura.ScriptC_isolate;
import com.lucasgoudin.pictura.ScriptC_sobel;
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
                Allocation allocationA = Allocation.createFromBitmap(rs, bmp);
                Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());

                ScriptC_improve improveScript = new ScriptC_improve(rs);
                improveScript.set_size(width * height);
                improveScript.forEach_root(allocationA, allocationB);
                improveScript.invoke_createRemapArray();
                improveScript.forEach_remaptoRGB(allocationB, allocationA);
                allocationA.copyTo(bmp);
                allocationA.destroy();
                allocationB.destroy();
                improveScript.destroy();
                break;
            case BLUR:
                int filterSize = 11;
                ScriptC_Convolution scriptC_gaussian = new ScriptC_Convolution(rs);

                scriptC_gaussian.set_gIn(input);
                scriptC_gaussian.set_gWidth(width);
                scriptC_gaussian.set_gHeight(height);
                scriptC_gaussian.set_gKernelSize(filterSize);

                float[] coeffs = gaussianMatrix(filterSize, filterSize);
                Allocation coeffs_alloc = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                coeffs_alloc.copyFrom(coeffs);

                scriptC_gaussian.set_gCoeffs(coeffs_alloc);

                scriptC_gaussian.forEach_root(output);
                scriptC_gaussian.destroy();
                output.copyTo(bmp);
                break;
            case LAPLACE:
                filterSize = 3;
                ScriptC_Convolution scriptC_laplace = new ScriptC_Convolution(rs);

                scriptC_laplace.set_gIn(input);
                scriptC_laplace.set_gWidth(width);
                scriptC_laplace.set_gHeight(height);
                scriptC_laplace.set_gKernelSize(filterSize);

                float[] coeffs_laplace = {-1,-1,-1,-1,8,-1,-1,-1,-1};
                Allocation coeffs__laplace_alloc = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                coeffs__laplace_alloc.copyFrom(coeffs_laplace);

                scriptC_laplace.set_gCoeffs(coeffs__laplace_alloc);

                scriptC_laplace.forEach_root(output);
                scriptC_laplace.destroy();
                output.copyTo(bmp);
                break;
            case SOBEL:
                filterSize = 3;
                ScriptC_sobel scriptC_sobel = new ScriptC_sobel(rs);

                scriptC_sobel.set_gIn(input);
                scriptC_sobel.set_gWidth(width);
                scriptC_sobel.set_gHeight(height);
                scriptC_sobel.set_gKernelSize(filterSize);

                float[] coeffs_sobel_x = {-1,0,1,-2,0,2,-1,0,1};
                float[] coeffs_sobel_y = {-1,-2,-1,0,0,0,1,2,1};
                Allocation sobelKernelX = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                Allocation sobelKernelY = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                sobelKernelX.copyFrom(coeffs_sobel_x);
                sobelKernelY.copyFrom(coeffs_sobel_y);

                scriptC_sobel.set_gCoeffsX(sobelKernelX);
                scriptC_sobel.set_gCoeffsY(sobelKernelY);
                scriptC_sobel.forEach_root(output);

                scriptC_sobel.destroy();
                output.copyTo(bmp);
                break;
            case AVERAGE:
                int filterSizeAverage = 5;
                ScriptC_Convolution scriptC_average = new ScriptC_Convolution(rs);

                scriptC_average.set_gIn(input);
                scriptC_average.set_gWidth(width);
                scriptC_average.set_gHeight(height);
                scriptC_average.set_gKernelSize(filterSizeAverage);

                float[] coeffs_average = CreateMask.averaging(filterSizeAverage);
                Allocation coeffs__average_alloc = Allocation.createSized(rs, Element.F32(rs), filterSizeAverage*filterSizeAverage, Allocation.USAGE_SCRIPT);
                coeffs__average_alloc.copyFrom(coeffs_average);

                scriptC_average.set_gCoeffs(coeffs__average_alloc);

                scriptC_average.forEach_root(output);
                scriptC_average.destroy();
                output.copyTo(bmp);
                break;
            case DRAWING:
                ScriptC_gray grayScriptDrawing = new ScriptC_gray(rs);
                Allocation temp = Allocation.createTyped(rs, output.getType());
                grayScriptDrawing.forEach_toGray(input, temp);
                grayScriptDrawing.destroy();

                filterSize = 6;
                ScriptC_drawing scriptC_drawing = new ScriptC_drawing(rs);

                scriptC_drawing.set_gIn(temp);
                scriptC_drawing.set_gWidth(width);
                scriptC_drawing.set_gHeight(height);
                scriptC_drawing.set_gKernelSize(filterSize);

                float[] coeffs_drawing = {  1,1,1,1,1,1,
                                            1,1,1,1,1,1,
                                            1,1,-8,-8,1,1,
                                            1,1,-8,-8,1,1,
                                            1,1,1,1,1,1,
                                            1,1,1,1,1,1};


                //float[] coeffs_drawing = {1,1,1,1,-8,1,1,1,1};
                for(int i = 0; i < coeffs_drawing.length; i++) {
                    coeffs_drawing[i] /= 8;
                }
                Allocation coeffs_drawing_allocation = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                coeffs_drawing_allocation.copyFrom(coeffs_drawing);

                scriptC_drawing.set_gCoeffs(coeffs_drawing_allocation);
                scriptC_drawing.forEach_root(output);

                scriptC_drawing.destroy();
                output.copyTo(bmp);
                break;
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
                Allocation allocationA = Allocation.createFromBitmap(rs, bmp);
                Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());

                ScriptC_improve improveScript = new ScriptC_improve(rs);
                improveScript.set_size(width * height);
                improveScript.forEach_root(allocationA, allocationB);
                improveScript.invoke_createRemapArray();
                improveScript.forEach_remaptoRGB(allocationB, allocationA);
                allocationA.copyTo(bmp);
                allocationA.destroy();
                allocationB.destroy();
                improveScript.destroy();
                break;
            case BLUR:
                //Convolution.ApplyConvolution(bmp, CreateMask.gaussien((int)value), 5);
                int filterSize;
                if((int) value % 2 == 0) {
                    filterSize = (int) value + 1;
                } else {
                    filterSize = (int) value;
                }
                //ScriptC_convolve scriptC_convolve = new ScriptC_convolve(rs);
                ScriptC_Convolution scriptC_gaussian = new ScriptC_Convolution(rs);

                scriptC_gaussian.set_gIn(input);
                scriptC_gaussian.set_gWidth(width);
                scriptC_gaussian.set_gHeight(height);
                scriptC_gaussian.set_gKernelSize(filterSize);

                float[] coeffs = gaussianMatrix(filterSize, filterSize*2);
                Allocation coeffs_alloc = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                coeffs_alloc.copyFrom(coeffs);

                scriptC_gaussian.set_gCoeffs(coeffs_alloc);

                scriptC_gaussian.forEach_root(output);
                scriptC_gaussian.destroy();
                output.copyTo(bmp);
                break;
            case LAPLACE:
                filterSize = 3;
                ScriptC_Convolution scriptC_laplace = new ScriptC_Convolution(rs);

                scriptC_laplace.set_gIn(input);
                scriptC_laplace.set_gWidth(width);
                scriptC_laplace.set_gHeight(height);
                scriptC_laplace.set_gKernelSize(filterSize);

                float[] coeffs_laplace = {-1,-1,-1,-1,8,-1,-1,-1,-1};
                Allocation coeffs__laplace_alloc = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                coeffs__laplace_alloc.copyFrom(coeffs_laplace);

                scriptC_laplace.set_gCoeffs(coeffs__laplace_alloc);

                scriptC_laplace.forEach_root(output);
                scriptC_laplace.destroy();
                output.copyTo(bmp);
                break;
            case SOBEL:
                filterSize = 3;
                ScriptC_sobel scriptC_sobel = new ScriptC_sobel(rs);

                scriptC_sobel.set_gIn(input);
                scriptC_sobel.set_gWidth(width);
                scriptC_sobel.set_gHeight(height);
                scriptC_sobel.set_gKernelSize(filterSize);

                float[] coeffs_sobel_x = {-1,0,1,-2,0,2,-1,0,1};
                float[] coeffs_sobel_y = {-1,-2,-1,0,0,0,1,2,1};
                Allocation sobelKernelX = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                Allocation sobelKernelY = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                sobelKernelX.copyFrom(coeffs_sobel_x);
                sobelKernelY.copyFrom(coeffs_sobel_y);

                scriptC_sobel.set_gCoeffsX(sobelKernelX);
                scriptC_sobel.set_gCoeffsY(sobelKernelY);
                scriptC_sobel.forEach_root(output);

                scriptC_sobel.destroy();
                output.copyTo(bmp);
                break;
            case AVERAGE:
                int filterSizeAverage;
                if((int) value % 2 == 0) {
                    filterSizeAverage = (int) value + 1;
                } else {
                    filterSizeAverage = (int) value;
                }
                ScriptC_Convolution scriptC_average = new ScriptC_Convolution(rs);

                scriptC_average.set_gIn(input);
                scriptC_average.set_gWidth(width);
                scriptC_average.set_gHeight(height);
                scriptC_average.set_gKernelSize(filterSizeAverage);

                float[] coeffs_average = CreateMask.averaging(filterSizeAverage);
                Allocation coeffs__average_alloc = Allocation.createSized(rs, Element.F32(rs), filterSizeAverage*filterSizeAverage, Allocation.USAGE_SCRIPT);
                coeffs__average_alloc.copyFrom(coeffs_average);

                scriptC_average.set_gCoeffs(coeffs__average_alloc);

                scriptC_average.forEach_root(output);
                scriptC_average.destroy();
                output.copyTo(bmp);
                break;
            case DRAWING:
                ScriptC_gray grayScriptDrawing = new ScriptC_gray(rs);
                Allocation temp = Allocation.createTyped(rs, output.getType());
                grayScriptDrawing.forEach_toGray(input, temp);
                grayScriptDrawing.destroy();

                filterSize = 6;
                ScriptC_drawing scriptC_drawing = new ScriptC_drawing(rs);

                scriptC_drawing.set_gIn(temp);
                scriptC_drawing.set_gWidth(width);
                scriptC_drawing.set_gHeight(height);
                scriptC_drawing.set_gKernelSize(filterSize);

                float[] coeffs_drawing = {1,1,1,1,1,1,
                                          1,1,1,1,1,1,
                                          1,1,-8,-8,1,1,
                                          1,1,-8,-8,1,1,
                                          1,1,1,1,1,1,
                                          1,1,1,1,1,1};


                //float[] coeffs_drawing = {1,1,1,1,-8,1,1,1,1};
                for(int i = 0; i < coeffs_drawing.length; i++) {
                    coeffs_drawing[i] /= (11 - (int)value);
                }
                Allocation coeffs_drawing_allocation = Allocation.createSized(rs, Element.F32(rs), filterSize*filterSize, Allocation.USAGE_SCRIPT);
                coeffs_drawing_allocation.copyFrom(coeffs_drawing);

                scriptC_drawing.set_gCoeffs(coeffs_drawing_allocation);
                scriptC_drawing.forEach_root(output);

                scriptC_drawing.destroy();
                output.copyTo(bmp);
                break;
            default:
                return;
        }



        input.destroy();
        output.destroy();
        rs.destroy();
    }

    private float[] gaussianMatrix(int size, float sigma) {
        float kernel[] = new float[size*size];
        float mean = size / 2.f;
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
