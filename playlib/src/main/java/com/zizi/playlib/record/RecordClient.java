package com.zizi.playlib.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.zizi.playlib.CycleBuffer;
import com.zizi.playlib.record.utils.Pcm2Wav;
import com.zizi.playlib.record.utils.RecordLogTag;

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

    private RecordProcessor mRecordProcessor;
    private CycleBuffer mRevCycleBuffer;
    private Thread mReceiveRecordThread;
    private Thread mWriteRunnableThread;
    private OnRecordNotifyListner mOnRecordNotifyListner;

    private int readsize;
    private ArrayList<Short> inBuf = new ArrayList<Short>();//缓冲区数据
    public int rateX = 100;//控制多少帧取一帧
    private ArrayList<byte[]> write_data = new ArrayList<byte[]>();//写入文件数据
    public boolean isRecording = false;// 录音线程控制标记
    private boolean isWriting = false;// 录音线程控制标记
    private String savePcmPath ;//保存pcm文件路径
    private String saveWavPath;//保存wav文件路径

    /**
     * 录音数据拿取
     */
    private Runnable mReceiveRecordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                /**
                 * 此处有可能AudioRecord还没有初始化成功
                 */
                int audioRecordBufferSize = mRecordProcessor.getAudioRecordBufferSize();
                short[] buffer = new short[audioRecordBufferSize];
                while (isRecording) {
                    // 从MIC保存数据到缓冲区
                    if (mRevCycleBuffer.getUnreadLen() <= 0) {
                        //[todo]truyayong 这里生产者消费者模型，这里实现不够好
                        Thread.sleep(20);
                        continue;
                    }

                    /**
                     *
                     */
                    if (buffer.length <= 0) {
                        audioRecordBufferSize = mRecordProcessor.getAudioRecordBufferSize();
                        buffer = new short[audioRecordBufferSize];
                    }
                    readsize = mRevCycleBuffer.read(buffer, audioRecordBufferSize);

                    Log.e(TAG, "readsize : " + readsize + " buffer size : " + buffer.length);
                    synchronized (inBuf) {
                        for (int i = 0; i < readsize; i += rateX) {
                            inBuf.add(buffer[i]);
                        }
                    }
                    if (mOnRecordNotifyListner != null) {
                        mOnRecordNotifyListner.onData(inBuf);
                    }
                    if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                        synchronized (write_data) {
                            byte  bys[] = new byte[readsize*2];
                            //因为arm字节序问题，所以需要高低位交换
                            for (int i = 0; i < readsize; i++) {
                                byte ss[] =	getBytes(buffer[i]);
                                bys[i*2] =ss[0];
                                bys[i*2+1] = ss[1];
                            }
                            write_data.add(bys);
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
                try {
                    file2wav = new File(savePcmPath);
                    if (file2wav.exists()) {
                        file2wav.delete();
                    }
                    fos2wav = new FileOutputStream(file2wav);// 建立一个可存取字节的文件
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (isWriting || write_data.size() > 0) {
                    byte[] buffer = null;
                    synchronized (write_data) {
                        if(write_data.size() > 0){
                            buffer = write_data.get(0);
                            write_data.remove(0);
                        }
                    }
                    try {
                        if(buffer != null){
                            fos2wav.write(buffer);
                            fos2wav.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                fos2wav.close();
                Pcm2Wav p2w = new Pcm2Wav();//将pcm格式转换成wav 其实就加了一个44字节的头信息
                p2w.convertAudioFiles(savePcmPath, saveWavPath);
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
        mRecordProcessor = new RecordProcessor("RecordProcessor", mRevCycleBuffer);
        mReceiveRecordThread = new Thread(mReceiveRecordRunnable,"ReceiveRecordThread");
        mWriteRunnableThread = new Thread(mWrittingRunnable, "WriteRunnableThread");
        savePcmPath = path + audioName +".pcm";
        saveWavPath = path + audioName +".wav";
    }

    public void start() {
        isRecording = true;
        isWriting = true;
        mRecordProcessor.proccessStart();
        mReceiveRecordThread.start();
        mWriteRunnableThread.start();
        if (mOnRecordNotifyListner != null) {
            mOnRecordNotifyListner.onStart();
        }
    }

    public void stop() {
        isRecording = false;
        isWriting = false;
        mRecordProcessor.proccessStop();
        if (mOnRecordNotifyListner != null) {
            mOnRecordNotifyListner.onStop();
        }
    }

    public CycleBuffer getRecCycleBuffer() {
        return mRevCycleBuffer;
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
