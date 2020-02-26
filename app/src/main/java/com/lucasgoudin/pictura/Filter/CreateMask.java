package com.lucasgoudin.pictura.Filter;

import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.sqrt;

public class CreateMask {
    static final double sigma = 0.8;

    static int[] gaussien(int size){
        int halfSize = (size-1)/2;
        int[] filter = new int[size*size];
        double[] temp = new double[size*size];
        int index = 0;
        int kernelFactor=0;
        for(int y=-halfSize; y<=halfSize; y++){
            for(int x=-halfSize; x<=halfSize; x++){
                temp[index] = (1/(2*PI*sigma*sigma)) * exp(-((x*x)+(y*y))/(2*(sigma*sigma)));
                index++;
            }
        }
        kernelFactor = (int) (1/temp[0]);
        for(int i=0; i<size*size;i++){
            filter[i] = (int) (temp[i] * kernelFactor);
            if(filter[i] == 0 && temp[i] !=0){
                filter[i] = 1;
            }
        }
        return filter;
    }

    static int[] averaging(int size){
        int[] filter = new int[size*size];
        for(int i=0; i<size*size; i++){
            filter[i]= 1;
        }
        return filter;
    }

    static int[] laplace(){
        int filter[] = {0,1,0,1,-4,1,0,1,0};
        return filter;
    }

    static int[] sobelX(){
        int filter[] = {1,0,-1,2,0,-2,1,0,-1};
        return filter;
    }

    static int[] sobelY(){
        int filter[] = {1,2,1,0,0,0,-1,-2,-1};
        return filter;
    }


}
