#pragma version(1)
#pragma rs java_package_name(com.lucasgoudin.pictura)

static float brightM = 0.f;
static float brightC = 0.f;

void setBright(float v){
    brightM = pow(2.f, v / 100.f);
    brightC = 127.f - brightM * 127.f;
}

void contrast(const uchar4 *in, uchar4 *out) {
#if 0
    out->r = rsClamp((int)(brightM * in->r + brightC),0,255);
    out->g = rsClamp((int)(brightM * in->g + brightC),0,255);
    out->b = rsClamp((int)(brightM * in->b + brightC),0,255);
#else
    float3 v = convert_float3(in->rgb) * brightM + brightC;
    out->rgb = convert_uchar3(clamp(v, 0.f, 255.f));
#endif

}