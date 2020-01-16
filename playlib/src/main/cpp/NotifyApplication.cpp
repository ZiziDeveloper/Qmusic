//
// Created by Administrator on 2018/12/15 0015.
//

#include "NotifyApplication.h"

NotifyApplication::NotifyApplication() {}

NotifyApplication::~NotifyApplication() {}

NotifyApplication::NotifyApplication(const NotifyApplication&) {}

NotifyApplication& NotifyApplication::operator=(const NotifyApplication&){}

NotifyApplication& NotifyApplication::getIns() {
    static NotifyApplication instance;
    return instance;
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
    this->jmid_started = jenv->GetMethodID(jlz, "onStarted", "()V");
    this->jmid_resumed = jenv->GetMethodID(jlz, "onResumed", "()V");
    this->jmid_paused = jenv->GetMethodID(jlz, "onPaused", "()V");
    this->jmid_stopped = jenv->GetMethodID(jlz, "onStopped", "()V");
    this->jmid_seeked = jenv->GetMethodID(jlz, "onSeeked", "(I)V");
    this->jmid_volumeModified = jenv->GetMethodID(jlz, "onVolumeModified", "(I)V");
    this->jmid_channelLayoutModified = jenv->GetMethodID(jlz, "onChannelLayoutModify", "(I)V");
    this->jmid_pitchModified = jenv->GetMethodID(jlz, "onPitchModified", "(F)V");
    this->jmid_speedModified = jenv->GetMethodID(jlz, "onSpeedModified", "(F)V");
    this->jmid_progress = jenv->GetMethodID(jlz, "onPlayProgress", "(FI)V");
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

void NotifyApplication::notifyStopped(int type) {

    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_stopped);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyStopped get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_stopped);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifyStarted(int type) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_started);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyStarted get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_started);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifyResumed(int type) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_resumed);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyStarted get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_resumed);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifyPaused(int type) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_paused);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyStarted get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_paused);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifySeeked(int type, int progress) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_seeked, progress);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifySeeked get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_seeked, progress);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifyVolumeModified(int type, int percent) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_volumeModified, percent);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyVolumeModified get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_volumeModified, percent);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifyChannelLayoutModified(int type, int layout) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_channelLayoutModified, layout);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyChannelLayoutModified get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_channelLayoutModified, layout);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifyPitchModified(int type, float pitch) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_pitchModified, pitch);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyPitchModified get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_pitchModified, pitch);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifySpeedModified(int type, float speed) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_speedModified, speed);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifySpeedModified get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_speedModified, speed);
        jvm->DetachCurrentThread();
    }
}

void NotifyApplication::notifyProgress(int type, float current, int total) {
    if (MAIN_THREAD == type) {
        jenv->CallVoidMethod(jobj, jmid_progress, current, total);
    } else if (CHILD_THREAD == type) {
        JNIEnv* env;
        if (jvm->AttachCurrentThread(&env, 0) != JNI_OK) {
            LOGE("NotifyApplication::notifyProgress get child jnienv wrong");
            return;
        }
        env->CallVoidMethod(jobj, jmid_progress, current, total);
        jvm->DetachCurrentThread();
    }
}
