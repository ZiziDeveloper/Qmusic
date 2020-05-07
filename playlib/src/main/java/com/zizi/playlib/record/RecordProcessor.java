package com.zizi.playlib.record;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zizi.playlib.CycleBuffer;
import com.zizi.playlib.record.utils.ApplicationUtil;
import com.zizi.playlib.record.utils.RecordLogTag;

/**
 * 录音处理类
 */
public class RecordProcessor extends Thread {
    private static final String TAG = RecordLogTag.RECORD_PROCCESS_TAG + "RecordProcessor";

    /**
     * AudioRecord创建时允许使用最大的bufferSize
     */
    private static final int AUDIO_RECORD_MAX_BUF_SIZE = 4 * 6000;
    /**
     * AudioRecord创建时使用的bufferSize
     */
    private int mRecBufSize;
    /**
     * AudioRecord最小的bufferSize
     */
    private int mRecMinBufSize;

    private AudioRecord mAudioRecord;

    /**
     * 用于生产者与消费者之前传递音频数据的buffer
     */
    private CycleBuffer mRecCycleBuffer;

    private boolean mIsBluetoothOn;

    /**
     * usb音频设备信息
     */
    private AudioDeviceInfo mUsbMicDevice = null;

    /**
     * 是否使用usb音频设备
     */
    private boolean mIsUsbMicIn;

    private RecordSession mSession = RecordSession.getInstance();

    /**
     * 音频录制采样率
     */
    private int mInSampleRate = mSession.getInSampleRate();

    public RecordProcessor(@NonNull String name, CycleBuffer buffer) {
        super(name);
        mRecCycleBuffer = buffer;
    }

    @Override
    public void run() {
        if (mAudioRecord == null) {
            //检查usb状态
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//版本号判断
                mIsUsbMicIn = checkUsbMic();
            }else {
                mUsbMicDevice = null;
            }
            Log.e(TAG, "mUsbMicDevice : " + mUsbMicDevice);
            mAudioRecord = createAudioRecord(mUsbMicDevice);
        }

        if (mRecCycleBuffer == null) {
            Log.e(TAG, "mRecCycleBuffer == null");
            return;
        }

        short[] buffer = new short[mRecBufSize];
        mAudioRecord.startRecording();
        try{
            while (mSession.isRecording()) {
                int readSize = mAudioRecord.read(buffer, 0, mRecBufSize);
                if (readSize <= 0) {
                    sleep(1);
                    continue;
                }
                mRecCycleBuffer.write(buffer, readSize);
                Log.e(TAG, "mAudioRecord readSize : " + readSize + " buffer size : " + buffer.length + "  UnreadLen : " + mRecCycleBuffer.getUnreadLen());
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException : " + e);
        }

    }

    public void proccessStart() {
        mSession.setIsRecording(true);
        start();
    }

    public void proccessStop(){
        mSession.setIsRecording(false);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private AudioRecord createAudioRecord(AudioDeviceInfo audioDeviceInfo) {
        if (audioDeviceInfo != null) {
            for (int i = 0; i < audioDeviceInfo.getChannelCounts().length; i++) {
                if (audioDeviceInfo.getChannelCounts()[i] == 2) {
                    mSession.setInChannels(AudioFormat.CHANNEL_IN_STEREO);
                    break;
                } else {
                    mSession.setInChannels(AudioFormat.CHANNEL_IN_MONO);
                }
            }
        } else {
            mSession.setInChannels(AudioFormat.CHANNEL_IN_MONO);
        }
        mRecMinBufSize = AudioRecord.getMinBufferSize(mInSampleRate, mSession.getInChannels(), AudioFormat.ENCODING_PCM_16BIT);

        if (mRecMinBufSize > 0) {
            mRecBufSize = audioBufSize(mRecMinBufSize);
            AudioRecord audioRecord = null;
            if (mIsBluetoothOn) {
                mRecBufSize = mRecMinBufSize;
                audioRecord = newAudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, mInSampleRate, mSession.getInChannels(), AudioFormat.ENCODING_PCM_16BIT, mRecBufSize);
            } else {
                audioRecord = newAudioRecord(MediaRecorder.AudioSource.MIC, mInSampleRate, mSession.getInChannels(), AudioFormat.ENCODING_PCM_16BIT, mRecBufSize);
                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    do {
                        mRecBufSize = mRecBufSize / 2;
                        audioRecord = newAudioRecord(MediaRecorder.AudioSource.MIC, mInSampleRate, mSession.getInChannels(), AudioFormat.ENCODING_PCM_16BIT, mRecBufSize);
                        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                            return  audioRecord;
                        }
                    } while (mRecBufSize > mRecMinBufSize);
                }
            }

            if (audioDeviceInfo != null) {
                boolean success = audioRecord.setPreferredDevice(audioDeviceInfo);
                if (!success) {
                    audioRecord.setPreferredDevice(null);
                }
            }

            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                return audioRecord;
            }
        }
        return null;
    }

    private AudioRecord newAudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat,
                                       int bufferSizeInBytes) {
        Log.e(TAG, "newAudioRecord audioSource : " + audioSource + " sampleRateInHz : " + sampleRateInHz
                + " channelConfig : " + channelConfig + " audioFormat : " + audioFormat + " bufferSizeInBytes : " + bufferSizeInBytes);
        RecordSession.getInstance().setRecordBufSize(bufferSizeInBytes);
        return new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean checkUsbMic(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//版本号判断
            mUsbMicDevice = null;
            return false;
        }
        AudioManager audioManager = (AudioManager) ApplicationUtil.getContext().getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] devicesInfos = audioManager.getDevices(audioManager.GET_DEVICES_INPUTS);
        boolean usbMic = false;
        for(int i=0; i<devicesInfos.length; i++){
            final String deviceName = devicesInfos[i].getProductName().toString();
            usbMic = deviceName.contains("USB-Audio - USB Advanced Audio Device") || deviceName.contains("USB-Audio");
            if(usbMic == true){
                mUsbMicDevice = devicesInfos[i];
                break;
            }else {
                mUsbMicDevice = null;
            }
        }
        return usbMic;
    }

    private int audioBufSize(int bufSize) {
        int result = bufSize;
        if (result < AUDIO_RECORD_MAX_BUF_SIZE) {
            result *= 2;
        }
        return result;
    }


}
