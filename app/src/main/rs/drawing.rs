#pragma version(1)
#pragma rs java_package_name(com.lucasgoudin.pictura)

int32_t gWidth;
int32_t gHeight;
int32_t gKernelSize;
rs_allocation gIn;
rs_allocation gCoeffs;
uchar4 __attribute__((kernel)) root(uint32_t x, uint32_t y) {
    int offsetA, offsetB;

    if(gKernelSize % 2 != 0) {
        offsetA = gKernelSize / 2;
        offsetB = gKernelSize / 2;
    } else {
        offsetA = gKernelSize / 2;
        offsetB = gKernelSize / 2 - 1;
    }


    uint32_t x1 = min((int32_t)x+offsetB, gWidth-offsetA);
    uint32_t x2 = max((int32_t)x-offsetA, 0);
    uint32_t y1 = min((int32_t)y+offsetB, gHeight-offsetA);
    uint32_t y2 = max((int32_t)y-offsetA, 0);

    float4 pixels[gKernelSize * gKernelSize];
    int index = 0;
    for(int index_y = y2; index_y <= y1; index_y++) {
        for(int index_x = x2; index_x <= x1; index_x++) {
            pixels[index] = convert_float4(rsGetElementAt_uchar4(gIn, index_x, index_y));
            index++;
        }
    }

    float4 sum = pixels[0] * rsGetElementAt_float(gCoeffs, 0);
    for(int i = 1; i < gKernelSize * gKernelSize; i++) {
        sum += (pixels[i] * rsGetElementAt_float(gCoeffs, i));
    }

    sum = 255.f - sum;
    sum = clamp(sum, 0.f, 240.f);
    sum.a = 255.f;

    return convert_uchar4(sum);
}
