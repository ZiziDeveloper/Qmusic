//
// Created by Administrator on 2018/12/15 0015.
//

#ifndef QMUSIC_AUDIOPROCCESSOR_H
#define QMUSIC_AUDIOPROCCESSOR_H

#include <stdint.h>
#include "AudioCoder.h"
#include "AudioPlayer.h"

/**
 * 音频处理层，调度AudioCoder与AudioPlayer
 */
class AudioProccessor {
private:
    AudioCoder *pAudioCoder;
    AudioPlayer *pAudioPlayer;
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
    void switchChannel(int channel);
    void setPitch(float pitch);
    void setSpeed(float speed);

};


#endif //QMUSIC_AUDIOPROCCESSOR_H
