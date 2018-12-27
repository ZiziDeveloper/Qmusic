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
extern "C" {
#include <libavformat/avformat.h>
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
public:
    pthread_t prepareDecodeThread;
    pthread_mutex_t prepareDecodeMutex;
public:
    AudioCoder();
    virtual ~AudioCoder();

    void prepare();
    void prepareDecoder();
    void start();
    int getSampleRate();

};


#endif //QMUSIC_AUDIOCODER_H
