package com.lucasgoudin.pictura;

import android.graphics.Bitmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

public class FiltersRS {
    public static void toGrayRS (Bitmap bmp, AppCompatActivity context) {
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
}
