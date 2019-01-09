//
// Created by Administrator on 2018/12/27 0027.
//

#ifndef QMUSIC_PACKETQUEUE_H
#define QMUSIC_PACKETQUEUE_H

#include "queue"
#include "pthread.h"
#include "PlaySession.h"
#include "AndroidLog.h"
extern "C" {
#include <libavcodec/avcodec.h>
};

class PacketQueue {
private:
    std::queue<AVPacket*> mQueue;
    pthread_mutex_t mMutex;
    pthread_cond_t mCond;
public:
    //队列缓存不宜过大，如果过大，则网络文件就会一次缓存完,导致seek状态bug
    static const int MAX_SIZE = 100;
public:
    PacketQueue();
    virtual ~PacketQueue();
    int putAvPacket(AVPacket* packet);
    int getAvPacket(AVPacket* packet);
    int size();
    void clearQueue();
};


#endif //QMUSIC_PACKETQUEUE_H
