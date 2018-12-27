//
// Created by Administrator on 2018/12/15 0015.
//

#ifndef QMUSIC_AUDIOPLAYER_H
#define QMUSIC_AUDIOPLAYER_H

#define MIX_ITF_NUM 1
#define PLAY_ITF_NUM 4

#include <stdint.h>
#include "AndroidLog.h"
#include "NotifyApplication.h"

extern "C" {
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
};
/**
 * 音频播放层
 */
class AudioPlayer {
private:
    //opensl 引擎
    SLObjectItf engineObj = NULL;
    SLEngineItf engineItf = NULL;
    SLDataSink audioSink;

    //混音器
    SLObjectItf  outputMixObj = NULL;
    SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    SLEnvironmentalReverbSettings reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

    //opensl 播放pcm接口
    SLObjectItf pcmPlayObj = NULL;
    SLPlayItf  pcmPlayItf = NULL;
    SLVolumeItf pcmVolumeItf = NULL;
    SLMuteSoloItf  pcmMuteSoloItf = NULL;

    //播放缓冲队列
    SLAndroidSimpleBufferQueueItf  pcmBufQueueItf = NULL;
private:
    //创建引擎
    bool prepareSLEngien();
    //创建混音器,用于输出音频
    bool prepareSLOutputMix();
    //创建播放器
    bool prepareSLPlay();
public:
    AudioPlayer();
    virtual ~AudioPlayer();

    bool prepare();
    void start();
    void resume();
    void puase();
    void stop();
    void seek(int64_t second);
    void setVolume(int percent);
    void switchChannel(int channel);
    void setPitch(float pitch);
    void setSpeed(float speed);

};


#endif //QMUSIC_AUDIOPLAYER_H
