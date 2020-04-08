package com.zizi.playlib.record;

/**
 * 录音过程中共享数据Session
 */
public class RecordSession {

    private static RecordSession INSTANCE = new RecordSession();

    private RecordSession() {
    }

    public static RecordSession getInstance() {
        return INSTANCE;
    }

    private int mRecordBufSize = 0;

    private int mPlayBufSize = 0;

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
}
