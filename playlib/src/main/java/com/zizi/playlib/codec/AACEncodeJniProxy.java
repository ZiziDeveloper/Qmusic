package com.zizi.playlib.codec;

/**
 * AAC编码jni代理类
 */
public class AACEncodeJniProxy {

    /**
     * [todo]truyayong 需要统一调度System.loadLibrary加载动态库，防止重复加载
     */
    static {
        System.loadLibrary("PlayNative");
    }

    public native int init(int channel, int sampleRate, int brate, int[] frameLen);

    public native void destroy();

    public native byte[] encode(short[] buffer, int len);

    public native byte[] flush(long aacPtr);

}
