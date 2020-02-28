#pragma version(1)
#pragma rs java_package_name(com.lucasgoudin.pictura)

int32_t gWidth;
int32_t gHeight;
int32_t gKernelSize;
rs_allocation gIn;
rs_allocation gCoeffs;
uchar4 __attribute__((kernel)) root(uint32_t x, uint32_t y) {
    int offset = gKernelSize / 2;
    uint32_t x1 = min((int32_t)x+offset, gWidth-offset);
    uint32_t x2 = max((int32_t)x-offset, 0);
    uint32_t y1 = min((int32_t)y+offset, gHeight-offset);
    uint32_t y2 = max((int32_t)y-offset, 0);

    float4 pixels[gKernelSize * gKernelSize];
    int index = 0;
    for(int index_y = y2; index_y <= y1; index_y++) {
        for(int index_x = x2; index_x <= x1; index_x++) {
            pixels[index] = convert_float4(rsGetElementAt_uchar4(gIn, index_x, index_y)) * rsGetElementAt_float(gCoeffs, index);
            index++;
        }
    }

    float4 sum = pixels[0];
    for(int i = 1; i < gKernelSize * gKernelSize; i++) {
        if(i != (gKernelSize * gKernelSize) / 2) {
            sum += pixels[i];
        }
    }

    sum = clamp(sum, 0.f, 255.f);
    return convert_uchar4(sum);
}
