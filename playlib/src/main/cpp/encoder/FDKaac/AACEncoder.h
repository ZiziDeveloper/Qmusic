//
// Created by Administrator on 2020/4/21 0021.
//

#ifndef QMUSIC_AACENCODER_H
#define QMUSIC_AACENCODER_H

#include "libAACenc/include/aacenc_lib.h"
#include <cstdint>
#include <cstring>
#include <cstdlib>

#include "android/log.h"
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"qmusic_encoder",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"qmusic_encoder",FORMAT,##__VA_ARGS__);

typedef struct _FDKaacEncoder {
    HANDLE_AACENCODER handle;
    AACENC_InfoStruct info;
    int input_size;
    int channels;

} FDKaacEncoder;

class AACEncoder {
private:
    FDKaacEncoder* mEncoder;
    /**
     * AACENC_AOT : 1,AAC Main; 2,AAC LC; 3, AAC SSR; 8, CELP
     */
    constexpr static uint32_t AOT_MAIN = 1;
    constexpr static uint32_t AOT_LC = 2;
    constexpr static uint32_t AOT_SSR = 3;
    constexpr static uint32_t AOT_CELP = 8;
public:

    AACEncoder();

    virtual ~AACEncoder();

    /**
     * 初始化编码器
     * @param channels 声道：1，单声道； 2，双声道
     * @param sampleRate  采样率
     * @param brate 比特率
     * @param ptrFrameLen 帧长
     * @return 编码器指针
     */
    int64_t init(int channels, int sampleRate, int brate, int* ptrFrameLen);

    /**
     * 编码函数
     * @param ptrEncoder 编码器指针
     * @param ptrBuffer 待编码数据
     * @param len 数据长度
     */
    void encode(int64_t ptrEncoder, short* ptrBuffer, int len);

    /**
     * 销毁编码器
     * @param ptrEncoder 编码器指针
     */
    void destroy(int64_t ptrEncoder);

};


#endif //QMUSIC_AACENCODER_H
