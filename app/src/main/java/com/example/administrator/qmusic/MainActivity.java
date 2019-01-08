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
    private SeekBar mVolumeBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayJniProxy = new PlayJniProxy();
        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
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
    }

    public void onPrepare(View view) {
        mPlayJniProxy.prepare(playUrl, 0, 0, 0);
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
}
