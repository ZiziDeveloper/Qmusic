//
// Created by Administrator on 2018/12/15 0015.
//

#ifndef QMUSIC_AUDIOCODER_H
#define QMUSIC_AUDIOCODER_H

#include <stdint.h>
#include <pthread.h>
#include "ErrUtil.h"
#include "PlaySession.h"
#include "NotifyApplication.h"
#include "PacketQueue.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/time.h>
#include <libswresample/swresample.h>
};

/**
 * 音频编解码层
 */
class AudioCoder {
private:
    AVFormatContext* pAVFormatCtx = NULL;
    int mStreamIndex = -1;
    AVCodecContext* pAVCodecCtx = NULL;
    AVCodecParameters* pCodecPara = NULL;
    PacketQueue* pQueue = NULL;
public:
    pthread_t prepareDecodeThread;
    pthread_mutex_t prepareDecodeMutex;
    int sampleNum;
    uint8_t *buffer = NULL;
private:
    /**
     * 计算当前播放时间
     * @param time
     */
    void calcCurrentClock(double time);
public:
    AudioCoder();
    virtual ~AudioCoder();

    void prepare();
    void prepareDecoder();
    void start();
    //音频重采样
    int reSampleAudio(void **pcmBuf);
    int getSampleRate();
    void stop();
    void seek(int64_t second);

};


#endif //QMUSIC_AUDIOCODER_H
