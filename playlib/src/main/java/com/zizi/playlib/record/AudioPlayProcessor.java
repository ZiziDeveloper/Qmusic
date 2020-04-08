package com.zizi.playlib.record;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zizi.playlib.CycleBuffer;
import com.zizi.playlib.record.utils.RecordLogTag;

public class AudioPlayProcessor extends Thread {

    private static final String TAG = RecordLogTag.RECORD_PROCCESS_TAG + "AudioPlayProcessor";

    /**
     * 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
     */
    private static final int FREQUENCY = 44100;
    /**
     * 单声道或者双声道
     */
    private int channels = AudioFormat.CHANNEL_OUT_STEREO;

    /**
     * AudioTrack创建时允许使用最大的bufferSize
     */
    private static final int AUDIO_PLAY_MAX_BUF_SIZE = 20000;

    private AudioTrack mAudioTrack = null;
    private CycleBuffer mPlayCycleBuffer;
    private int mPlayMinBufSize;
    private int mPlayBufSize;
    private boolean isPlaying = false;
    private RecordSession mSession = RecordSession.getInstance();

    public AudioPlayProcessor(@NonNull String name, CycleBuffer buffer) {
        super(name);
        this.mPlayCycleBuffer = buffer;
    }

    public void proccessStart() {
        isPlaying = true;
        start();
    }

    public void proccessStop(){
        isPlaying = false;
        mAudioTrack.stop();
        mAudioTrack.release();
    }

    @Override
    public void run() {
        if (mAudioTrack == null) {
            mAudioTrack = newAudioTrack();
            mAudioTrack.play();
        }
        /**
         * 此时有可能AudioRecord没有初始化成功
         */
        short[] buffer = new short[mSession.getRecordBufSize()];
        while (isPlaying) {
            try{
                if (buffer.length <= 0) {
                    buffer = new short[mSession.getRecordBufSize()];
                    sleep(1);
                    continue;
                }
                if (mPlayCycleBuffer.getUnreadLen() <= 0) {
                    sleep(20);
                    continue;
                }
                int readSize = mPlayCycleBuffer.read(buffer, mSession.getRecordBufSize());
                mAudioTrack.write(buffer, 0, readSize);
            } catch (Exception e) {
                Log.e(TAG, "Exception : " + e);
            }
        }
    }

    private AudioTrack newAudioTrack() {
        mPlayMinBufSize = AudioTrack.getMinBufferSize(FREQUENCY, channels, AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack audioTrack = null;
        if (mPlayMinBufSize > 0) {
            mPlayBufSize = audioBufSize(mPlayMinBufSize);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, FREQUENCY, channels
                    , AudioFormat.ENCODING_PCM_16BIT, mPlayBufSize, AudioTrack.MODE_STREAM);
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                RecordSession.getInstance().setPlayBufSize(mPlayBufSize);
                return audioTrack;
            }
        }
        return null;
    }

    private int audioBufSize(int bufSize) {
        int result = bufSize;
        if (result < AUDIO_PLAY_MAX_BUF_SIZE) {
            result *= 2;
        }
        return result;
    }
}
