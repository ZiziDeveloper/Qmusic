//
// Created by Administrator on 2018/12/15 0015.
//


#include "AudioCoder.h"

AudioCoder::AudioCoder() {
    pthread_mutex_init(&prepareDecodeMutex, NULL);
    pQueue = new PacketQueue();
}

AudioCoder::~AudioCoder() {
    pQueue->clearQueue();
    if (NULL != buffer) {
        av_free(buffer);
        buffer = NULL;
    }
    pthread_mutex_destroy(&prepareDecodeMutex);
}

void *decodePrepareRunnable(void* data) {
    AudioCoder* pCoder = (AudioCoder*) data;
    pCoder->prepareDecoder();
    pthread_exit(&pCoder->prepareDecodeThread);
}

void AudioCoder::prepare() {
    LOGI("AudioCoder::prepare");
    pthread_create(&prepareDecodeThread
            , NULL, decodePrepareRunnable, this);
}

void AudioCoder::prepareDecoder() {
    LOGI("AudioCoder::prepareDecoder");
    pthread_mutex_lock(&prepareDecodeMutex);
    av_register_all();
    avformat_network_init();
    pAVFormatCtx = avformat_alloc_context();

    int ret = avformat_open_input(&pAVFormatCtx
            , PlaySession::getIns()->getUrl(), NULL, NULL);
    if (ret != 0) {
        LOGE("AudioCoder::prepareDecoder avformat_open_input err : %s, url : %s", ErrUtil::errLog(ret), PlaySession::getIns()->getUrl());
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
            PlaySession::getIns()->duration = pAVFormatCtx->duration / AV_TIME_BASE;
            PlaySession::getIns()->timeBase = pAVFormatCtx->streams[i]->time_base;
            PlaySession::getIns()->inSampleRate = pCodecPara->sample_rate;
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

    ret = avcodec_parameters_to_context(pAVCodecCtx, pCodecPara);
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
    int count = 0;
    LOGI("AudioCoder::start");
    while (!PlaySession::getIns()->bExit) {
        if (PlaySession::getIns()->bSeeking) {
            av_usleep(1000 * 100);
            continue;
        }
        if (pQueue->size() > PacketQueue::MAX_SIZE) {
            av_usleep(1000 * 100);
            continue;
        }
        AVPacket* avPacket = av_packet_alloc();
        int ret = av_read_frame(pAVFormatCtx, avPacket);
        if (ret == 0) {
            if (avPacket->stream_index == mStreamIndex) {
                count++;
                pQueue->putAvPacket(avPacket);
            } else {
                av_packet_free(&avPacket);
                av_free(avPacket);
            }
        } else {
            LOGI("decode finish");
            av_packet_free(&avPacket);
            av_free(avPacket);
            while (!PlaySession::getIns()->bExit) {
                if (pQueue->size() > 0) {
                    av_usleep(1000 * 100);
                    continue;
                } else {
                    PlaySession::getIns()->bExit = true;
                    break;
                }
            }
        }
    }
}

int AudioCoder::getSampleRate() {
    return 0;
}

int AudioCoder::reSampleAudio(void **pcmBuf) {
    int ret;
    int dataSize = 0;
    AVPacket* avPacket = NULL;
    AVFrame* avFrame = NULL;
    while (!PlaySession::getIns()->bExit) {
        if (PlaySession::getIns()->bSeeking) {
            av_usleep(1000 * 100);
            continue;
        }

        if (pQueue->size() == 0) {
            if (!PlaySession::getIns()->bLoading) {
                PlaySession::getIns()->bLoading = true;
                NotifyApplication::getIns()->notifyLoad(true);
            }
            av_usleep(1000 * 100);
            continue;
        } else {
            if (PlaySession::getIns()->bLoading) {
                PlaySession::getIns()->bLoading = false;
                NotifyApplication::getIns()->notifyLoad(false);
            }
        }

        if (bReadFrameOver) {
            avPacket = av_packet_alloc();
            if (pQueue->getAvPacket(avPacket) != 0) {
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;
                continue;
            }
            ret = avcodec_send_packet(pAVCodecCtx, avPacket);
            if (ret != 0) {
                LOGE("avcodec_send_packet ret : %d err: %s", ret, ErrUtil::errLog(ret));
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;
                continue;
            }
        }
        avFrame = av_frame_alloc();
        ret = avcodec_receive_frame(pAVCodecCtx, avFrame);
        if (ret == 0) {
            bReadFrameOver = false;
            if (avFrame->channels > 0 && avFrame->channel_layout == 0) {
                avFrame->channel_layout = av_get_default_channel_layout(avFrame->channel_layout);
            } else if (avFrame->channels == 0 && avFrame->channel_layout > 0) {
                avFrame->channels = av_get_channel_layout_nb_channels(avFrame->channel_layout);
            }
            PlaySession::getIns()->inChannelLayout = avFrame->channel_layout;
            PlaySession::getIns()->inFmt = (AVSampleFormat)avFrame->format;
            SwrContext* pSwrCtx;
            pSwrCtx = swr_alloc_set_opts(
                    NULL,
                    AV_CH_LAYOUT_STEREO,
                    PlaySession::getIns()->outFmt,
                    avFrame->sample_rate,
                    PlaySession::getIns()->inChannelLayout,
                    PlaySession::getIns()->inFmt,
                    avFrame->sample_rate,
                    NULL, NULL
            );
            if (!pSwrCtx || swr_init(pSwrCtx) < 0) {
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;
                av_frame_free(&avFrame);
                av_free(avFrame);
                avFrame = NULL;
                swr_free(&pSwrCtx);
                bReadFrameOver = true;
                continue;
            }

            if (NULL == buffer) {
                buffer = (uint8_t*) av_malloc(PlaySession::getIns()->inSampleRate
                                              * PlaySession::getIns()->getInChannelLayoutBytes()
                                              * av_get_bytes_per_sample(PlaySession::getIns()->outFmt));
            }
            PlaySession::getIns()->numSampleAvFrame = swr_convert(
                    pSwrCtx, &buffer,
                    avFrame->nb_samples,
                    (const uint8_t **)avFrame->data,
                    avFrame->nb_samples);
            *pcmBuf = buffer;
            int outChannels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
            dataSize = PlaySession::getIns()->numSampleAvFrame * outChannels * av_get_bytes_per_sample(PlaySession::getIns()->outFmt);

            double time = avFrame->pts * av_q2d(PlaySession::getIns()->timeBase);
            calcCurrentClock(time);

            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            swr_free(&pSwrCtx);
            break;
        } else {
            bReadFrameOver = true;
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            continue;
        }
    }
    return dataSize;
}

void AudioCoder::stop() {
    LOGI("AudioCoder::stop");
    if (NULL != pQueue && pQueue->size() > 0) {
        pQueue->clearQueue();
    }
}

void AudioCoder::seek(int64_t second) {
    LOGI("AudioCoder::seek second : %ld duration : %ld", second, PlaySession::getIns()->duration);
    if (PlaySession::getIns()->duration < 0) {
        return;
    }
    if (second >= 0 && second <= PlaySession::getIns()->duration) {
        PlaySession::getIns()->bSeeking = true;
        if (NULL != pQueue) {
            pQueue->clearQueue();
        }
        int64_t rel = second * AV_TIME_BASE;
        avcodec_flush_buffers(pAVCodecCtx);
        avformat_seek_file(pAVFormatCtx, -1, INT64_MIN, rel, INT64_MAX, 0);
        PlaySession::getIns()->bSeeking = false;
    }
}

void AudioCoder::calcCurrentClock(double time) {
    if (time < PlaySession::getIns()->currentClock) {
        time = PlaySession::getIns()->currentClock;
    }
    PlaySession::getIns()->currentClock = time;
}
