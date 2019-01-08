package com.example.playlib;

import android.util.Log;

/**
 * Created by Administrator on 2018/12/13 0013.
 */

public class PlayJniProxy {
    private static final String TAG = "PlayJniProxy";

    static {
        System.loadLibrary("PlayNative");
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("swscale-4");
        System.loadLibrary("postproc-54");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avdevice-57");
    }

    public static final int PLAY_CHANNEL_RIGHT = 0;
    public static final int PLAY_CHANNEL_LEFT = 1;
    public static final int PLAY_CHANNEL_STEREO = 2;

    /**
     *暴露给java层的api
     *
     */
    public void prepare(final String source, final int volume, final int playState, final int mutesole) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                native_prepare(source, volume, playState,mutesole );
            }
        }).start();
    }

    public void start() {
        native_start();
    }

    public void resume() {
        native_resume();
    }

    public void pause() {
        native_pause();
    }

    public void stop() {
        native_stop();
    }

    public void seek(int progress) {
        native_seek(progress);
    }

    public int duration() {
        return native_duration();
    }

    public void setVolume(int percent) {
        if (percent < 0 || percent > 100) {
            return;
        }
        native_volume(percent);
    }

    public void switchChannel(int channel) {
        native_channel_switch(channel);
    }

    public void setPitch(float pitch) {
        native_pitch(pitch);
    }

    public void setSpeed(float speed) {
        native_speed(speed);
    }

    public int getSamplerate(){
        return native_samplerate();
    }

    /**
     * native方法
     */
    private native void native_prepare(String source, int volume, int playState, int mutesole);
    private native void native_start();
    private native void native_resume();
    private native void native_pause();
    private native void native_stop();
    private native void native_seek(int progress);
    private native int native_duration();
    private native void native_volume(int percent);
    private native void native_channel_switch(int channel);
    private native void native_pitch(float pitch);
    private native void native_speed(float speed);
    private native int native_samplerate();

    /**
     * native层通知应用层接口
     */
    private void onPrepared() {
        Log.e(TAG, "qmusic onPrepared");
    }
    private void onError(int code, String msg) {
        Log.e(TAG, "qmusic onError");
    }
}
