package com.zizi.qmusic;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.zizi.playlib.PlayJniProxy;
import com.zizi.qmusic.qmusic.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int PLAYSTATE_INIT = -1;
    private static final int PLAYSTATE_START = 0;
    private static final int PLAYSTATE_RESUME = 1;
    private static final int PLAYSTATE_PAUSE = 2;
    private static final int PLAYSTATE_STOP = 3;
    private static final int PLAYSTATE_NEXT = 4;

    PlayJniProxy mPlayJniProxy;
    private String playUrl = "http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3";
    private String nextUrl = "http://ngcdn004.cnr.cn/live/dszs/index.m3u8";
    private String ape = Environment.getExternalStorageDirectory() + File.separator + "被风吹过的夏天.ape";
    private String Aape = Environment.getExternalStorageDirectory() + File.separator + "SideA-small.ape";
    public String local = "/storage/emulated/0/1.mp3";
    private SeekBar mVolumeBar;
    private SeekBar mTimeBar;
    private ProgressBar mClockBar;
    private boolean mPlayNext = false;
    private int mPlayState = PLAYSTATE_INIT;
    private int mVolume = 85;
    private int mChannelLayout = PlayJniProxy.PLAY_CHANNEL_STEREO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAudioPlay();
        mVolumeBar = (SeekBar) findViewById(R.id.seek_volume);
        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPlayJniProxy.setVolume(progress);
                Log.i(TAG, "mVolumeBar progress : " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mClockBar = (ProgressBar) findViewById(R.id.progress_time);
        mVolumeBar.setProgress(mVolume);
        mTimeBar = (SeekBar) findViewById(R.id.seek_time);
        mTimeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPlayJniProxy.seek(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initAudioPlay() {
        mPlayJniProxy = new PlayJniProxy();
        mPlayJniProxy.setPlayProgressCallBack(new PlayJniProxy.PlayProgressCallBack() {
            @Override
            public void onPrepared() {
                mPlayJniProxy.start();
            }

            @Override
            public void onStarted() {
                mPlayState = PLAYSTATE_START;
            }

            @Override
            public void onResumed() {
                mPlayState = PLAYSTATE_RESUME;
            }

            @Override
            public void onPaused() {
                mPlayState = PLAYSTATE_PAUSE;
            }

            @Override
            public void onStopped() {
                if (mPlayNext) {
                    mPlayJniProxy.prepare();
                    mPlayNext = false;
                } else {
                    mPlayState = PLAYSTATE_STOP;
                }
            }

            @Override
            public void onSeeked(int progress) {
                mTimeBar.setProgress(progress);
            }

            @Override
            public void onVolumeModified(int percent) {
                mVolume = percent;
            }

            @Override
            public void onChannelLayoutModify(int layout) {

            }

            @Override
            public void onPitchModified(float pitch) {

            }

            @Override
            public void onSpeedModified(float speed) {

            }

            @Override
            public void onError(final int code, final String msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "error code : " + code + " msg : " + msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onPlayProgress(final float currentProgress, final int total) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int progress = (int)(currentProgress / (float) total * 100.0f);
                        mClockBar.setProgress(progress);
                    }
                });
            }
        });
    }

    public void onStart(View view) {
        if (mPlayState == PLAYSTATE_START) {
            return;
        }
        mPlayJniProxy.prepare(playUrl,  mVolume, mChannelLayout);
    }

    public void onPause(View view) {
        if (mPlayState == PLAYSTATE_PAUSE) {
            return;
        }
        mPlayJniProxy.pause();
    }

    public void onResume(View view) {
        if (mPlayState == PLAYSTATE_RESUME) {
            return;
        }
        mPlayJniProxy.resume();
    }

    public void onStop(View view) {
        if (mPlayState == PLAYSTATE_STOP) {
            return;
        }
        mPlayJniProxy.stop();
    }

    public void onLeft(View view) {
        mPlayJniProxy.switchChannel(PlayJniProxy.PLAY_CHANNEL_LEFT);
    }

    public void onStereo(View view) {
        mPlayJniProxy.switchChannel(PlayJniProxy.PLAY_CHANNEL_STEREO);
    }

    public void onRight(View view) {
        mPlayJniProxy.switchChannel(PlayJniProxy.PLAY_CHANNEL_RIGHT);
    }

    public void onPitch(View view) {
        mPlayJniProxy.setPitch(1.5f);
    }

    public void onNormal(View view) {
        mPlayJniProxy.setPitch(1.0f);
        mPlayJniProxy.setSpeed(1.0f);
    }

    public void onSpeed(View view) {
        mPlayJniProxy.setSpeed(1.5f);
    }

    public void onNext(View view) {
        mPlayNext = true;
        mPlayState = PLAYSTATE_NEXT;
        mPlayJniProxy.next(playUrl);
    }
}
