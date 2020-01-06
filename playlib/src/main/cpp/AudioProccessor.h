//
// Created by Administrator on 2018/12/15 0015.
//
#pragma once
#ifndef QMUSIC_AUDIOPROCCESSOR_H
#define QMUSIC_AUDIOPROCCESSOR_H

#include <stdint.h>
#include "AudioCoder.h"
#define MIX_ITF_NUM 1
#define PLAY_ITF_NUM 4

#include <stdint.h>
#include "AndroidLog.h"
#include "NotifyApplication.h"
#include "SoundTouch.h"
#include <memory>
extern "C" {
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};
using namespace soundtouch;
/**
 * 音频处理层，负责播放音频与调度AudioCoder解码  音频播放使用openSL,解码用ffmpeg,音频特效处理使用的是SoundTouch
 * [todo]qiuyayong之后需要考虑接入硬件解码
 */
class AudioProccessor {
private:
    //opensl 引擎
    SLObjectItf engineObj = NULL;
    SLEngineItf engineItf = NULL;

    //混音器
    SLObjectItf  outputMixObj = NULL;
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

    //opensl 播放pcm接口
    SLObjectItf pcmPlayObj = NULL;
    SLPlayItf  pcmPlayItf = NULL;
    SLVolumeItf pcmVolumeItf = NULL;
    SLMuteSoloItf  pcmMuteSoloItf = NULL;


public:
    std::shared_ptr<AudioCoder> pAudioCoder;
    //开始播放线程
    pthread_t startPlayThread;
    pthread_t startDecodeThread;
    pthread_mutex_t adapterPcmMutex;
    //播放缓冲队列
    SLAndroidSimpleBufferQueueItf  pcmBufQueueItf = NULL;
    uint8_t *pOutBuf = NULL;

    SoundTouch* soundTouch = NULL;
    SAMPLETYPE* soundTouchBuffer = NULL;

private:
    void setPlayState(int state);
    int adapterSLSampleRate(int rate);
    //释放OpenSL
    void releaseSL();

    //申请SoundTouch资源
    void allocSoundTouch();
    //释放SoundTouch资源
    void freeSoundTouch();
public:
    AudioProccessor();
    virtual ~AudioProccessor();

    void prepare();
    void start();
    void pause();
    void resume();
    void stop();
    void seek(int64_t second);
    void setVolume(int percent);
    void switchChannel(int64_t channel);
    void setPitch(float pitch);
    void setSpeed(float speed);

    //创建引擎
    bool prepareSLEngien();
    //创建混音器和播放器,用于输出音频
    bool prepareSLOutputMixAndPlay();
    //创建播放器
    bool prepareSLPlay(SLDataSink& audioSink);

    /**
     * 将从frame中pcm数据转成适配SoundTouch格式
     * @return
     */
    int adapterPcmToSoundTouch();

};


#endif //QMUSIC_AUDIOPROCCESSOR_H
