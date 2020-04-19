package com.zizi.playlib.record;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.nio.ShortBuffer;

public class SamplePlayer {
    public interface OnCompletionListener {
        public void onCompletion();
    };

    private ShortBuffer mSamples;
    private int mSampleRate;
    private int mChannels;
    private int mNumSamples;  // Number of samples per channel.
    private AudioTrack mAudioTrack;
    private short[] mBuffer;
    private int mPlaybackStart;  // Start offset, in samples.
    private Thread mPlayThread;
    private boolean mKeepPlaying;
    private OnCompletionListener mListener;

    private boolean mIsPlaying;

    public SamplePlayer(ShortBuffer samples, int sampleRate, int channels, int numSamples) {
        mSamples = samples;
        mSampleRate = sampleRate;
        mChannels = channels;
        mNumSamples = numSamples;
        mPlaybackStart = 0;

        int bufferSize = AudioTrack.getMinBufferSize(
                mSampleRate,
                mChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        // make sure minBufferSize can contain at least 1 second of audio (16 bits sample).
        if (bufferSize < mChannels * mSampleRate * 2) {
            bufferSize = mChannels * mSampleRate * 2;
        }
        mBuffer = new short[bufferSize/2]; // 緩衝區大小是以字節為單位.
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                mSampleRate,
                mChannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                mBuffer.length * 2,
                AudioTrack.MODE_STREAM);
        // Check when player played all the given data and notify user if mListener is set.
        mPlayThread = null;
        mKeepPlaying = true;
        mListener = null;
    }

    public SamplePlayer(SoundFile sf) {
        this(sf.getSamples(), sf.getSampleRate(), sf.getChannels(), sf.getNumSamples());
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        mListener = listener;
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public boolean isPaused() {
        return !mIsPlaying;
    }

    public void start() {
        if (isPlaying()) {
            return;
        }

        mIsPlaying = true;
        mKeepPlaying = true;
        mAudioTrack.flush();
        mAudioTrack.play();
        // Setting thread feeding the audio samples to the audio hardware.
        // (Assumes mChannels = 1 or 2).
        mPlayThread = new Thread () {
            public void run() {
                int position = mPlaybackStart * mChannels;
                mSamples.position(position);
                int limit = mNumSamples * mChannels;
                while (mSamples.position() < limit && mKeepPlaying) {


                    int numSamplesLeft = limit - mSamples.position();
                    if(numSamplesLeft >= mBuffer.length) {
                        mSamples.get(mBuffer);//-从PCM文件中，以流的形式读出存放在 mBuffer 中，將此緩衝區的短路傳輸到給定的目標數組
                    } else {
                        for(int i=numSamplesLeft; i<mBuffer.length; i++) {
                            mBuffer[i] = 0;
                        }
                        mSamples.get(mBuffer, 0, numSamplesLeft);//取出shortBuffer中的short数组
                    }
                    // 使用以ByteBuffer為參數的write方法
                    mAudioTrack.write(mBuffer, 0, mBuffer.length);//write是从你的buffer里取数据，write到audiotrack的缓冲中去，而且每次只能write有限的长度，因为缓冲空间是有限的

                }
                /**
                 * 流式播放的时候，跳出循环说明已经播放完毕
                 */
                mIsPlaying = false;
                if (mListener != null) {
                    mListener.onCompletion();
                    SamplePlayer.this.stop();
                }
            }
        };
        mPlayThread.start();
    }

    public void pause() {
        if (isPlaying()) {
            mAudioTrack.pause();
            // mAudioTrack.write() should block if it cannot write.
        }
        mIsPlaying = false;
    }
    int tag = 0;
    public void stop() {
        if (isPlaying() || isPaused()) {
            mKeepPlaying = false;
            mAudioTrack.pause();  // pause() stops the playback immediately.
            mAudioTrack.stop();   // Unblock mAudioTrack.write() to avoid deadlocks.
            if (mPlayThread != null) {
                try {
                    mPlayThread.join();
                } catch (InterruptedException e) {
                }
                mPlayThread = null;
            }
            mAudioTrack.flush();  // just in case...
        }
        if(tag==0){ //First tag
            mKeepPlaying = false;
            mAudioTrack.pause();  // pause() stops the playback immediately.
            mAudioTrack.stop();   // Unblock mAudioTrack.write() to avoid deadlocks.
            if (mPlayThread != null) {
                try {
                    mPlayThread.join();
                } catch (InterruptedException e) {
                }
                mPlayThread = null;
            }
            mAudioTrack.flush();  // just in case...
            tag++; //add tag state
        }
        mIsPlaying = false;
    }

    public void release() {
        stop();
        mAudioTrack.release();
    }

    public void seekTo(int msec) {
        tag = 0;
        boolean wasPlaying = isPlaying();
        stop();
        mPlaybackStart = (int)(msec * (mSampleRate / 1000.0));
        if (mPlaybackStart > mNumSamples) {
            mPlaybackStart = mNumSamples;  // Nothing to play...
        }
        mAudioTrack.setNotificationMarkerPosition(mNumSamples - 1 - mPlaybackStart);
        if (wasPlaying) {
            start();
        }
    }

    public int getCurrentPosition() {
        return (int)((mPlaybackStart + mAudioTrack.getPlaybackHeadPosition()) *  (1000.0 / mSampleRate));
    }
}
