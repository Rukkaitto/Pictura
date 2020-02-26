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
                brightnessScript.invoke_setBright(150.f);
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
            case CONTRAST:
                float[] LUT = new float[256];
                LUT = createLUT(bmp);
                Allocation LUT_alloc = Allocation.createSized( rs, Element.F32_4(rs), Allocation.USAGE_SCRIPT);
                LUT_alloc.copyFrom(LUT);
                ScriptC_contrast contrastScript = new ScriptC_contrast(rs);
                contrastScript.bind_LUT(LUT_alloc);
                contrastScript.forEach_contrast(input, output);
                contrastScript.destroy();
                break;
            case IMPROVE:
                float[] histcumul = new float[256];
                histcumul = createHist(bmp);
                Allocation hist_alloc = Allocation.createSized( rs,Element.F32_4(rs) , Allocation.USAGE_SCRIPT);
                hist_alloc.copyFrom(histcumul);
                ScriptC_improve improveScript = new ScriptC_improve(rs);
                improveScript.bind_hist_cumul(hist_alloc);
                improveScript.forEach_improve(input, output);
                improveScript.destroy();
                break;
            case BLUR:
                Convolution.ApplyConvolution(bmp, CreateMask.gaussien(5), 5); //size -> variable
                break;
            case AVERAGE:
                Convolution.ApplyConvolution(bmp, CreateMask.averaging(5), 5); //size -> variable
                break;
            case SOBEL:
                Convolution.ApplyConvolution(bmp, CreateMask.sobelX(), 3); //size -> do not modify
                Convolution.ApplyConvolution(bmp, CreateMask.sobelY(), 3); //size -> do not modify
                break;
            case LAPLACE:
                Convolution.ApplyConvolution(bmp, CreateMask.laplace(), 3); //size -> do not modify
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
                brightnessScript.invoke_setBright(value);
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
            case CONTRAST:
                float[] LUT = new float[256];
                LUT = createLUT(bmp);
                Allocation LUT_alloc = Allocation.createSized( rs,Element.F32_4(rs) , Allocation.USAGE_SCRIPT);
                LUT_alloc.copyFrom(LUT);
                ScriptC_contrast contrastScript = new ScriptC_contrast(rs);
                contrastScript.bind_LUT(LUT_alloc);
                contrastScript.forEach_contrast(input, output);
                contrastScript.destroy();
                break;
            case IMPROVE:
                float[] histcumul = new float[256];
                histcumul = createHist(bmp);
                Allocation hist_alloc = Allocation.createSized( rs,Element.F32_4(rs) , Allocation.USAGE_SCRIPT);
                hist_alloc.copyFrom(histcumul);
                ScriptC_improve improveScript = new ScriptC_improve(rs);
                improveScript.bind_hist_cumul(hist_alloc);
                improveScript.forEach_improve(input, output);
                improveScript.destroy();
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

    float[] createHist(Bitmap bmp){
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int hist[] = new int [256];
        final int[] pixels = new int[w * h];
        float p;
        float hsv[] = new float[3];
        float hist_cumul[] = new float [256];

        bmp.getPixels(pixels, 0, w, 0, 0, w, h);

        //création de l'histogramme de niveau de gris de la photo.

        for (int x = 0 ; x< w ; x++){
            for (int y =0 ; y<h; y++) {

                final int offset = y * w + x;

                int r = Color.red(pixels[offset]);
                int g = Color.green(pixels[offset]);
                int b = Color.blue(pixels[offset]);

                new_RGBToHSV(r,g,b,hsv);
                hist[(int)(hsv[2]*255)] ++;
            }
        }
        for (int i =0 ; i<256; i++){
            p = (hist[i])/(float)(w*h);
            if (i == 0){
                hist_cumul[i] = p;
            }
            else{
                hist_cumul[i] = hist_cumul[i-1] + p;
            }
        }
        return hist_cumul;
    }

    float[] createLUT (Bitmap bmp){
        float[] LUT = new float[256];
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int min = 255;
        int max = 0;
        float hsv[] = new float[3];

        int[] nb_pix = new int[w * h];

        bmp.getPixels(nb_pix, 0, w, 0, 0, w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                int i = y * w + x;
                int r = Color.red(nb_pix[i]);
                int g = Color.green(nb_pix[i]);
                int b = Color.blue(nb_pix[i]);

                new_RGBToHSV(r,g,b,hsv);

                if (min > (int)(hsv[2]*255)){
                    min = (int)(hsv[2]*255);
                }
                if (max < (int)(hsv[2]*255)){
                    max = (int)(hsv[2]*255);
                }
            }
        }

        for (int ng = 0; ng < 256; ng++) {
            LUT[ng] = (255 * (ng - min)) / (max - min);
        }
        return LUT;
    }

    static void new_RGBToHSV(int r, int g, int b, float[] hsv) {
        float r2 = r / 255.f;
        float g2 = g / 255.f;
        float b2 = b / 255.f;

        float rgbmax = Math.max(r2, g2);
        float cmax = Math.max(rgbmax, b2);

        float rgbmin = Math.min(r2, g2);
        float cmin = Math.min(rgbmin, b2);

        float d = cmax - cmin;

        float h = 0;

        if (d == 0) {
            h = 0;
        } else if (cmax == r2) {
            h = 60 * (((g2 - b2) % 6) / d);
        } else if (cmax == g2) {
            h = 60 * (((b2 - r2) / d) + 2);
        } else if (cmax == b2) {
            h = 60 * (((r2 - g2) / d) + 4);
        }

        float s = 0;

        if (cmax == 0) {
            s = 0;
        } else {
            s = d / cmax;
        }

        hsv[0] = h;
        hsv[1] = s;
        hsv[2] = cmax;
    }

    /**
     * @return the Activity's context
     */
    AppCompatActivity getContext() {
        return context;
    }
}
