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
 * 播放期间的状态session
 */
class PlaySession {
private:
    PlaySession();
    ~PlaySession();
    PlaySession(const PlaySession&);
    PlaySession& operator=(const PlaySession&);
    char* pUrl;
public:
    /**
     * 是否已经停止播放
     */
    bool bExit;
    /**
     * 是否正在拖动播放进度
     */
    bool bSeeking;
    /**
     * 是否正在加载
     */
    bool bLoading;
    /**
     * 音量
     */
    int volume;
    /**
     * 播放状态
     */
    int playState;
    /**
     * 输出音频的声道布局
     */
    int64_t outChannelLayout;
    /**
     * 输入音频的声道布局
     */
    int64_t inChannelLayout;

    /**
     * 输出采样格式
     */
    AVSampleFormat outFmt;
    /**
     * 输入采样格式
     */
    AVSampleFormat inFmt;
    /**
     * 播放器输出采样率
     */
    int outSmapleRate;
    /**
     * 输入音源的采样率
     */
    int inSampleRate;
    /**
     * 一帧包含的样本数
     */
    int numSampleAvFrame;

    /**
     * 音频时长
     */
    int64_t duration;
    AVRational timeBase;
    /**
     * 当前播放时长
     */
    double currentClock;
    /**
     * 最近一次的播放时长
     */
    double lastClock;

    /**
     * 播放音调
     */
    float pitch;
    /**
     * 播放速度
     */
    float speed;

    /**
     * 上报到应用层的最短时间间隔
     */
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
    static PlaySession& getIns();

    char *getUrl() const;

    void allocUrl(char *pUrl, int length);

    //获取音频布局不同所需要的byte数
    int getInChannelLayoutBytes();

    int getoutChannelLayoutBytes();
};


#endif //QMUSIC_PLAYSESSION_H
