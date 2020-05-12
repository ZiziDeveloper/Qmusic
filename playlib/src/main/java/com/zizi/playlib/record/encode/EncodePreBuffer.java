package com.zizi.playlib.record.encode;

import android.app.ActivityManager;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.zizi.playlib.nativeUtils.FileUtilJniProxy;
import com.zizi.playlib.record.utils.ApplicationUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 音频编码数据前处理：先将pcm数据存到临时文件，再读出来进行编码
 * 数据处理流程：
 *     写数据：数据先写到mEncodeBuffer缓存，缓存满了再写入文件（writeBufferToFile）
 *     读数据：数据先从mEncodeBuffer缓存读，当缓存剩余的数据只能读20次时，再从文件里面读（readBufferFromFile）到mEncodeBuffer
 */
public class EncodePreBuffer implements Handler.Callback {
    private static final String TAG = "EncodePreBuffer";

    private static final int MIN_ENCODE_BUFFER_SIZE = 1024 * 8;

    private static final int MSG_WRITE_BUFFER = 1;
    private static final int MSG_READ_BUFFER = 2;

    private HandlerThread mFileDataThread;
    private Handler mFileDataHandler;
    private FileUtilJniProxy mFileUtilJniProxy;
    private int mFd = -1;

    /**
     * PCM数据缓存
     */
    private short[] mEncodeBuffer;

    //文件读seek位置
    private int mReadFileOffset = 0;
    //文件写seek位置
    private int mWriteFileOffset = 0;
    //buffer有效数据开始位置
    private int mBufferStartPos;
    //buffer有效数据结束位置
    private int mBufferEndPos;

    private File mTempDataFile;

    private EncodeData mEncodeData;

    private boolean mIsStop = false;

    public EncodePreBuffer() {

        if (getMaxBufferSize() > MIN_ENCODE_BUFFER_SIZE) {
            mEncodeBuffer = new short[256 * 1024];
        } else {
            mEncodeBuffer = new short[MIN_ENCODE_BUFFER_SIZE];
        }

        if (mEncodeBuffer == null) {
            throw new UnsupportedOperationException("mEncodeBuffer is null, can not encode");
        }

        mFileUtilJniProxy = new FileUtilJniProxy();

        mFileDataThread = new HandlerThread("file_data_thread");
    }

    public void startProcess() {
        if (mFileDataThread != null) {
            mFileDataThread.start();
            mFileDataHandler = new Handler(mFileDataThread.getLooper(), this);
        }
    }



    public EncodeData read() {

        if (mEncodeData == null) {
            mEncodeData = new EncodeData();
        }

        if (mBufferEndPos == mBufferStartPos && mReadFileOffset == mWriteFileOffset) {
            mEncodeData.validateLength = 0;
            if (mIsStop) {
                mEncodeData.mData = null;
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException : " + e);
            }
            Log.e(TAG, "read mBufferEndPos : " + mBufferEndPos + " mBufferStartPos : " + mBufferStartPos);
            Log.e(TAG, "read mReadFileOffset : " + mReadFileOffset + " mWriteFileOffset : " + mWriteFileOffset);
            Log.e(TAG, "read mEncodeData.validateLength : " + mEncodeData.validateLength + " mEncodeData.validateLength : " + mEncodeData.validateLength);
            return mEncodeData;

        }

        synchronized (this) {
            if (mBufferEndPos - mBufferStartPos > 0) {
                mEncodeData.validateLength = (mBufferEndPos - mBufferStartPos) >
                        mEncodeData.mData.length ? mEncodeData.mData.length : (mBufferEndPos - mBufferStartPos);
                System.arraycopy(mEncodeBuffer, mBufferStartPos, mEncodeData.mData, 0, mEncodeData.validateLength);

                mBufferStartPos = mEncodeData.validateLength;
                System.arraycopy(mEncodeBuffer, mBufferStartPos, mEncodeBuffer, 0, mBufferEndPos - mBufferStartPos);
            }
            mBufferStartPos = 0;
            mBufferEndPos = mBufferEndPos - mEncodeData.validateLength < 0 ? 0 : mBufferEndPos - mEncodeData.validateLength;

            /**
             * 缓存剩余数据小于每次读取数据的20倍时，触发从文件读
             */
            if (mEncodeData.validateLength != 0 && mBufferEndPos / mEncodeData.validateLength < 20) {
                Message m = mFileDataHandler.obtainMessage(MSG_READ_BUFFER);
                mFileDataHandler.sendMessage(m);
            }
        }

        return mEncodeData;
    }

    public synchronized void write(short[] data) {
        if (mReadFileOffset < mWriteFileOffset) {
            postWriteToFile(data, 0, data.length);
        } else if ((mBufferEndPos - mBufferStartPos) + data.length > mEncodeBuffer.length) {
            int fillLength = mEncodeBuffer.length - mBufferEndPos;
            System.arraycopy(data, 0, mEncodeBuffer, mBufferEndPos, mEncodeBuffer.length - mBufferEndPos);
            mBufferEndPos = mEncodeBuffer.length;
            postWriteToFile(data, fillLength, data.length - fillLength);
        } else {
            System.arraycopy(data, 0, mEncodeBuffer, mBufferEndPos, data.length);
            mBufferEndPos += data.length;
        }
    }

    public void setStop(boolean isStop) {
        mIsStop = isStop;
        if (mIsStop) {
            mFileUtilJniProxy.nativeClose(mFd);
        }
    }

    private void postWriteToFile(short[] data, int offset, int length) {
        Message m = mFileDataHandler.obtainMessage(MSG_WRITE_BUFFER, offset, length, data);
        mFileDataHandler.sendMessage(m);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_WRITE_BUFFER:
                writeBufferToFile((short[])msg.obj, msg.arg1, msg.arg2);
                break;
            case MSG_READ_BUFFER:
                readBufferFromFile();
                break;
        }
        return false;
    }


    private int getMaxBufferSize() {
        return (getJVMHeapsize() - getProcessDirtySize(Process.myPid())) / (4 * 2);
    }

    private int getJVMHeapsize() {
        int heapSize = 0;
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getFunction = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            String value = (String) getFunction.invoke(classType, new Object[]{"dalvik.vm.heapsize"});
            heapSize = Integer.parseInt(value.substring(0, value.indexOf("m"))) * 1024 * 1024;
        } catch (Exception e) {

        }
        return heapSize;
    }

    private int getProcessDirtySize(int pid) {
        ActivityManager activityManager = (ActivityManager) ApplicationUtil.getContext()
                .getSystemService(ApplicationUtil.getContext().ACTIVITY_SERVICE);
        int[] pids = new int[]{pid};
        Debug.MemoryInfo memoryInfo = activityManager.getProcessMemoryInfo(pids)[0];
        return memoryInfo == null ? 0 : (memoryInfo.getTotalPrivateDirty() * 1024);
    }

    private void writeBufferToFile(short[] data, int offset, int length) {
        if (mTempDataFile == null) {
            try {
                mTempDataFile = new File(ApplicationUtil.getContext().getFilesDir() + "recording_cache.dat");
                if (mTempDataFile.exists()) {
                    mTempDataFile.delete();
                }
                mTempDataFile.createNewFile();
            } catch (IOException e) {

            }
        }

        if (mFd < 0 && mTempDataFile != null) {
            mFd = mFileUtilJniProxy.nativeOpenFile(mTempDataFile.getAbsolutePath());
            if (mFd < 0) {
                // 打开文件失败
                return;
            }
        }
        if (!mFileUtilJniProxy.nativeWriteFile(mFd, mWriteFileOffset, data, offset, length)) {
            // 写入数据到文件失败
            return;
        }

        mWriteFileOffset += length;
    }

    private void readBufferFromFile() {

        if (mTempDataFile == null) {
            return;
        }

        if (mReadFileOffset == mWriteFileOffset) {
            return;
        }

        if (mFd < 0) {
            mFd = mFileUtilJniProxy.nativeOpenFile(mTempDataFile.getAbsolutePath());
            if (mFd < 0) {
                return;
            }
        }

        int dataCount = Math.min(mWriteFileOffset-mReadFileOffset, mEncodeBuffer.length - mBufferEndPos);
        mFileUtilJniProxy.nativeReadFile(mFd, mReadFileOffset, mEncodeBuffer, mBufferEndPos, dataCount);

        mReadFileOffset += dataCount;
        mBufferEndPos += dataCount;

    }


}
