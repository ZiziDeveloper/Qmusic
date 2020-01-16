#include <jni.h>
#include <string>
#include "AndroidLog.h"
#include "AudioProccessor.h"
#include "PlaySession.h"
#include "NotifyApplication.h"

AudioProccessor* pAudioProccessor = nullptr;
_JavaVM* javaVM = nullptr;

/***********************************************************************
 * c++ method
 */
void nativeStop() {
    if (nullptr != pAudioProccessor) {
        pAudioProccessor->stop();
        delete  pAudioProccessor;
        pAudioProccessor = nullptr;
    }
    NotifyApplication::getIns()->notifyStopped(MAIN_THREAD);
}

/***********************************************************************
 * jni method
 */


extern "C" {

    /**
     * 播放准备
     * @param env
     * @param instance
     * @param source_
     * @param volume
     * @param layout
     */
    void nativePrepare(JNIEnv *env, jobject instance,
                        jstring source_, jint volume, jint layout) {
        const char *source = env->GetStringUTFChars(source_, 0);
        int length = env->GetStringLength(source_);
        PlaySession::getIns().allocUrl((char *) source, length);
        NotifyApplication::getIns()->init(javaVM, env, &instance);
        nativeStop();
        if (nullptr == pAudioProccessor) {
            pAudioProccessor = new AudioProccessor;
        }
        pAudioProccessor->prepare();
        LOGI("native_prepare url : %s ", PlaySession::getIns().getUrl());
        env->ReleaseStringUTFChars(source_, source);
    }

    /**
     * 开始播放
     * @param env
     * @param instance
     */
    void nativeStart(JNIEnv *env, jobject instance) {
        if (nullptr != pAudioProccessor) {
            pAudioProccessor->start();
        }
    }

    /**
     * 继续播放
     * @param env
     * @param instance
     */
    void nativeResume(JNIEnv *env, jobject instance) {
        if (nullptr != pAudioProccessor) {
            pAudioProccessor->resume();
            NotifyApplication::getIns()->notifyResumed(MAIN_THREAD);
        }
    }

    /**
     * 暂停播放
     * @param env
     * @param instance
     */
    void nativePause(JNIEnv *env, jobject instance) {
        if (nullptr != pAudioProccessor) {
            pAudioProccessor->pause();
            NotifyApplication::getIns()->notifyPaused(MAIN_THREAD);
        }
    }

    /**
     * 停止播放
     * @param env
     * @param instance
     */
    void nativeJniStop(JNIEnv *env, jobject instance) {
        nativeStop();
    }

    /**
     * 拖动播放进度
     * @param env
     * @param instance
     * @param progress
     */
    void nativeSeek(JNIEnv *env, jobject instance, jint progress) {
        if (nullptr != pAudioProccessor) {
            pAudioProccessor->seek(progress);
            NotifyApplication::getIns()->notifySeeked(MAIN_THREAD, progress);
        }
    }

    /**
     * 音频时长
     * @param env
     * @param instance
     * @return
     */
    jint nativeDuration(JNIEnv *env, jobject instance) {
        // TODO
        return 0;
    }

    /**
     * 设置声音
     * @param env
     * @param instance
     * @param percent
     */
    void nativeVolume(JNIEnv *env, jobject instance, jint percent) {
        if (nullptr != pAudioProccessor) {
            pAudioProccessor->setVolume(percent);
            NotifyApplication::getIns()->notifyVolumeModified(MAIN_THREAD, percent);
        }
    }

    /**
     * 改变声道
     * @param env
     * @param instance
     * @param channel
     */
    void nativeChannelSwitch(JNIEnv *env, jobject instance,jint channel) {
        if (nullptr != pAudioProccessor) {
            pAudioProccessor->switchChannel(channel);
            NotifyApplication::getIns()->notifyChannelLayoutModified(MAIN_THREAD, channel);
        }
    }

    /**
     * 改变音调
     * @param env
     * @param instance
     * @param pitch
     */
    void nativePitch(JNIEnv *env, jobject instance, jfloat pitch) {
        if (nullptr != pAudioProccessor) {
            pAudioProccessor->setPitch(pitch);
            NotifyApplication::getIns()->notifyPitchModified(MAIN_THREAD, pitch);
        }
    }

    /**
     * 改变播放速度
     * @param env
     * @param instance
     * @param speed
     */
    void nativeSpeed(JNIEnv *env, jobject instance, jfloat speed) {
        if (nullptr != pAudioProccessor) {
            pAudioProccessor->setSpeed(speed);
            NotifyApplication::getIns()->notifySpeedModified(MAIN_THREAD, speed);
        }
    }

    /**
     * 获取采样率
     * @param env
     * @param instance
     * @return
     */
    jint nativeSamplerate(JNIEnv *env, jobject instance) {
        // TODO
        return 0;
    }

    jint RegisterNativeMethods(JNIEnv *env) {
        jclass clazz = env->FindClass("com/zizi/playlib/PlayJniProxy");
        if (clazz == nullptr) {
            LOGE("con't find class: com/zizi/playlib/PlayJniProxy");
            return JNI_ERR;
        }
        JNINativeMethod methods_Proxy[] = {
                {"native_prepare", "(Ljava/lang/String;II)V", (void *) nativePrepare},
                {"native_start", "()V", (void *) nativeStart},
                {"native_resume", "()V", (void *) nativeResume},
                {"native_pause", "()V", (void *) nativePause},
                {"native_stop", "()V", (void *) nativeJniStop},
                {"native_seek", "(I)V", (void *) nativeSeek},
                {"native_duration", "()I", (void *) nativeDuration},
                {"native_volume", "(I)V", (void *) nativeVolume},
                {"native_channel_switch", "(I)V", (void *) nativeChannelSwitch},
                {"native_pitch", "(F)V", (void *) nativePitch},
                {"native_speed", "(F)V", (void *) nativeSpeed},
                {"native_samplerate", "()I", (void *) nativeSamplerate}

        };
        return env->RegisterNatives(clazz, methods_Proxy, sizeof(methods_Proxy) / sizeof(methods_Proxy[0]));
    }
}

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    jint result = -1;
    javaVM = vm;
    JNIEnv *env;
    if(vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK)
    {

        return result;
    }
    result = RegisterNativeMethods(env);
    LOGI("RegisterNativeMethods result : %d ", result);
    return JNI_VERSION_1_4;
}

