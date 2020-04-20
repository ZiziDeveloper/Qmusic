//
// Created by Administrator on 2020/4/19 0019.
//
#include <jni.h>
#include <string>
#include "AndroidLog.h"


extern "C" {

    /**
     * 初始化
     * @param env
     * @param obj
     * @param channels
     * @param sampleRate
     * @param brate
     * @param frameLen
     * @return
     */
    jlong init(JNIEnv *env, jobject obj, jint channels, jint sampleRate, jint brate, jintArray frameLen) {
        //__android_log_print(ANDROID_LOG_INFO, "Mp3Encode_init", " Mp3Encode_init start ");
        return (long) 0;
    }

    /**
     * 销毁编码器
     * @param env
     * @param obj
     * @param aacHandle
     */
    void destroy(JNIEnv *env, jobject obj, jlong aacHandle) {
        LOGE("com/zizi/playlib/codec/AACEncodeJniProxy: destroy");
    }

    /**
     * 编码
     * @param env
     * @param obj
     * @param aacHandle
     * @param buffer
     * @param len
     * @return
     */
    jbyteArray encode(JNIEnv *env, jobject obj, jlong aacHandle, jshortArray buffer, jint len) {


        jbyteArray result = (env)->NewByteArray(100);

        (env)->ReleaseShortArrayElements(buffer, nullptr, 0);
        return result;
    }

    jint AACEncodeRegisterNativeMethods(JNIEnv *env) {
        jclass clazz = env->FindClass("com/zizi/playlib/codec/AACEncodeJniProxy");
        if (clazz == nullptr) {
            LOGE("can't find class: com/zizi/playlib/codec/AACEncodeJniProxy");
            return JNI_ERR;
        }
        JNINativeMethod methods_Proxy[] = {
                {"init",    "(III[I)J", (void *) init},
                {"destroy", "(J)V",     (void *) destroy},
                {"encode",  "(J[SI)[B", (void *) encode}
        };
        return env->RegisterNatives(clazz, methods_Proxy, sizeof(methods_Proxy) / sizeof(methods_Proxy[0]));
    }
}
