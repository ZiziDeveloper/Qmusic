//
// Created by Administrator on 2018/12/27 0027.
//

#include "PacketQueue.h"

PacketQueue::PacketQueue() {
    pthread_mutex_init(&mMutex, nullptr);
    pthread_cond_init(&mCond, nullptr);
}

PacketQueue::~PacketQueue() {
    clearQueue();
    pthread_cond_destroy(&mCond);
    pthread_mutex_destroy(&mMutex);
}

int PacketQueue::putAvPacket(AVPacket *packet) {
    pthread_mutex_lock(&mMutex);
    mQueue.push(packet);
    pthread_cond_signal(&mCond);
    pthread_mutex_unlock(&mMutex);
    return 0;
}

int PacketQueue::getAvPacket(AVPacket *packet) {
    pthread_mutex_lock(&mMutex);
    while (!PlaySession::getIns()->bExit) {
        if (mQueue.size() > 0) {
            AVPacket* avPacket = mQueue.front();
            if (av_packet_ref(packet, avPacket) == 0) {
                mQueue.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = nullptr;
            break;
        } else {
            pthread_cond_wait(&mCond, &mMutex);
        }
    }
    pthread_mutex_unlock(&mMutex);
    return 0;
}

int PacketQueue::size() {
    int size = 0;
    pthread_mutex_lock(&mMutex);
    size = mQueue.size();
    pthread_mutex_unlock(&mMutex);
    return size;
}

void PacketQueue::clearQueue() {
    pthread_cond_signal(&mCond);
    pthread_mutex_lock(&mMutex);
    while (!mQueue.empty()) {
        AVPacket *packet = mQueue.front();
        mQueue.pop();
        av_packet_free(&packet);
        av_free(packet);
        packet = nullptr;
    }
    pthread_mutex_unlock(&mMutex);
}
