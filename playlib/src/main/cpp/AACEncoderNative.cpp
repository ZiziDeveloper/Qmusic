//
// Created by Administrator on 2020/4/19 0019.
//
#include <jni.h>
#include <string>
#include "AndroidLog.h"
#include "AACEncoder.h"

AACEncoder* mAACEncoder;
extern "C" {

    /**
     * 初始化
     * @param env
     * @param obj
     * @param channels 声道数：1，单声道；2，双声道
     * @param sampleRate 采样率
     * @param brate 码率
     * @param frameLen 帧长
     * @return
     */
    jlong init(JNIEnv *env, jobject obj, jint channels, jint sampleRate, jint brate, jintArray frameLen) {
        if ((sampleRate > 48000) || (sampleRate < 16000) || (channels <= 0)
            || (channels > 2) || (brate < 32000) || (brate > 320000)) {
            return -1;
        }
        mAACEncoder = new AACEncoder();
        int realFrameLen;
        long encodePtr = mAACEncoder->init(channels, sampleRate, brate, &realFrameLen);
        int* frameLength = env->GetIntArrayElements(frameLen, nullptr);
        frameLength[0] = realFrameLen;
        env->ReleaseIntArrayElements(frameLen, frameLength, 0);
        return encodePtr;
    }

    /**
     * 销毁编码器
     * @param env
     * @param obj
     * @param aacHandle
     */
    void destroy(JNIEnv *env, jobject obj, jlong aacHandle) {
        LOGE("com/zizi/playlib/codec/AACEncodeJniProxy: destroy");
        mAACEncoder->destroy(aacHandle);
        delete(mAACEncoder);
        mAACEncoder = nullptr;
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
