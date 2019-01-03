//
// Created by Administrator on 2018/12/15 0015.
//

#include "AudioPlayer.h"

AudioPlayer::AudioPlayer(AudioProccessor* proccessor) {
    pAudioProccessor = proccessor;
}

AudioPlayer::~AudioPlayer() {
    pAudioProccessor = NULL;
}

bool AudioPlayer::prepare() {
    //创建引擎
    if (!prepareSLEngien()) {
        return false;
    }

    //创建混音器,用于输出音频
    if (!prepareSLOutputMix()) {
        return false;
    }
    //创建播放器
    if (!prepareSLPlay()) {
        return false;
    }
    return true;
}

void AudioPlayer::start() {

}

void AudioPlayer::resume() {

}

void AudioPlayer::puase() {

}

void AudioPlayer::stop() {

}

void AudioPlayer::seek(int64_t second) {

}

void AudioPlayer::setVolume(int percent) {

}

void AudioPlayer::switchChannel(int channel) {

}

void AudioPlayer::setPitch(float pitch) {

}

void AudioPlayer::setSpeed(float speed) {

}

bool AudioPlayer::prepareSLEngien() {
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

bool AudioPlayer::prepareSLOutputMix() {
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
    audioSink = {&outputMix, 0};
    return true;
}

void methodBufferCallBack(SLAndroidSimpleBufferQueueItf bf, void * context) {
    AudioPlayer *pPlayer = (AudioPlayer*) context;
    if (pPlayer != NULL) {
        int dataSize = pPlayer->pAudioProccessor->pAudioCoder->reSampleAudio((void **) &pPlayer->pOutBuf);
        (*pPlayer->pcmBufQueueItf)->Enqueue(pPlayer->pcmBufQueueItf, (char*)pPlayer->pOutBuf
                , dataSize * 2 * 2);
    }
}

bool AudioPlayer::prepareSLPlay() {
    //TODO[truyayong] 失败之后资源释放问题
    //配置pcm格式
    SLDataLocator_AndroidSimpleBufferQueue androidQueue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,2};
    SLDataFormat_PCM pcm = {
            SL_DATAFORMAT_PCM,//播放pcm格式的数据
            2,//2个声道（立体声）
            44100,//44100hz的频率 TODO[truyayong] 写成一个通用接口
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
        LOGE("engineItf CreateAudioPlayer fail code : %d", res);
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
    return true;
}
