#pragma version(1)
#pragma rs java_package_name(com.lucasgoudin.pictura)


uchar4 RS_KERNEL negative(uchar4 in) {
    const float4 pixelf = rsUnpackColor8888(in);

    return rsPackColorTo8888 (1.f - pixelf.r, 1.f - pixelf.g, 1.f - pixelf.b, pixelf.a);
}