//
// Created by Administrator on 2018/12/15 0015.
//

#include "AudioProccessor.h"

AudioProccessor::AudioProccessor() : reverbSettings(SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR), pAudioCoder(new AudioCoder) {
    engineObj = nullptr;
    engineItf = nullptr;

    outputMixObj = nullptr;
    outputMixEnvironmentalReverb = nullptr;

    pcmPlayObj = nullptr;
    pcmPlayItf = nullptr;
    pcmVolumeItf = nullptr;
    pcmMuteSoloItf = nullptr;


    pcmBufQueueItf = nullptr;
    pOutBuf = nullptr;

    pthread_mutex_init(&adapterPcmMutex, nullptr);
}

AudioProccessor::~AudioProccessor() {
    pthread_mutex_destroy(&adapterPcmMutex);
}

void AudioProccessor::prepare() {
    LOGI("AudioProccessor::prepare");
    if (nullptr != pAudioCoder) {
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
    if (nullptr != proccessor->pAudioCoder) {
        proccessor->pAudioCoder->start();
    }
    pthread_exit(&proccessor->startDecodeThread);
}

void AudioProccessor::start() {
    LOGI("AudioProccessor::start");

    allocSoundTouch();
    PlaySession::getIns().bExit = false;
    PlaySession::getIns().playState = PlaySession::PLAY_STATE_PLAYING;
    pthread_create(&startDecodeThread, nullptr, startDecodeRunnable, this);
    pthread_create(&startPlayThread, nullptr, startPlayRunnable, this);
}

void AudioProccessor::pause() {
    LOGI("AudioProccessor::pause");
    PlaySession::getIns().playState = PlaySession::PLAY_STATE_PAUSED;
    setPlayState(PlaySession::getIns().playState);
}

void AudioProccessor::resume() {
    LOGI("AudioProccessor::resume");
    PlaySession::getIns().playState = PlaySession::PLAY_STATE_PLAYING;
    setPlayState(PlaySession::getIns().playState);
}

void AudioProccessor::stop() {
    LOGI("AudioProccessor::stop");
    PlaySession::getIns().bExit = true;
    PlaySession::getIns().playState = PlaySession::PLAY_STATE_STOPPED;
    setPlayState(PlaySession::getIns().playState);
    if (nullptr != pAudioCoder) {
        pAudioCoder->stop();
    }
    releaseSL();
    freeSoundTouch();
}

void AudioProccessor::seek(int64_t second) {
    if (nullptr != pAudioCoder) {
        pAudioCoder->seek(second);
    }
}

void AudioProccessor::setVolume(int percent) {
    LOGI("AudioProccessor::setVolume percent : %d", percent);
    if (nullptr == pcmVolumeItf) {
        return;
    }
    PlaySession::getIns().volume = percent;
    if (percent > 30)
    {
        (*pcmVolumeItf)->SetVolumeLevel(pcmVolumeItf, (100 - percent) * -20);
    }
    else if (percent > 25)
    {
        (*pcmVolumeItf)->SetVolumeLevel(pcmVolumeItf, (100 - percent) * -22);
    }
    else if (percent > 20)
    {
        (*pcmVolumeItf)->SetVolumeLevel(pcmVolumeItf, (100 - percent) * -25);
    }
    else if (percent > 15)
    {
        (*pcmVolumeItf)->SetVolumeLevel(pcmVolumeItf, (100 - percent) * -28);
    }
    else if (percent > 10)
    {
        (*pcmVolumeItf)->SetVolumeLevel(pcmVolumeItf, (100 - percent) * -30);
    }
    else if (percent > 5)
    {
        (*pcmVolumeItf)->SetVolumeLevel(pcmVolumeItf, (100 - percent) * -34);
    }
    else if (percent > 3)
    {
        (*pcmVolumeItf)->SetVolumeLevel(pcmVolumeItf, (100 - percent) * -37);
    }
    else if (percent > 0)
    {
        (*pcmVolumeItf)->SetVolumeLevel(pcmVolumeItf, (100 - percent) * -40);
    }
    else {
        (*pcmVolumeItf)->SetVolumeLevel(pcmVolumeItf, (100 - percent) * -100);
    }
}

void AudioProccessor::switchChannel(int64_t channel) {
    LOGI("AudioProccessor::switchChannel");
    if (nullptr == pcmMuteSoloItf) {
        return;
    }
    if (channel == PlaySession::PLAY_CHANNEL_RIGHT) {//右声道
        (*pcmMuteSoloItf)->SetChannelMute(pcmMuteSoloItf, 1, false);
        (*pcmMuteSoloItf)->SetChannelMute(pcmMuteSoloItf, 0, true);
    } else if (channel == PlaySession::PLAY_CHANNEL_LEFT) {//左声道
        (*pcmMuteSoloItf)->SetChannelMute(pcmMuteSoloItf, 1, true);
        (*pcmMuteSoloItf)->SetChannelMute(pcmMuteSoloItf, 0, false);
    } else if (channel == PlaySession::PLAY_CHANNEL_STEREO) {//立体声
        (*pcmMuteSoloItf)->SetChannelMute(pcmMuteSoloItf, 1, false);
        (*pcmMuteSoloItf)->SetChannelMute(pcmMuteSoloItf, 0, false);
    }
}

void AudioProccessor::setPitch(float pitch) {
    if (nullptr != soundTouch) {
        PlaySession::getIns().pitch = pitch;
        soundTouch->setPitch(PlaySession::getIns().pitch);
    }
}

void AudioProccessor::setSpeed(float speed) {
    if (nullptr != soundTouch) {
        PlaySession::getIns().speed = speed;
        soundTouch->setTempo(PlaySession::getIns().speed);
    }
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
    if (nullptr != pPlayer) {
        int soundTouchReceiveNum = pPlayer->adapterPcmToSoundTouch();
        if (soundTouchReceiveNum > 0) {
            PlaySession::getIns().currentClock += soundTouchReceiveNum / (double)(PlaySession::getIns().inSampleRate * 2 * 2);
            if (PlaySession::getIns().currentClock - PlaySession::getIns().lastClock >= PlaySession::TIME_INTERVAL) {
                PlaySession::getIns().lastClock = PlaySession::getIns().currentClock;
                //TODO[truyayong] 时间回调到应用层
//                LOGE("[truyayong] current : %f, tol : %d", PlaySession::getIns()->currentClock
//                , PlaySession::getIns()->duration);
                NotifyApplication::getIns()->notifyProgress(CHILD_THREAD, PlaySession::getIns().currentClock, PlaySession::getIns().duration);
            }
        }
        (*pPlayer->pcmBufQueueItf)->Enqueue(pPlayer->pcmBufQueueItf, (char*)pPlayer->soundTouchBuffer
                , soundTouchReceiveNum * (PlaySession::getIns().getoutChannelLayoutBytes()
                                          * av_get_bytes_per_sample(PlaySession::getIns().outFmt)));
    }
}

int AudioProccessor::adapterPcmToSoundTouch() {
    pthread_mutex_lock(&adapterPcmMutex);
    bool finished = true;
    int pcmSize = 0;
    int receiveNum;
    while(!PlaySession::getIns().bExit) {
        pOutBuf = nullptr;
        if (finished) {
            pcmSize = pAudioCoder->reSampleAudio((void **)&pOutBuf);
            if (pcmSize > 0) {
                for (int i = 0; i < pcmSize / 2 + 1; i++) {
                    soundTouchBuffer[i] = (pOutBuf[i * 2] | ((pOutBuf[i * 2 + 1]) << 8));
                }
                soundTouch->putSamples(soundTouchBuffer, PlaySession::getIns().numSampleAvFrame);
                receiveNum = soundTouch->receiveSamples(
                        soundTouchBuffer, pcmSize / (PlaySession::getIns().getoutChannelLayoutBytes()
                                                     * av_get_bytes_per_sample(PlaySession::getIns().outFmt)));
            } else {
                soundTouch->flush();
            }

            if (receiveNum == 0) {
                finished = true;
                continue;
            } else {
                if (nullptr == pOutBuf) {
                    receiveNum = soundTouch->receiveSamples(
                            soundTouchBuffer, pcmSize / (PlaySession::getIns().getoutChannelLayoutBytes()
                                                         * av_get_bytes_per_sample(PlaySession::getIns().outFmt)));
                    if (receiveNum == 0) {
                        finished = true;
                        continue;
                    }
                }
                pthread_mutex_unlock(&adapterPcmMutex);
                return receiveNum;
            }
        }

    }
    pthread_mutex_unlock(&adapterPcmMutex);
    return 0;
}

bool AudioProccessor::prepareSLPlay(SLDataSink &audioSink) {
    //TODO[truyayong] 失败之后资源释放问题
    //配置pcm格式
    SLDataLocator_AndroidSimpleBufferQueue androidQueue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,2};
    SLDataFormat_PCM pcm = {
            SL_DATAFORMAT_PCM,//播放pcm格式的数据
            2,//2个声道（立体声）
            static_cast<SLuint32>(adapterSLSampleRate(PlaySession::getIns().outSmapleRate)),//44100hz的频率
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
    setVolume(PlaySession::getIns().volume);
    switchChannel(PlaySession::getIns().outChannelLayout);
    setPlayState(PlaySession::getIns().playState);
    NotifyApplication::getIns()->notifyStarted(CHILD_THREAD);
    return true;
}

void AudioProccessor::setPlayState(int state) {
    if (nullptr == pcmPlayItf) {
        return;
    }
    if (state == PlaySession::PLAY_STATE_STOPPED) {
        (*pcmPlayItf)->SetPlayState(pcmPlayItf, SL_PLAYSTATE_STOPPED);
    } else if (state == PlaySession::PLAY_STATE_PAUSED) {
        (*pcmPlayItf)->SetPlayState(pcmPlayItf, SL_PLAYSTATE_PAUSED);
    } else if (state == PlaySession::PLAY_STATE_PLAYING) {
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

void AudioProccessor::releaseSL() {
    if (nullptr != pcmPlayObj) {
        (*pcmPlayObj)->Destroy(pcmPlayObj);
        pcmPlayObj = nullptr;
        pcmPlayItf = nullptr;
        pcmBufQueueItf = nullptr;
        pcmMuteSoloItf = nullptr;
        pcmVolumeItf = nullptr;
    }

    if (nullptr != outputMixObj) {
        (*outputMixObj)->Destroy(outputMixObj);
        outputMixObj = nullptr;
        outputMixEnvironmentalReverb = nullptr;
    }

    if (nullptr != engineObj) {
        (*engineObj)->Destroy(engineObj);
        engineObj = nullptr;
        engineItf = nullptr;
    }

    if (nullptr != pOutBuf) {
        pOutBuf = nullptr;
    }
}

void AudioProccessor::allocSoundTouch() {
    soundTouch.reset(new SoundTouch());
    soundTouch->setSampleRate(PlaySession::getIns().outSmapleRate);
    soundTouch->setChannels(PlaySession::getIns().getoutChannelLayoutBytes());
    soundTouch->setPitch(PlaySession::getIns().pitch);
    soundTouch->setTempo(PlaySession::getIns().speed);
    soundTouchBuffer = (SAMPLETYPE*) av_malloc(
            PlaySession::getIns().outSmapleRate
            * PlaySession::getIns().getoutChannelLayoutBytes() * av_get_bytes_per_sample(PlaySession::getIns().outFmt));
}

void AudioProccessor::freeSoundTouch() {
    if (nullptr != soundTouchBuffer) {
        av_free(soundTouchBuffer);
        soundTouchBuffer = nullptr;
    }
}


