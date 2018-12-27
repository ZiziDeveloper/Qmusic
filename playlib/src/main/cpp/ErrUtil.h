//
// Created by Administrator on 2018/12/16 0016.
//

#ifndef QMUSIC_ERRUTIL_H
#define QMUSIC_ERRUTIL_H

#define ERR_INFO_SIZE 1024
#include <malloc.h>
#include "AndroidLog.h"
extern "C" {
#include <libavutil/error.h>
};
class ErrUtil {
public:
    static const char* errLog(int errCode);
};


#endif //QMUSIC_ERRUTIL_H
