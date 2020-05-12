package com.zizi.playlib.record.encode;

import android.media.AudioFormat;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zizi.playlib.codec.AACEncodeJniProxy;
import com.zizi.playlib.record.RecordSession;
import com.zizi.playlib.record.utils.RecordLogTag;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 录音AAC编码处理类
 */
public class EncodeProcessor extends Thread {

    private static final String TAG = RecordLogTag.RECORD_PROCCESS_TAG + "EncodeProcessor";

    /**
     * 编码传递缓存
     */
    private EncodePreBuffer mEncodePreBuffer;

    private String mEncodePath;

    /**
     * 编码代理
     */
    private AACEncodeJniProxy mProxy;

    private RecordSession mSession = RecordSession.getInstance();

    public EncodeProcessor(@NonNull String name, EncodePreBuffer encodeBuffer, String encodePath) {
        super(name);
        this.mProxy = new AACEncodeJniProxy();
        this.mEncodePreBuffer = encodeBuffer;
        this.mEncodePath = encodePath;
    }

    @Override
    public void run() {
        int[] frameLen = new int[1];

        try {
            File file = new File(mEncodePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            RandomAccessFile randfile = new RandomAccessFile(file,"rw");

            int channel = 0;
            if (mSession.getOutChannels() == AudioFormat.CHANNEL_OUT_STEREO) {
                channel = 2;
            } else {
                channel = 1;
            }
            int result = mProxy.init(channel, mSession.getOutSampleRate(), mSession.getEncodeBrate(),frameLen);
            if (result != 0) {
                Log.e(TAG, "mProxy.init error : " + result);
                return;
            }
            Log.e(TAG, "mProxy.cycle error : " + result);

            EncodeData encodeData;
            while (mSession.isRecording()) {
                encodeData = mEncodePreBuffer.read();
                if (encodeData.validateLength == 0) {
                    try {
                        sleep(20);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException : " + e);
                    }
                    continue;
                }

                byte[] aacDatas;
                aacDatas = mProxy.encode(encodeData.mData, encodeData.validateLength);
                randfile.write(aacDatas, 0, aacDatas.length);
            }
            randfile.close();
            mProxy.destroy();
            Log.e(TAG, "mProxy.close destroy ");
        } catch (IOException e) {
            Log.e(TAG, "IOException : " + e);
        }

    }
}
