//
// Created by Administrator on 2020/4/21 0021.
//


#include "AACEncoder.h"

AACEncoder::AACEncoder() {}

AACEncoder::~AACEncoder() {

}

int64_t AACEncoder::init(int channels, int sampleRate, int brate, int *ptrFrameLen) {

    mpEncodeNode.reset(new EncoderNode());
    mpEncodeNode->mChannels = channels;
    mpEncodeNode->mInputSize = 0;

    /**
     * 初始化编码器句柄（申请内存）
     */
    AACENC_ERROR ret = aacEncOpen(&(mpEncodeNode->mHandle), 0, mpEncodeNode->mChannels);
    if (ret != AACENC_OK) {
        LOGE("Unable to open encoder errcode : %d \n ", ret);
        return ret;
    }

    /**
     * 设置编码模式
     * AACENC_AOT : 1,AAC Main; 2,AAC LC; 3, AAC SSR; 8, CELP
     */
     ret = aacEncoder_SetParam(mpEncodeNode->mHandle, AACENC_AOT, AOT_LC);
    if (ret != AACENC_OK) {
        LOGE("Unable to set the AOT errcode : %d \n ", ret);
        return ret;
    }

    /**
     * 设置采样率
     */
    ret = aacEncoder_SetParam(mpEncodeNode->mHandle, AACENC_SAMPLERATE, sampleRate);
    if (ret != AACENC_OK) {
        LOGE("Unable to set the sample errcode : %d \n ", ret);
        return ret;
    }

    /**
     * 设置编码声道数
     */
    if (mpEncodeNode->mChannels == 2) {
        ret = aacEncoder_SetParam(mpEncodeNode->mHandle, AACENC_CHANNELMODE, MODE_2);
        if (ret != AACENC_OK) {
            LOGE("Unable to set the channel mode_2 errcode : %d \n ", ret);
            return ret;
        }
    } else {
        ret = aacEncoder_SetParam(mpEncodeNode->mHandle, AACENC_CHANNELMODE, MODE_1);
        if (ret != AACENC_OK) {
            LOGE("Unable to set the channel mode_1 errcode : %d \n ", ret);
            return ret;
        }
    }

    /**
     * 设置声道左右声道布局
     */
     ret = aacEncoder_SetParam(mpEncodeNode->mHandle, AACENC_CHANNELORDER, 1);
    if (ret != AACENC_OK) {
        LOGE("Unable to set the channel order errcode : %d \n ", ret);
        return ret;
    }

    /**
     * 设置码率
     */
     ret = aacEncoder_SetParam(mpEncodeNode->mHandle, AACENC_BITRATE, brate);
    if (ret != AACENC_OK) {
        LOGE("Unable to set the bitrate errcode : %d \n ", ret);
        return ret;
    }

    /**
     * 设置传输格式
     * - 0: raw access units
     *1: ADIF bitstream format
     *2: ADTS bitstream format
     *6: Audio Mux Elements (LATM) with muxConfigPresent = 1
     *7: Audio Mux Elements (LATM) with muxConfigPresent = 0, out of band StreamMuxConfig
     *10: Audio Sync Stream (LOAS)
     */
     ret = aacEncoder_SetParam(mpEncodeNode->mHandle, AACENC_TRANSMUX, 2);
    if(ret != AACENC_OK) {
        LOGE("Unable to set the ADTS transmux errcode : %d \n ", ret);
        return ret;
    }

    /**
     * 设置afterbuffer
     */
    ret = aacEncoder_SetParam(mpEncodeNode->mHandle, AACENC_AFTERBURNER, 1);
    if (ret != AACENC_OK) {
        LOGE("Unable to set the AACENC_AFTERBURNER errcode : %d \n ", ret);
        return ret;
    }

    /**
     * 配置编码器aacEncEncode传的除了编码器句柄，其他参数都是null表示改变编码器配置
     */
     ret = aacEncEncode(mpEncodeNode->mHandle, nullptr, nullptr, nullptr, nullptr);
    if (ret != AACENC_OK) {
        LOGE("Unable to config encoder errcode : %d \n ", ret);
        return ret;
    }

    /**
     * 获取编码器信息
     */
    ret = aacEncInfo(mpEncodeNode->mHandle, &(mpEncodeNode->mInfo));
    if (ret != AACENC_OK) {
        LOGE("Unable to get encoder info errcode : %d \n ", ret);
        return ret;
    }

    /**
     * 编码时候输入的字节数：声道数 * 16bit/8bit * 编码器每次输入的采样数
     */
    mpEncodeNode->mInputSize = mpEncodeNode->mChannels * 2 * mpEncodeNode->mInfo.frameLength;
    *ptrFrameLen = mpEncodeNode->mInputSize;
    LOGI("create fdkaac encoder success \n ");
    return 0;
}

void AACEncoder::encode(short* ptrInBuffer, uint8_t *ptrOutBuffer, int inBufLen, int outBufLen) {
    if (ptrInBuffer == nullptr || ptrOutBuffer == nullptr) {
        LOGE("encode errorr  ");
        return;
    }

    AACENC_BufDesc inBufDesc{0}, outBufDesc{0};
    AACENC_InArgs inArgs{0};
    AACENC_OutArgs outArgs{0};
    AACENC_BufferIdentifier inBufIndentifier = IN_AUDIO_DATA;
    AACENC_BufferIdentifier outBufIndentifier = OUT_BITSTREAM_DATA;


}

void AACEncoder::destroy() {
}
