#pragma version(1)
#pragma rs java_package_name(com.lucasgoudin.pictura)

int32_t gWidth;
int32_t gHeight;
int32_t gKernelSize;
rs_allocation gIn;
rs_allocation gCoeffsX;
rs_allocation gCoeffsY;

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
            pixels[index] = convert_float4(rsGetElementAt_uchar4(gIn, index_x, index_y));
            index++;
        }
    }

    float4 sumX = pixels[0] * rsGetElementAt_float(gCoeffsX, 0);
    float4 sumY = pixels[0] * rsGetElementAt_float(gCoeffsY, 0);
    for(int i = 1; i < gKernelSize * gKernelSize; i++) {
        sumX += (pixels[i] * rsGetElementAt_float(gCoeffsX, i));
        sumY += (pixels[i] * rsGetElementAt_float(gCoeffsY, i));
    }

    float4 result = sqrt(sumX*sumX+sumY*sumY);
    result = clamp(result, 0.f, 255.f);
    result.a = 255.f;

    return convert_uchar4(result);
}
