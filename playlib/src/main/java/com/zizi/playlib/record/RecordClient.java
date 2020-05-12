package com.zizi.playlib.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import com.zizi.playlib.CycleBuffer;
import com.zizi.playlib.record.encode.EncodePreBuffer;
import com.zizi.playlib.record.encode.EncodeProcessor;
import com.zizi.playlib.record.utils.Pcm2Wav;
import com.zizi.playlib.record.utils.RecordLogTag;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 录音功能对外接口
 */
public class RecordClient {
    private static final String TAG = RecordLogTag.RECORD_PROCCESS_TAG + "RecordClient";

    private static final int REV_CYCLE_BUFFER_SIZE = 100 * 1024;

    private static final int PLAY_CYCLE_BUFFER_SIZE = 100 * 1024;

    /**
     * 录音处理线程
     */
    private RecordProcessor mRecordProcessor;
    /**
     * 录音播放线程
     */
    private AudioPlayProcessor mAudioPlayProcessor;
    /**
     * 录音音频数据缓存
     */
    private CycleBuffer mRevCycleBuffer;
    /**
     * 播放录音数据缓存
     */
    private CycleBuffer mPlayCycleBuffer;

    /**
     * 编码音频数据线程
     */
    private EncodeProcessor mEncodeProcessor;

    /**
     * 编码音频前处理数据线程
     */
    private EncodePreBuffer mEncodePreBuffer;

    private Thread mReceiveRecordThread;
    private Thread mWriteRunnableThread;
    private OnRecordNotifyListner mOnRecordNotifyListner;

    private int mReadsize;
    private ArrayList<Short> inBuf = new ArrayList<Short>();//缓冲区数据
    public int mRateX = 100;//控制多少帧取一帧
    private ArrayList<byte[]> mWriteData = new ArrayList<byte[]>();//写入文件数据
    public boolean isRecording = false;// 录音线程控制标记
    private boolean isWriting = false;// 录音线程控制标记
    private String savePcmPath ;//保存pcm文件路径
    private String saveWavPath;//保存wav文件路径
    private String saveEncodePath;

    private RecordSession mSession = RecordSession.getInstance();

    /**
     * 录音数据拿取
     */
    private Runnable mReceiveRecordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                /**
                 * 此处有可能AudioRecord还没有初始化成功，audioRecordBufferSize为0
                 */
                int audioRecordBufferSize = RecordSession.getInstance().getRecordBufSize();
                short[] buffer = new short[audioRecordBufferSize];
                while (isRecording) {
                    if (mRevCycleBuffer.getUnreadLen() <= 0) {
                        //[todo]truyayong 这里生产者消费者模型，这里实现不够好
                        Thread.sleep(20);
                        continue;
                    }

                    /**
                     * 此处再check一下buffer是否已经初始化成功
                     */
                    if (buffer.length <= 0) {
                        audioRecordBufferSize = RecordSession.getInstance().getRecordBufSize();
                        buffer = new short[audioRecordBufferSize];
                    }
                    mReadsize = mRevCycleBuffer.read(buffer, audioRecordBufferSize);
                    synchronized (mPlayCycleBuffer) {
                        mPlayCycleBuffer.write(buffer, mReadsize);
                    }

                    Log.e(TAG, "readsize : " + mReadsize + " buffer size : " + buffer.length);
                    synchronized (inBuf) {
                        for (int i = 0; i < mReadsize; i += mRateX) {
                            inBuf.add(buffer[i]);
                        }
                    }
                    if (mOnRecordNotifyListner != null) {
                        mOnRecordNotifyListner.onData(inBuf);
                    }
                    if (AudioRecord.ERROR_INVALID_OPERATION != mReadsize) {
                        synchronized (mWriteData) {
                            byte  bys[] = new byte[mReadsize*2];
                            //因为arm字节序问题，所以需要高低位交换
                            for (int i = 0; i < mReadsize; i++) {
                                byte ss[] =	getBytes(buffer[i]);
                                bys[i*2] =ss[0];
                                bys[i*2+1] = ss[1];
                            }
                            mWriteData.add(bys);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "mReceiveRecordRunnable Exception e : ", e);
            }

        }
    };

    /**
     * 录音音频数据存文件
     */
    private Runnable mWrittingRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                FileOutputStream fos2wav = null;
                File file2wav = null;
                BufferedOutputStream bos = null;
                DataOutputStream dos = null;
                try {
                    file2wav = new File(savePcmPath);
                    if (file2wav.exists()) {
                        file2wav.delete();
                    }
                    fos2wav = new FileOutputStream(file2wav);// 建立一个可存取字节的文件
                    bos = new BufferedOutputStream(fos2wav);
                    dos = new DataOutputStream(bos);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mEncodePreBuffer.startProcess();
                mEncodeProcessor.start();

                while (isWriting ) {
                    int audioRecordBufferSize = RecordSession.getInstance().getRecordBufSize();
                    short[] buffer = new short[audioRecordBufferSize];
//                    synchronized (mWriteData) {
//                        if(mWriteData.size() > 0){
//                            buffer = mWriteData.get(0);
//                            mWriteData.remove(0);
//                        }
//                    }
                    int readSize;
                    readSize = mPlayCycleBuffer.read(buffer, audioRecordBufferSize);


                    short[] playBuffer = new short[readSize];
                    short[] aacBuffer = new short[readSize];

                    try {
                        if(buffer != null && readSize > 0){
                            for (int i = 0; i < readSize; i++) {
                                aacBuffer[i] = buffer[i];
                                playBuffer[i] = Short.reverseBytes(buffer[i]);
                                dos.writeShort(playBuffer[i]);
                                dos.flush();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mEncodePreBuffer.write(aacBuffer);
                }

                fos2wav.close();
                bos.close();
                dos.close();
                Pcm2Wav p2w = new Pcm2Wav();//将pcm格式转换成wav 其实就加了一个44字节的头信息
                p2w.convertAudioFiles(savePcmPath, saveWavPath,mSession.getOutSampleRate(), mSession.getOutChannels(), AudioFormat.ENCODING_PCM_16BIT);
            } catch (Exception e) {
                Log.e(TAG, "mWrittingRunnable Exception e : ", e);
            }
        }
    };

    public byte[] getBytes(short s)
    {
        byte[] buf = new byte[2];
        for (int i = 0; i < buf.length; i++)
        {
            buf[i] = (byte) (s & 0x00ff);
            s >>= 8;
        }
        return buf;
    }

    public RecordClient(String audioName,String path) {
        mRevCycleBuffer = new CycleBuffer(REV_CYCLE_BUFFER_SIZE);
        mPlayCycleBuffer = new CycleBuffer(PLAY_CYCLE_BUFFER_SIZE);
        mRecordProcessor = new RecordProcessor("RecordProcessor", mRevCycleBuffer);
        mAudioPlayProcessor = new AudioPlayProcessor("AudioPlayProcessor", mPlayCycleBuffer);

        mReceiveRecordThread = new Thread(mReceiveRecordRunnable,"ReceiveRecordThread");
        mWriteRunnableThread = new Thread(mWrittingRunnable, "WriteRunnableThread");
        savePcmPath = path + audioName +".pcm";
        saveWavPath = path + audioName +".wav";
        saveEncodePath = path + audioName + "encode" +".aac";

        mEncodePreBuffer = new EncodePreBuffer();
        mEncodeProcessor = new EncodeProcessor("EncodeProcessor", mEncodePreBuffer, saveEncodePath);
    }

    public void start() {
        isRecording = true;
        isWriting = true;
        mRecordProcessor.proccessStart();
        mReceiveRecordThread.start();
        mWriteRunnableThread.start();
//        mAudioPlayProcessor.proccessStart();
        if (mOnRecordNotifyListner != null) {
            mOnRecordNotifyListner.onStart();
        }
    }

    public void stop() {
        isRecording = false;
        isWriting = false;
        mRecordProcessor.proccessStop();
        mEncodePreBuffer.setStop(true);
//        mAudioPlayProcessor.proccessStop();
        if (mOnRecordNotifyListner != null) {
            mOnRecordNotifyListner.onStop();
        }
    }

    public void setOnRecordNotifyListner(OnRecordNotifyListner listner) {
        mOnRecordNotifyListner = listner;
    }

    public interface OnRecordNotifyListner{

        void onStart();

        void onData(ArrayList<Short> data);

        void onStop();
    }

}
