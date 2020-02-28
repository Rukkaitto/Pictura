#pragma version(1)
#pragma rs java_package_name(com.lucasgoudin.pictura)
#include "rs_debug.rsh"


static float3 RGBtoHSV(float4 pixelf) {
    float3 primes = {pixelf.r / 255.0f, pixelf.g / 255.0f, pixelf.b / 255.0f};

    float cmax = fmax(primes.r, primes.g);
    cmax = fmax(cmax, primes.b);

    float cmin = fmin(primes.r, primes.g);
    cmin = fmin(cmin, primes.b);

    float delta = cmax - cmin;
    float h = 0.0f;
    float s;
    float v;

    if (delta == 0.0f) {
      h = 0.0f;
    } else if (cmax == primes.r) {
      h = 60 * fmod(((primes.g - primes.b) / delta), 6);
    } else if (cmax == primes.g) {
      h = 60 * (((primes.g - primes.b) / delta) + 2);
    } else if (cmax == primes.b) {
      h = 60 * (((primes.r - primes.g) / delta) + 4);
    }

    if (cmax == 0.0f) {
      s = 0.0f;
    } else {
      s = delta / cmax;
    }

    v = cmax;

    float3 hsv = {fabs(h), fabs(s), fabs(v)};
    return hsv;
}

static float3 HSVtoColor(float3 hsv) {
    float h = hsv.r;
    float s = hsv.g;
    float v = hsv.b;

    float c = v * s;
    float x = c * (1 - (fabs( fmod(h / 60, 2) - 1)));
    float m = v - c;

    float rprime = 0.0f, gprime = 0.0f, bprime = 0.0f;

    if (h >= 0 && h < 60) {
      rprime = c;
      gprime = x;
      bprime = 0.0f;
    } else if (h >= 60 && h < 120) {
      rprime = x;
      gprime = c;
      bprime = 0.0f;
    } else if (h >= 120 && h < 180) {
      rprime = 0.0f;
      gprime = c;
      bprime = x;
    } else if (h >= 180 && h < 240) {
      rprime = 0.0f;
      gprime = x;
      bprime = c;
    } else if (h >= 240 && h < 300) {
      rprime = x;
      gprime = 0.0f;
      bprime = c;
    } else if (h >= 300 && h < 360) {
      rprime = c;
      gprime = 0.0f;
      bprime = x;
    }

    float r = ((rprime + m) * 255.0f);
    float g = ((gprime + m) * 255.0f);
    float b = ((bprime + m) * 255.0f);

    float3 color = {r, g, b};
    return color;
}


int32_t histo[256];
float remapArray[256];
int size;

uchar4 RS_KERNEL root(uchar4 in) {
    //Convert input uchar4 to float4

    float4 f4 = rsUnpackColor8888(in);

    //Convert into hsv values
    float3 hsv = RGBtoHSV(f4);
    //Get v value between 0 and 255 (included)
    int32_t val = fmod(hsv.z * 255.0f, 255.0f);

    //Increment histogram for that value
    rsAtomicInc(&histo[val]);

    float3 newpixelf = HSVtoColor(hsv);

    //Put the values in the output uchar4
    return rsPackColorTo8888(newpixelf);
}

uchar4 RS_KERNEL remaptoRGB(uchar4 in) {
    //Convert input uchar4 to float4
    float4 f4 = rsUnpackColor8888(in);

    //Convert into hsv values
    float3 hsv = RGBtoHSV(f4);

    //Get v value between 0 and 255 (included)
    int32_t val = hsv.z * 25500.0f;
    //rsDebug("val : ", val);
    //rsDebug("remaparray : ", remapArray[val]);
    //Get v new value in the map array
    hsv.z = remapArray[val]/25500.0f;
    //rsDebug("hsv : ", hsv.z);
    float3 newpixelf = HSVtoColor(hsv);
    //Put the values in the output uchar4
    return rsPackColorTo8888(newpixelf);
}

void init() {
    //init the array with zeros
    for (int i = 0; i < 256; i++) {
        histo[i] = 0;
        remapArray[i] = 0.0f;
    }
}

void createRemapArray() {
    //create map for v
    float sum = 0;
    for (int i = 0; i < 256; i++) {
        sum += native_divide((float)histo[i],(float) (size));
        //rsDebug("sum : ", sum);
        if (i==0){
            remapArray[i] = sum;
        }
        else{
        remapArray[i] = remapArray[i-1] + sum;
        //rsDebug("arraycumul : ", remapArray[i]);
        }
    }
}