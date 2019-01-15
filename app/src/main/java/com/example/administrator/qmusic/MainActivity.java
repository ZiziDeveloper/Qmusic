package com.example.administrator.qmusic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.playlib.PlayJniProxy;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    PlayJniProxy mPlayJniProxy;
    private String playUrl = "http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3";
    private String nextUrl = "http://ngcdn004.cnr.cn/live/dszs/index.m3u8";
    private SeekBar mVolumeBar;
    private SeekBar mTimeBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayJniProxy = new PlayJniProxy();
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

    public void onPrepare(View view) {
        mPlayJniProxy.prepare(nextUrl, 0, 0, 0);
    }

    public void onStart(View view) {
        mPlayJniProxy.start();
    }

    public void onPause(View view) {
        mPlayJniProxy.pause();
    }

    public void onResume(View view) {
        mPlayJniProxy.resume();
    }

    public void onStop(View view) {
        mPlayJniProxy.stop(PlayJniProxy.NOT_PLAY_NEXT);
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
        mPlayJniProxy.next(playUrl);
    }
}
