//
// Created by Administrator on 2018/12/15 0015.
//

#include <cwchar>
#include "PlaySession.h"

PlaySession::PlaySession() {}

PlaySession *PlaySession::getIns() {
    return p;
}

PlaySession* PlaySession::p = new PlaySession();


char *PlaySession::getUrl() const {
    return pUrl;
}

void PlaySession::setUrl(char *pUrl) {
    PlaySession::pUrl = pUrl;
}
