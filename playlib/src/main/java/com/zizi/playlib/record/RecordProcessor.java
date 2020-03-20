package com.zizi.playlib.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;

/**
 * 录音处理类
 */
public class RecordProcessor extends Thread {

    private static final int FREQUENCY = 44100;// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static final int CHANNELCONGIFIGURATION = AudioFormat.CHANNEL_IN_MONO;// 设置单声道声道
    private static final int AUDIOENCODING = AudioFormat.ENCODING_PCM_16BIT;// 音频数据格式：每个样本16位
    public final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;// 音频获取源
    private int mRecBufSize;// 录音最小buffer大小

    private AudioRecord mAudioRecord;

    public RecordProcessor(@NonNull String name) {
        super(name);
    }

    @Override
    public void run() {
        if (mAudioRecord == null) {
            mRecBufSize = AudioRecord.getMinBufferSize(FREQUENCY,
                    CHANNELCONGIFIGURATION, AUDIOENCODING);// 录音组件
            mAudioRecord = new AudioRecord(AUDIO_SOURCE,// 指定音频来源，这里为麦克风
                    FREQUENCY, // 16000HZ采样频率
                    CHANNELCONGIFIGURATION,// 录制通道
                    AUDIO_SOURCE,// 录制编码格式
                    mRecBufSize);// 录制缓冲区大小 //先修改
        }
    }

}
