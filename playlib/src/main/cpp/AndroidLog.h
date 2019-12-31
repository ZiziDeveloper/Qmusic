//
// Created by Administrator on 2018/12/13 0013.
//

#ifndef QMUSIC_ANDROIDLOG_H
#define QMUSIC_ANDROIDLOG_H

#include "android/log.h"
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"qmusic",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"qmusic",FORMAT,##__VA_ARGS__);
#endif //QMUSIC_ANDROIDLOG_H
