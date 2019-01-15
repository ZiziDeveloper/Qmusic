#include <jni.h>
#include <string>
#include "AndroidLog.h"
#include "AudioProccessor.h"
#include "PlaySession.h"
#include "NotifyApplication.h"

AudioProccessor* pAudioProccessor = NULL;
_JavaVM* javaVM = NULL;

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
    return JNI_VERSION_1_4;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1prepare(JNIEnv *env, jobject instance, jstring source_,
                                                     jint volume, jint playState, jint mutesole) {
    const char *source = env->GetStringUTFChars(source_, 0);
    if (NULL == pAudioProccessor) {
        PlaySession::getIns()->setUrl((char *) source);
        NotifyApplication::getIns()->init(javaVM, env, &instance);
        pAudioProccessor = new AudioProccessor;
        pAudioProccessor->prepare();
    }
    LOGI("native_prepare");
    env->ReleaseStringUTFChars(source_, source);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1start(JNIEnv *env, jobject instance) {
    if (NULL != pAudioProccessor) {
        pAudioProccessor->start();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1resume(JNIEnv *env, jobject instance) {
    if (NULL != pAudioProccessor) {
        pAudioProccessor->resume();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1pause(JNIEnv *env, jobject instance) {
    if (NULL != pAudioProccessor) {
        pAudioProccessor->pause();
    }
}
const int NOT_PLAY_NEXT = 0;
const int PLAY_NEXT = 1;
extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1stop(JNIEnv *env, jobject instance, jint bNext) {
    if (NULL != pAudioProccessor) {
        pAudioProccessor->stop();
        if (PLAY_NEXT == bNext) {
            NotifyApplication::getIns()->notifyPlayNext(MAIN_THREAD);
        }
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1seek(JNIEnv *env, jobject instance, jint progress) {
    if (NULL != pAudioProccessor) {
        pAudioProccessor->seek(progress);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_playlib_PlayJniProxy_native_1duration(JNIEnv *env, jobject instance) {

    // TODO

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1volume(JNIEnv *env, jobject instance, jint percent) {
    if (NULL != pAudioProccessor) {
        pAudioProccessor->setVolume(percent);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1pitch(JNIEnv *env, jobject instance, jfloat pitch) {
    if (NULL != pAudioProccessor) {
        pAudioProccessor->setPitch(pitch);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1speed(JNIEnv *env, jobject instance, jfloat speed) {
    if (NULL != pAudioProccessor) {
        pAudioProccessor->setSpeed(speed);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_playlib_PlayJniProxy_native_1samplerate(JNIEnv *env, jobject instance) {

    // TODO

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_playlib_PlayJniProxy_native_1channel_1switch(JNIEnv *env, jobject instance,
                                                             jint channel) {
    if (NULL != pAudioProccessor) {
        pAudioProccessor->switchChannel(channel);
    }
}
