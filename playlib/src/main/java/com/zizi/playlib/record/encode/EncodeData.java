package com.zizi.playlib.record.encode;

/**
 * 投喂给aac编码器的pcm数据array
 */
public class EncodeData {

    public short[] mData;
    public int validateLength;

    public EncodeData() {
        mData = new short[2 * 1024];
    }
}
