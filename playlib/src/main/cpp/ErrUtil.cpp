//
// Created by Administrator on 2018/12/16 0016.
//

#include "ErrUtil.h"

const char* ErrUtil::errLog(int errCode) {
    char* info = (char *) malloc(ERR_INFO_SIZE);
    av_strerror(errCode, info, ERR_INFO_SIZE);
    return info;
}
