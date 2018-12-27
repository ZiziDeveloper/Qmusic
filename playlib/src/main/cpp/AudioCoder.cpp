//
// Created by Administrator on 2018/12/15 0015.
//

#include "AudioCoder.h"

AudioCoder::AudioCoder() {
    pthread_mutex_init(&prepareDecodeMutex, NULL);
}

AudioCoder::~AudioCoder() {
    pthread_mutex_destroy(&prepareDecodeMutex);
}

void *decodePrepareRunnable(void* data) {
    AudioCoder* pCoder = (AudioCoder*) data;
    pCoder->prepareDecoder();
    pthread_exit(&pCoder->prepareDecodeThread);
}

void AudioCoder::prepare() {
    pthread_create(&prepareDecodeThread
            , NULL, decodePrepareRunnable, this);
}

void AudioCoder::prepareDecoder() {
    pthread_mutex_lock(&prepareDecodeMutex);
    av_register_all();
    avformat_network_init();
    pAVFormatCtx = avformat_alloc_context();

    int ret = avformat_open_input(&pAVFormatCtx
            , PlaySession::getIns()->getUrl(), NULL, NULL);
    if (ret != 0) {
        LOGE("AudioCoder::prepareDecoder avformat_open_input err : %s", ErrUtil::errLog(ret));
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, ret, ErrUtil::errLog(ret));
        PlaySession::getIns()->bExit = true;
        pthread_mutex_unlock(&prepareDecodeMutex);
        return;
    }

    ret = avformat_find_stream_info(pAVFormatCtx, NULL);
    if (ret < 0) {
        LOGE("AudioCoder::prepareDecoder avformat_find_stream_info err : %s", ErrUtil::errLog(ret));
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, ret, ErrUtil::errLog(ret));
        PlaySession::getIns()->bExit = true;
        pthread_mutex_unlock(&prepareDecodeMutex);
        return;
    }

    for (int i = 0; i < pAVFormatCtx->nb_streams; i++) {
        if (pAVFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            mStreamIndex = i;
            pCodecPara = pAVFormatCtx->streams[i]->codecpar;
        }
    }

    AVCodec* codec = avcodec_find_decoder(pCodecPara->codec_id);
    if (!codec) {
        LOGE("AudioCoder::prepareDecoder avcodec_find_decoder is null ");
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, ret, ErrUtil::errLog(ret));
        PlaySession::getIns()->bExit = true;
        pthread_mutex_unlock(&prepareDecodeMutex);
        return;
    }

    pAVCodecCtx = avcodec_alloc_context3(codec);
    if (!pAVCodecCtx) {
        LOGE("AudioCoder::prepareDecoder avcodec_alloc_context3 is null ");
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, ret, ErrUtil::errLog(ret));
        PlaySession::getIns()->bExit = true;
        pthread_mutex_unlock(&prepareDecodeMutex);
        return;
    }

    ret = avcodec_parameters_from_context(pCodecPara, pAVCodecCtx);
    if (ret < 0) {
        LOGE("AudioCoder::prepareDecoder avcodec_parameters_from_context err : %s", ErrUtil::errLog(ret));
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, ret, ErrUtil::errLog(ret));
        PlaySession::getIns()->bExit = true;
        pthread_mutex_unlock(&prepareDecodeMutex);
        return;
    }

    ret = avcodec_open2(pAVCodecCtx, codec, NULL);
    if (ret < 0) {
        LOGE("AudioCoder::prepareDecoder avcodec_open2 err : %s", ErrUtil::errLog(ret));
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, ret, ErrUtil::errLog(ret));
        PlaySession::getIns()->bExit = true;
        pthread_mutex_unlock(&prepareDecodeMutex);
        return;
    }
    pthread_mutex_unlock(&prepareDecodeMutex);
    NotifyApplication::getIns()->notifyPrepared(CHILD_THREAD);
}

void AudioCoder::start() {

}

int AudioCoder::getSampleRate() {
    return 0;
}
