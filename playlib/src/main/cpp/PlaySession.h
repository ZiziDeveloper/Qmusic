//
// Created by Administrator on 2018/12/15 0015.
//

#ifndef QMUSIC_PLAYSESSION_H
#define QMUSIC_PLAYSESSION_H

/**
 * 存储播放会话的状态
 */
class PlaySession {
private:
    PlaySession();
    static PlaySession *p;
    char* pUrl;
public:
    bool bExit;
    bool bSeeking;
    bool bLoading;
public:
    static PlaySession* getIns();

    char *getUrl() const;

    void setUrl(char *pUrl);
};


#endif //QMUSIC_PLAYSESSION_H
