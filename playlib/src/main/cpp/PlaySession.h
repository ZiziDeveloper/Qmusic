//
// Created by Administrator on 2018/12/15 0015.
//

#ifndef QMUSIC_PLAYSESSION_H
#define QMUSIC_PLAYSESSION_H

#include <libavutil/rational.h>

/**
 * 存储播放会话的状态
 */
const int PLAY_STATE_STOPPED = 1;
const int PLAY_STATE_PAUSED = 2;
const int PLAY_STATE_PLAYING = 3;

const int PLAY_CHANNEL_RIGHT = 0;
const int PLAY_CHANNEL_LEFT = 1;
const int PLAY_CHANNEL_STEREO = 2;
class PlaySession {
private:
    PlaySession();
    static PlaySession *p;
    char* pUrl;
public:
    bool bExit = false;
    bool bSeeking = false;
    bool bLoading = true;
    int volume = 85;
    int playState = PLAY_STATE_PLAYING;
    int channelLayout = 2;
    //播放器输出采样率
    int outSmapleRate = 44100;
    //输入音源的采样率
    int inSampleRate = 0;
    //音频时长
    int64_t duration = 0;
    AVRational timeBase;
    //当前播放时长
    double currentClock;
    //最近一次的播放时长
    double lastClock;
    //上报到应用层的最短时间间隔
    static const double TIME_INTERVAL = 0.1;
public:
    static PlaySession* getIns();

    char *getUrl() const;

    void setUrl(char *pUrl);
};


#endif //QMUSIC_PLAYSESSION_H
