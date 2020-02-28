package com.lucasgoudin.pictura.Filter;

import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.sqrt;

public class CreateMask {
    static final double sigma = 0.8;

    static float[] gaussian(int size, float sigma) {
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

    static float[] averaging(int size){
        float[] filter = new float[size*size];
        for(int i=0; i<size*size; i++){
            filter[i]= 1.f/(float)(size*size);
        }
        return filter;
    }
}
