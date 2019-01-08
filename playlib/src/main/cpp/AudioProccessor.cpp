//
// Created by Administrator on 2018/12/15 0015.
//

#include "AudioProccessor.h"

AudioProccessor::AudioProccessor() {
    pAudioCoder = new AudioCoder();
}

AudioProccessor::~AudioProccessor() {

}

void AudioProccessor::prepare() {
    if (NULL != pAudioCoder) {
        pAudioCoder->prepare();
    }
}

void* startPlayRunnable(void* data) {
    AudioProccessor* proccessor = (AudioProccessor*) data;
    proccessor->prepareSLEngien();
    proccessor->prepareSLOutputMixAndPlay();
    pthread_exit(&proccessor->startPlayThread);
}

void* startDecodeRunnable(void* data) {
    AudioProccessor* proccessor = (AudioProccessor*) data;
    if (NULL != proccessor->pAudioCoder) {
        proccessor->pAudioCoder->start();
    }
    pthread_exit(&proccessor->startDecodeThread);
}

void AudioProccessor::start() {
    pthread_create(&startDecodeThread, NULL, startDecodeRunnable, this);
    pthread_create(&startPlayThread,NULL, startPlayRunnable, this);
}

void AudioProccessor::pause() {

}

void AudioProccessor::resume() {

}

void AudioProccessor::stop() {

}

void AudioProccessor::seek(int64_t second) {

}

void AudioProccessor::setVolume(int percent) {

}

void AudioProccessor::switchChannel(int channel) {

}

void AudioProccessor::setPitch(float pitch) {

}

void AudioProccessor::setSpeed(float speed) {

}

bool AudioProccessor::prepareSLEngien() {
    //TODO[truyayong] 失败之后资源释放问题
    SLresult res;
    res = slCreateEngine(&engineObj, 0, 0, 0, 0, 0);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("slCreateEngine fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "slCreateEngine fail");
        return false;
    }
    res = (*engineObj)->Realize(engineObj, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("engineObj Realize fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "engineObj Realize fail");
        return false;
    }
    res = (*engineObj)->GetInterface(engineObj, SL_IID_ENGINE, &engineItf);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("engineObj GetInterface fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "engineObj GetInterface fail");
        return false;
    }
    return true;
}

bool AudioProccessor::prepareSLOutputMixAndPlay() {
    //TODO[truyayong] 失败之后资源释放问题
    SLresult res;
    const SLInterfaceID  mids[MIX_ITF_NUM] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean  mreq[MIX_ITF_NUM] = {SL_BOOLEAN_FALSE};
    res = (*engineItf)->CreateOutputMix(engineItf, &outputMixObj, MIX_ITF_NUM, mids, mreq);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("engineItf CreateOutputMix fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "engineItf CreateOutputMix fail");
        return false;
    }
    res = (*outputMixObj)->Realize(outputMixObj, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("outputMixObj Realize fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "outputMixObj Realize fail");
        return false;
    }
    res = (*outputMixObj)->GetInterface(outputMixObj
            , SL_IID_ENVIRONMENTALREVERB, &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("outputMixObj GetInterface fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "outputMixObj GetInterface fail");
        return false;
    }
    res = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
            outputMixEnvironmentalReverb, &reverbSettings);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("outputMixEnvironmentalReverb SetEnvironmentalReverbProperties fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "outputMixEnvironmentalReverb SetEnvironmentalReverbProperties fail");
        return false;
    }
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObj};
    SLDataSink audioSink = {&outputMix, 0};
    prepareSLPlay(audioSink);
    return true;
}

void methodBufferCallBack(SLAndroidSimpleBufferQueueItf bf, void * context) {
    AudioProccessor *pPlayer = (AudioProccessor*) context;
    if (pPlayer != NULL) {
        int dataSize = pPlayer->pAudioCoder->reSampleAudio((void **) &pPlayer->pOutBuf);
        (*pPlayer->pcmBufQueueItf)->Enqueue(pPlayer->pcmBufQueueItf, (char*)pPlayer->pOutBuf
                , dataSize);
    }
}

bool AudioProccessor::prepareSLPlay(SLDataSink &audioSink) {
    //TODO[truyayong] 失败之后资源释放问题
    //配置pcm格式
    SLDataLocator_AndroidSimpleBufferQueue androidQueue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,2};
    SLDataFormat_PCM pcm = {
            SL_DATAFORMAT_PCM,//播放pcm格式的数据
            2,//2个声道（立体声）
            adapterSLSampleRate(PlaySession::getIns()->outSmapleRate),//44100hz的频率
            SL_PCMSAMPLEFORMAT_FIXED_16,//位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,//和位数一致就行
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,//立体声（前左前右）
            SL_BYTEORDER_LITTLEENDIAN//结束标志
    };
    SLDataSource slDataSource = {&androidQueue, &pcm};

    SLresult res;
    const SLInterfaceID  ids[PLAY_ITF_NUM] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME, SL_IID_PLAYBACKRATE, SL_IID_MUTESOLO};
    const SLboolean req[PLAY_ITF_NUM] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    res = (*engineItf)->CreateAudioPlayer(engineItf, &pcmPlayObj, &slDataSource, &audioSink, PLAY_ITF_NUM, ids, req);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("engineItf CreateAudioPlayer fail code : %d str : %s", res, ErrUtil::errLog(res));
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "engineItf CreateAudioPlayer fail");
        return false;
    }
    res = (*pcmPlayObj)->Realize(pcmPlayObj, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("pcmPlayObj Realize fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "pcmPlayObj Realize fail");
        return false;
    }

    res = (*pcmPlayObj)->GetInterface(pcmPlayObj, SL_IID_PLAY, &pcmPlayItf);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("pcmPlayObj GetInterface pcmPlayItf fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "pcmPlayObj GetInterface pcmPlayItf fail");
        return false;
    }

    res = (*pcmPlayObj)->GetInterface(pcmPlayObj, SL_IID_VOLUME, &pcmVolumeItf);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("pcmPlayObj GetInterface pcmVolumeItf fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "pcmPlayObj GetInterface pcmVolumeItf fail");
        return false;
    }

    res = (*pcmPlayObj)->GetInterface(pcmPlayObj, SL_IID_MUTESOLO, &pcmMuteSoloItf);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("pcmPlayObj GetInterface pcmMuteSoloItf fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "pcmPlayObj GetInterface pcmMuteSoloItf fail");
        return false;
    }

    res = (*pcmPlayObj)->GetInterface(pcmPlayObj, SL_IID_BUFFERQUEUE, &pcmBufQueueItf);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("pcmPlayObj GetInterface pcmBufQueueItf fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "pcmPlayObj GetInterface pcmBufQueueItf fail");
        return false;
    }

    //TODO[truyayong] 设置缓冲回调接口
    res = (*pcmBufQueueItf)->RegisterCallback(pcmBufQueueItf, methodBufferCallBack, this);
    if (SL_RESULT_SUCCESS != res) {
        LOGE("pcmBufQueueItf RegisterCallback fail code : %d", res);
        NotifyApplication::getIns()->notifyError(CHILD_THREAD, res, "pcmBufQueueItf RegisterCallback fail");
        return false;
    }
    methodBufferCallBack(pcmBufQueueItf, this);

    //TODO[truyayong] 设置播放的初始状态 音量，声道，播放状态等
    setVolume(PlaySession::getIns()->volume);
    switchChannel(PlaySession::getIns()->channelLayout);
    setPlayState(PlaySession::getIns()->playState);
    return true;
}

void AudioProccessor::setPlayState(int state) {
    if (state == PLAY_STATE_STOPPED) {
        (*pcmPlayItf)->SetPlayState(pcmPlayItf, SL_PLAYSTATE_STOPPED);
    } else if (state == PLAY_STATE_PAUSED) {
        (*pcmPlayItf)->SetPlayState(pcmPlayItf, SL_PLAYSTATE_PAUSED);
    } else if (state == PLAY_STATE_PLAYING) {
        (*pcmPlayItf)->SetPlayState(pcmPlayItf, SL_PLAYSTATE_PLAYING);
    }
}

int AudioProccessor::adapterSLSampleRate(int sampleRate) {
    int rate = 0;
    switch (sampleRate)
    {
        case 8000:
            rate = SL_SAMPLINGRATE_8;
            break;
        case 11025:
            rate = SL_SAMPLINGRATE_11_025;
            break;
        case 12000:
            rate = SL_SAMPLINGRATE_12;
            break;
        case 16000:
            rate = SL_SAMPLINGRATE_16;
            break;
        case 22050:
            rate = SL_SAMPLINGRATE_22_05;
            break;
        case 24000:
            rate = SL_SAMPLINGRATE_24;
            break;
        case 32000:
            rate = SL_SAMPLINGRATE_32;
            break;
        case 44100:
            rate = SL_SAMPLINGRATE_44_1;
            break;
        case 48000:
            rate = SL_SAMPLINGRATE_48;
            break;
        case 64000:
            rate = SL_SAMPLINGRATE_64;
            break;
        case 88200:
            rate = SL_SAMPLINGRATE_88_2;
            break;
        case 96000:
            rate = SL_SAMPLINGRATE_96;
            break;
        case 192000:
            rate = SL_SAMPLINGRATE_192;
            break;
        default:
            rate =  SL_SAMPLINGRATE_44_1;
    }
    return rate;
}
