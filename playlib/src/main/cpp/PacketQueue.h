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
    PacketQueue();
    virtual ~PacketQueue();
    int putAvPacket(AVPacket* packet);
    int getAvPacket(AVPacket* packet);
    int size();
    void clearQueue();
};


#endif //QMUSIC_PACKETQUEUE_H
