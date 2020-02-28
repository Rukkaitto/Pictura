package com.lucasgoudin.pictura.Filter;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Convolution {

    static final int rgbMax = 255;
    static void ApplyConvolution(Bitmap bmp, int[] filter, int sizeFilter){
        //recover the information about the picture + pixel
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int [] pixels = new int [w * h];
        bmp.getPixels(pixels, 0, w, 0,0,w,h);

        //kernel
        int halfSize = (sizeFilter-1)/2;
        int indexF; //index of the neighbor

        for(int y=0; y<h; y++){
            for(int x=0; x<w; x++){
                int index = y * w + x;
                int cptF=0; //index for the kernel

                //recover information about the pixel x,y
                int pixel = pixels[index];
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                for(int i=-halfSize; i<=halfSize; i++){ //y = lines
                    for(int j=-halfSize; j<=halfSize; j++){ //x = columns
                        indexF = (y+i) * w + x+j;
                        if(indexF>=0 && indexF<w*h){    //check if index valid (in the picture)
                            //recover information about the pixel x+j,y+i
                            int pixelF = pixels[indexF];
                            int rF = Color.red(pixelF);
                            int gF = Color.green(pixelF);
                            int bF = Color.blue(pixelF);

                            r += rF*filter[cptF];
                            g += gF*filter[cptF];
                            b += bF*filter[cptF];

                        }
                        cptF++;
                    }
                }
                r = Math.min(255, Math.max(0,r));
                g = Math.min(255, Math.max(0,g));
                b = Math.min(255, Math.max(0,b));

                pixels [index] = Color.rgb(r,g,b);
            }
        }
        bmp.setPixels(pixels,0,w,0,0,w,h);
    }
}
