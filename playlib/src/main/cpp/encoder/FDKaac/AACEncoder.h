//
// Created by Administrator on 2020/4/21 0021.
//

#ifndef QMUSIC_AACENCODER_H
#define QMUSIC_AACENCODER_H

#include "libAACenc/include/aacenc_lib.h"
#include <cstdint>
#include <cstring>
#include <cstdlib>
#include <memory>

#include "android/log.h"
#include "EncoderNode.h"

#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"qmusic_encoder",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"qmusic_encoder",FORMAT,##__VA_ARGS__);

class AACEncoder {
private:
    std::unique_ptr<EncoderNode> mpEncodeNode{nullptr};
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
     * @param ptrInBuffer 待编码数据
     * @param ptrOutBuffer 编码之后的数据
     * @param inBufLen 待编码缓存长度
     * @param outBufLen 编码之后缓存长度
     */
    void encode(short* ptrInBuffer, uint8_t *ptrOutBuffer, int inBufLen, int outBufLen);

    /**
     * 销毁编码器
     * @param ptrEncoder 编码器指针
     */
    void destroy();

};


#endif //QMUSIC_AACENCODER_H
