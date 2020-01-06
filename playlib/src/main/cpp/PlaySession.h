//
// Created by Administrator on 2018/12/15 0015.
//

#ifndef QMUSIC_PLAYSESSION_H
#define QMUSIC_PLAYSESSION_H
extern "C" {
#include <libavutil/rational.h>
#include <libavutil/samplefmt.h>
};

/**
 * 存储播放会话的状态
 */
const int PLAY_STATE_STOPPED = 1;
const int PLAY_STATE_PAUSED = 2;
const int PLAY_STATE_PLAYING = 3;

const int64_t PLAY_CHANNEL_RIGHT = 0x00000002;
const int64_t PLAY_CHANNEL_LEFT = 0x00000001;
const int64_t PLAY_CHANNEL_STEREO = PLAY_CHANNEL_LEFT | PLAY_CHANNEL_RIGHT;
class PlaySession {
private:
    PlaySession();
    static PlaySession *p;
    char* pUrl = NULL;
public:
    bool bExit = false;
    bool bSeeking = false;
    bool bLoading = true;
    int volume = 85;
    int playState = PLAY_STATE_PLAYING;
    int64_t outChannelLayout = PLAY_CHANNEL_STEREO;
    int64_t inChannelLayout = 0;
    AVSampleFormat outFmt = AV_SAMPLE_FMT_S16;
    AVSampleFormat inFmt = AV_SAMPLE_FMT_S16;
    //播放器输出采样率
    int outSmapleRate = 44100;
    //输入音源的采样率
    int inSampleRate = 0;
    //一帧包含的样本数
    int numSampleAvFrame = 0;

    //音频时长
    int64_t duration = 0;
    AVRational timeBase;
    //当前播放时长
    double currentClock = 0;
    //最近一次的播放时长
    double lastClock = 0;
    //上报到应用层的最短时间间隔
    static constexpr double TIME_INTERVAL = 0.1;

    float pitch = 1.0f;
    float speed = 1.0f;
private:
    void releaseUrl();
public:
    static PlaySession* getIns();

    char *getUrl() const;

    void allocUrl(char *pUrl, int length);

    //获取音频布局不同所需要的byte数
    int getInChannelLayoutBytes();

    int getoutChannelLayoutBytes();
};


#endif //QMUSIC_PLAYSESSION_H
