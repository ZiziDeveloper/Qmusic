//
// Created by Administrator on 2018/12/15 0015.
//

#ifndef QMUSIC_PLAYSESSION_H
#define QMUSIC_PLAYSESSION_H

/**
 * 存储播放会话的状态
 */
const int PLAY_STATE_STOPPED = 1;
const int PLAY_STATE_PAUSED = 2;
const int PLAY_STATE_PLAYING = 3;
class PlaySession {
private:
    PlaySession();
    static PlaySession *p;
    char* pUrl;
public:
    bool bExit = false;
    bool bSeeking = false;
    bool bLoading = true;
    int volume = 70;
    int playState = PLAY_STATE_PLAYING;
    int channelLayout = 2;
    //播放器输出采样率
    int outSmapleRate = 44100;
public:
    static PlaySession* getIns();

    char *getUrl() const;

    void setUrl(char *pUrl);
};


#endif //QMUSIC_PLAYSESSION_H
