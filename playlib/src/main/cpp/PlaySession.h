//
// Created by Administrator on 2018/12/15 0015.
//

#ifndef QMUSIC_PLAYSESSION_H
#define QMUSIC_PLAYSESSION_H
extern "C" {
#include <libavutil/rational.h>
#include <libavutil/samplefmt.h>
};

class PlaySession {
private:
    PlaySession();
    static PlaySession *p;
    char* pUrl;
public:
    bool bExit;
    bool bSeeking;
    bool bLoading;
    int volume;
    int playState;
    int64_t outChannelLayout;
    int64_t inChannelLayout;
    AVSampleFormat outFmt;
    AVSampleFormat inFmt;
    //播放器输出采样率
    int outSmapleRate;
    //输入音源的采样率
    int inSampleRate;
    //一帧包含的样本数
    int numSampleAvFrame;

    //音频时长
    int64_t duration;
    AVRational timeBase;
    //当前播放时长
    double currentClock;
    //最近一次的播放时长
    double lastClock;

    float pitch;
    float speed;

    //上报到应用层的最短时间间隔
    static constexpr double TIME_INTERVAL = 0.1;

    /**
     * 存储播放会话的状态
     */
    static const int PLAY_STATE_STOPPED = 1;
    static const int PLAY_STATE_PAUSED = 2;
    static const int PLAY_STATE_PLAYING = 3;

    static const int64_t PLAY_CHANNEL_RIGHT = 0x00000002;
    static const int64_t PLAY_CHANNEL_LEFT = 0x00000001;
    static const int64_t PLAY_CHANNEL_STEREO = PLAY_CHANNEL_LEFT | PLAY_CHANNEL_RIGHT;
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
