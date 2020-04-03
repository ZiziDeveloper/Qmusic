package com.zizi.playlib;

public class CycleBuffer {
    private int mBufferLen = 0;
    private int mReadPos = 0;
    private int mWritePos = 0;
    private int mUnreadLen = 0;
    private short[] mBuffer = null;

    public CycleBuffer(int size) {
        if (size <= 0) {
            return;
        }
        mBufferLen = size;
        mBuffer = new short[size];
        mWritePos = 0;
        mReadPos = 0;
        mUnreadLen = 0;
    }

    public int read(short[] readBuffer, int size) {
        if ((size <= 0) || (mUnreadLen < size)) {
            return 0;
        }
        if (mBuffer == null) {
            return 0;
        }

        int len = size;
        if (mReadPos + len < mBufferLen) {
            System.arraycopy(mBuffer, mReadPos, readBuffer, 0, len);
            mReadPos += len;
            mUnreadLen -= len;
        } else {
            System.arraycopy(mBuffer, mReadPos, readBuffer, 0, mBufferLen - mReadPos);
            System.arraycopy(mBuffer, 0, readBuffer, mBufferLen - mReadPos, len - (mBufferLen - mReadPos));
            mReadPos = mReadPos + len - mBufferLen;
            mUnreadLen -= len;
        }

        return len;
    }

    public int write(short[] writeBuffer, int size) {
        if ((size <= 0) || ((mUnreadLen + size) >= mBufferLen)) {
            return 0;
        }
        if (mBuffer == null) {
            return 0;
        }

        int len = size;
        if (mWritePos + size < mBufferLen) {
            System.arraycopy(writeBuffer, 0, mBuffer, mWritePos, len);
            mWritePos += len;
            mUnreadLen += len;
        } else {
            System.arraycopy(writeBuffer, 0, mBuffer, mWritePos, mBufferLen - mWritePos);
            System.arraycopy(writeBuffer, mBufferLen - mWritePos, mBuffer, 0, len - (mBufferLen - mWritePos));
            mWritePos = mWritePos + len - mBufferLen;
            mUnreadLen += len;
        }
        return len;
    }

    public int getBufferLen() {
        return mBufferLen;
    }

    public int getUnreadLen() {
        return mUnreadLen;
    }

    public void release() {
        if (mBuffer != null) {
            mWritePos = 0;
            mReadPos = 0;
            mUnreadLen = 0;
            mBufferLen = 0;
            mBuffer = null;
        }

    }
}
