#include <jni.h>
#include <string>
#include "AndroidLog.h"
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>

extern "C" {

    /**
     *
     * @param env
     * @param obj
     * @param filename
     * @return
     */
    jint openFile(JNIEnv *env, jobject obj, jstring filename) {
        LOGE("class: com/zizi/playlib/nativeUtils/FileUtilJniProxy: openFile");
        if (filename == nullptr) {
            LOGE("class: com/zizi/playlib/nativeUtils/FileUtilJniProxy: openFile filename is null");
            return -1;
        }

        const char* fileChars = env->GetStringUTFChars(filename, nullptr);

        if (fileChars == nullptr) {
            LOGE("class: com/zizi/playlib/nativeUtils/FileUtilJniProxy: openFile fileChars is null");
            env->ReleaseStringUTFChars(filename, fileChars);
            return -1;
        }

        int fd = open(fileChars, O_RDWR);

        if (fd < 0) {
            LOGE("class: com/zizi/playlib/nativeUtils/FileUtilJniProxy: openFile file error");
        }

        env->ReleaseStringUTFChars(filename, fileChars);
        return fd;
    }

    /**
     *
     * @param env
     * @param obj
     * @param fd
     */
    void closeFile(JNIEnv *env, jobject obj, jint fd) {
        LOGE("class: com/zizi/playlib/nativeUtils/FileUtilJniProxy: close");
        if (fd < 0) {
            LOGE("class: com/zizi/playlib/nativeUtils/FileUtilJniProxy: close fd < 0");
            return;
        }
        close(fd);
        return ;
    }

    /**
     *
     * @param env
     * @param obj
     * @param startWrite
     * @param buffer
     * @param offset
     * @param length
     * @return
     */
    jboolean writeFile(JNIEnv *env, jobject obj,jint startWrite, jshortArray buffer, jint offset, jint length) {
        LOGE("class: com/zizi/playlib/nativeUtils/FileUtilJniProxy: writeFile");
        return 0;
    }

    /**
     *
     * @param env
     * @param obj
     * @param startRead
     * @param buffer
     * @param offset
     * @param length
     * @return
     */
    jboolean readFile(JNIEnv *env, jobject obj,jint startRead, jshortArray buffer, jint offset, jint length) {
        LOGE("class: com/zizi/playlib/nativeUtils/FileUtilJniProxy: readFile");
        return 0;
    }
    jint FileUtilRegisterNativeMethods(JNIEnv *env) {
        jclass clazz = env->FindClass("com/zizi/playlib/nativeUtils/FileUtilJniProxy");
        if (clazz == nullptr) {
            LOGE("can't find class: com/zizi/playlib/nativeUtils/FileUtilJniProxy");
            return JNI_ERR;
        }
        JNINativeMethod methods_Proxy[] = {
                {"nativeOpenFile",    "(Ljava/lang/String;)I", (void *) openFile},
                {"nativeClose", "(I)V",     (void *) closeFile},
                {"nativeWriteFile",  "(II[SII)Z", (void *) writeFile},
                {"nativeReadFile",  "(II[SII)Z", (void *) readFile}
        };
        return env->RegisterNatives(clazz, methods_Proxy, sizeof(methods_Proxy) / sizeof(methods_Proxy[0]));
    }
}
//

