#pragma version(1)
#pragma rs java_package_name(com.lucasgoudin.pictura)

float* hist_cumul;

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


uchar4 RS_KERNEL improve(uchar4 in) {
    const float4 pixelf = rsUnpackColor8888(in);
    float3 hsv = RGBtoHSV(pixelf);
    hsv.z = hist_cumul[(int)(hsv.z*255*255)];
    rsDebug("histcumul ", (int)(hsv.z*255*255));
    float3 newpixelf = HSVtoColor(hsv);
    return rsPackColorTo8888(newpixelf);
}