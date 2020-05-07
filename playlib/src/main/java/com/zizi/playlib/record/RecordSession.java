package com.zizi.playlib.record;

import android.media.AudioFormat;

/**
 * 录音过程中共享数据Session
 */
public class RecordSession {

    /**
     * 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
     */
    private static final int FREQUENCY = 44100;

    private static RecordSession INSTANCE = new RecordSession();

    private RecordSession() {
    }

    public static RecordSession getInstance() {
        return INSTANCE;
    }

    private int mRecordBufSize = 0;

    private int mPlayBufSize = 0;

    /**
     * 输入采样率
     */
    private int mInSampleRate = FREQUENCY;


    /**
     * 输出采样率
     */
    private int mOutSampleRate = FREQUENCY;

    /**
     * 输入声道数：单声道，双声道
     */
    private int mInChannels = AudioFormat.CHANNEL_IN_MONO;

    /**
     * 输出声道数：单声道，双声道
     */
    private int mOutChannels = AudioFormat.CHANNEL_OUT_MONO;

    /**
     * AAC编码码率
     */
    private int mEncodeBrate = 128000;

    /**
     * 是否正在录音
     */
    private boolean mIsRecording = false;

    public void setRecordBufSize(int size) {
        this.mRecordBufSize = size;
    }

    public int getRecordBufSize() {
        return mRecordBufSize;
    }

    public void setPlayBufSize(int size) {
        this.mPlayBufSize = size;
    }

    public int getPlayBufSize() {
        return mPlayBufSize;
    }

    public int getInSampleRate() {
        return mInSampleRate;
    }

    public void setInSampleRate(int inSampleRate) {
        this.mInSampleRate = inSampleRate;
    }

    public int getOutSampleRate() {
        return mOutSampleRate;
    }

    public void setOutSampleRate(int outSampleRate) {
        this.mOutSampleRate = outSampleRate;
    }

    public int getInChannels() {
        return mInChannels;
    }

    public void setInChannels(int inChannels) {
        this.mInChannels = inChannels;
    }

    public int getOutChannels() {
        return mOutChannels;
    }

    public void setOutChannels(int outChannels) {
        this.mOutChannels = outChannels;
    }

    public int getEncodeBrate() {
        return mEncodeBrate;
    }

    public void setEncodeBrate(int encodeBrate) {
        this.mEncodeBrate = encodeBrate;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void setIsRecording(boolean isRecording) {
        this.mIsRecording = isRecording;
    }
}
