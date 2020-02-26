#pragma  version (1)
#pragma rs java_package_name(com.lucasgoudin.pictura)
#pragma rs_fp_relaxed

int w;
int h;
int *pixels;
int size;
int* filter;

uchar4 __attribute__((kernel)) convolution(uchar4 in,uint32_t x,uint32_t y) {
    int cptF=0; //index of the filter
    int indexF; //index of the neighbor

    //recover the Kernel's coefficient
    int sumF=0;
    for(int i=0; i < size*size; i++){
        sumF += filter[i];
    }
    //uchar4 same= rsGetElementAt_uchar4(in, x,y);
    float4 color=0;
    int l =(size-1)/2;
    for (int i=-l;i<=l;i++){   //row - y
        for(int j=-l;j<=l;j++){    //col - x
            indexF=(y+i)*w + x + j;
            if(indexF >= 0 && indexF < w*h){    //check if index valid (in the picture)
                float4 temp = rsUnpackColor8888(pixels[indexF]);
                color.r +=  (filter[cptF] * temp.r)/sumF;
                color.g +=  (filter[cptF] * temp.g)/sumF;
                color.g +=  (filter[cptF] * temp.b)/sumF;
                cptF++;
            }
        }
    }
    if(color.r > 255){
        color.r = 255;
    }
    if(color.g > 255){
        color.g = 255;
    }
    if(color.b > 255){
        color.b = 255;
    }
    return rsPackColorTo8888(color);
}


