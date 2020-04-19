package com.zizi.playlib.codec;

/**
 * AAC编码jni代理类
 */
public class AACEncodeJniProxy {

    public native long init(int channel, int sampleRate, int brate, int[] frameLen);

    public native void destroy(long aacPtr);

    public native byte[] encode(long aacPtr, short[] buffer, int len);

    public native byte[] flush(long aacPtr);

}
