package com.zizi.playlib.record;

import android.media.AudioFormat;
import android.support.annotation.NonNull;
import android.util.Log;

import com.zizi.playlib.CycleBuffer;
import com.zizi.playlib.codec.AACEncodeJniProxy;
import com.zizi.playlib.nativeUtils.FileUtilJniProxy;
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
    private CycleBuffer mEncodeCycleBuffer;

    private String mEncodePath;

    /**
     * 编码代理
     */
    private AACEncodeJniProxy mProxy;

    private RecordSession mSession = RecordSession.getInstance();

    public EncodeProcessor(@NonNull String name, CycleBuffer encodeCycleBuffer, String encodePath) {
        super(name);
        this.mProxy = new AACEncodeJniProxy();
        this.mEncodeCycleBuffer = encodeCycleBuffer;
        this.mEncodePath = encodePath;
    }

    @Override
    public void run() {
        int[] frameLen = new int[1];
        short[] buffer ;
        int encodeCount = 10;
        int i = 0;

        try {
            File file = new File(mEncodePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            RandomAccessFile randfile = new RandomAccessFile(file,"rw");
            int offset = 0;
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

            while (mSession.isRecording() || mEncodeCycleBuffer.getUnreadLen() > 0) {
                int unRead = mEncodeCycleBuffer.getUnreadLen();
                if (unRead <= 0) {
                    try {
                        sleep(10);
                        continue;
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException error : " + e);
                    }
                }

                int audioRecordBufferSize = RecordSession.getInstance().getRecordBufSize();
                buffer = new short[audioRecordBufferSize];
                int readSize = mEncodeCycleBuffer.read(buffer, audioRecordBufferSize);

                synchronized (mEncodeCycleBuffer) {
                    mEncodeCycleBuffer.read(buffer, readSize);
                }
                Log.e(TAG, "mProxy.encode readSize : " + readSize + " buffer.length : " + buffer.length);
                byte[] encodeDatas;
                if (i < encodeCount) {
                    i++;
                } else {
                    encodeDatas = mProxy.encode(buffer, readSize);
                    randfile.write(encodeDatas, 0, encodeDatas.length);
                }
            }
            randfile.close();
            mProxy.destroy();
            Log.e(TAG, "mProxy.close destroy ");
        } catch (IOException e) {
            Log.e(TAG, "IOException : " + e);
        }

    }
}
