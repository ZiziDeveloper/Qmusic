package com.zizi.playlib.record;

import com.zizi.playlib.CycleBuffer;

/**
 * 录音功能对外接口
 */
public class RecordClient {
    private static final int REV_CYCLE_BUFFER_SIZE = 100 * 1024;

    private RecordProcessor mRecordProcessor;
    private CycleBuffer mRevCycleBuffer;

    public RecordClient() {
        mRevCycleBuffer = new CycleBuffer(REV_CYCLE_BUFFER_SIZE);
        mRecordProcessor = new RecordProcessor("RecordProcessor", mRevCycleBuffer);
    }

    public void start() {
        mRecordProcessor.proccessStart();
    }

    public void stop() {
        mRecordProcessor.proccessStop();
    }

    public CycleBuffer getRecCycleBuffer() {
        return mRevCycleBuffer;
    }
}
