//
// Created by Administrator on 2018/12/15 0015.
//

#ifndef QMUSIC_NOTIFYAPPLICATION_H
#define QMUSIC_NOTIFYAPPLICATION_H

#include <jni.h>
#include <cwchar>
#include "AndroidLog.h"

//主进程
#define MAIN_THREAD 0
//子进程
#define CHILD_THREAD 1

/**
 * 通知应用层
 */
class NotifyApplication {
private:
    static NotifyApplication* p;
    _JavaVM* jvm;
    JNIEnv* jenv;
    jobject jobj;
    jmethodID jmid_prepare;
    jmethodID jmid_started;
    jmethodID jmid_resumed;
    jmethodID jmid_paused;
    jmethodID jmid_stopped;
    jmethodID jmid_seeked;
    jmethodID jmid_volumeModified;
    jmethodID jmid_channelLayoutModified;
    jmethodID jmid_pitchModified;
    jmethodID jmid_speedModified;
    jmethodID jmid_progress;
    jmethodID jmid_error;
private:
    NotifyApplication();
    ~NotifyApplication();
    NotifyApplication(const NotifyApplication&);
    NotifyApplication& operator=(const NotifyApplication&);
public:
    static NotifyApplication& getIns();
    void init(_JavaVM *jvm, JNIEnv *jenv, jobject* pObj);
    void notifyPrepared(int type);
    void notifyStarted(int type);
    void notifyResumed(int type);
    void notifyPaused(int type);
    void notifyStopped(int type);
    void notifySeeked(int type, int progress);
    void notifyVolumeModified(int type, int percent);
    void notifyChannelLayoutModified(int type, int layout);
    void notifyPitchModified(int type, float pitch);
    void notifySpeedModified(int type, float speed);
    void notifyLoad(bool load);
    void notifyProgress(int type, float current, int total);
    void notifyError(int type, int code, const char* msg);
};


#endif //QMUSIC_NOTIFYAPPLICATION_H
