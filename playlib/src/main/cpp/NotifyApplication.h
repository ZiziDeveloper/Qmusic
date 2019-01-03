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
    jmethodID jmid_error;
private:
    NotifyApplication();
public:
    static NotifyApplication* getIns();
    void init(_JavaVM *jvm, JNIEnv *jenv, jobject* pObj);
    void notifyError(int type, int code, const char* msg);
    void notifyPrepared(int type);
    void notifyLoad(bool load);
    void notifyComplete();
};


#endif //QMUSIC_NOTIFYAPPLICATION_H
