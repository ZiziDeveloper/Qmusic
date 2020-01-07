//
// Created by Administrator on 2018/12/15 0015.
//

#include <cwchar>
#include "PlaySession.h"

PlaySession::PlaySession(){

    bExit = false;
    bSeeking = false;
    bLoading = true;
    volume = 85;
    playState = PLAY_STATE_PLAYING;
    outChannelLayout = PLAY_CHANNEL_STEREO;
    inChannelLayout = 0;
    outFmt = AV_SAMPLE_FMT_S16;
    inFmt = AV_SAMPLE_FMT_S16;
    outSmapleRate = 44100;
    inSampleRate = 0;
    numSampleAvFrame = 0;


    duration = 0;

    currentClock = 0;
    lastClock = 0;

    pitch = 1.0f;
    speed = 1.0f;

    pUrl = nullptr;
}

PlaySession *PlaySession::getIns() {
    return p;
}

PlaySession* PlaySession::p = new PlaySession();


char *PlaySession::getUrl() const {
    return this->pUrl;
}

int PlaySession::getInChannelLayoutBytes() {
    int bytes = 0;
    if (inChannelLayout == PLAY_CHANNEL_RIGHT) {//右声道
        bytes = 1;
    } else if (inChannelLayout == PLAY_CHANNEL_LEFT) {//左声道
        bytes = 1;
    } else if (inChannelLayout == PLAY_CHANNEL_STEREO) {//立体声
        bytes = 2;
    }
    return bytes;
}

int PlaySession::getoutChannelLayoutBytes() {
    int bytes = 0;
    if (outChannelLayout == PLAY_CHANNEL_RIGHT) {//右声道
        bytes = 1;
    } else if (outChannelLayout == PLAY_CHANNEL_LEFT) {//左声道
        bytes = 1;
    } else if (outChannelLayout == PLAY_CHANNEL_STEREO) {//立体声
        bytes = 2;
    }
    return bytes;
}

void PlaySession::allocUrl(char *pUrl, int length) {
    if (nullptr != this->pUrl) {
        free(this->pUrl);
        this->pUrl = nullptr;
    }
    this->pUrl = (char*)malloc(length);
    strcpy(this->pUrl, pUrl);
}

void PlaySession::releaseUrl() {
    if (nullptr != this->pUrl) {
        free(this->pUrl);
        this->pUrl = nullptr;
    }
}
