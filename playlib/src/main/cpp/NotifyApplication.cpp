//
// Created by Administrator on 2018/12/15 0015.
//

#include "NotifyApplication.h"

NotifyApplication::NotifyApplication() {}

NotifyApplication* NotifyApplication::getIns() {
    return p;
}

NotifyApplication* NotifyApplication::p = new NotifyApplication();

void NotifyApplication::init(_JavaVM *jvm, JNIEnv *jenv, jobject *pObj) {
    this->jvm = jvm;
    this->jenv = jenv;
    this->jobj = *pObj;
    this->jobj = jenv->NewGlobalRef(jobj);

    jclass jlz = jenv->GetObjectClass(this->jobj);
    if (!jlz) {
        LOGE("get jclass wrong");
        return;
    }

    this->jmid_error = jenv->GetMethodID(jlz
            , "onError", "(ILjava/lang/String;)V");
    this->jmid_prepare = jenv->GetMethodID(jlz, "onPrepared", "()V");
    LOGE("truyayong init onPlayNext");
    this->jmid_playnext = jenv->GetMethodID(jlz, "onPlayNext", "()V");
}

void NotifyApplication::notifyError(int type, int code, const char *msg) {
    if (MAIN_THREAD == type) {
        jstring jmsg = jenv->NewStringUTF(msg);
        jenv->CallVoidMethod(jobj, jmid_error, code, jmsg);
        jenv->DeleteLocalRef(jmsg);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyError get child jnienv wrong");
            return;
        }
        jstring jmsg = env->NewStringUTF(msg);
        env->CallVoidMethod(jobj, jmid_error, code, jmsg);
        env->DeleteLocalRef(jmsg);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifyPrepared(int type) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_prepare);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyPrepared get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_prepare);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifyLoad(bool load) {

}

void NotifyApplication::notifyComplete() {

}

void NotifyApplication::notifyPlayNext(int type) {

    if (MAIN_THREAD == type) {
        LOGE("truyayong enter onPlayNext");
        jenv->CallVoidMethod(jobj, jmid_playnext);
        LOGE("truyayong end onPlayNext");
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyPlayNext get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_playnext);
        jvm->DetachCurrentThread();
    }
}
