package com.zizi.qmusic.record;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zizi.qmusic.componets.waveComponent.draw.WaveCanvas;
import com.zizi.qmusic.componets.waveComponent.utils.SamplePlayer;
import com.zizi.qmusic.componets.waveComponent.utils.SoundFile;
import com.zizi.qmusic.componets.waveComponent.view.FileToWaveView;
import com.zizi.qmusic.componets.waveComponent.view.WaveSurfaceView;
import com.zizi.qmusic.qmusic.R;
import com.zizi.qmusic.utils.U;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class AudioRecorderActivity extends AppCompatActivity implements View.OnClickListener , SamplePlayer.OnCompletionListener{

    private int color;
    private boolean autoStart;
    private boolean keepDisplayOn;

    private Timer timer;
    private MenuItem saveMenuItem;
    private int recorderSecondsElapsed;
    private int playerSecondsElapsed;
    private boolean isRecording;

    private RelativeLayout contentLayout;
    private TextView statusView;
    private TextView timerView;
    private ImageButton restartView;
    private ImageButton recordView;
    private ImageButton playView;


    private WaveCanvas waveCanvas;
    private String mFileName = "test_new";//文件名

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        if(savedInstanceState != null) {
            color = savedInstanceState.getInt(AndroidAudioRecorder.EXTRA_COLOR);
            autoStart = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_AUTO_START);
            keepDisplayOn = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON);
        } else {
            color = getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_COLOR, Color.BLACK);
            autoStart = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_AUTO_START, false);
            keepDisplayOn = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON, false);
        }

        if(keepDisplayOn){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(Util.getDarkerColor(color)));
            getSupportActionBar().setHomeAsUpIndicator(
                    ContextCompat.getDrawable(this, R.drawable.aar_ic_clear));
        }


        contentLayout = (RelativeLayout) findViewById(R.id.content);
        statusView = (TextView) findViewById(R.id.status);
        timerView = (TextView) findViewById(R.id.timer);
        restartView = (ImageButton) findViewById(R.id.btn_restart);
        restartView.setOnClickListener(this);
        recordView = (ImageButton) findViewById(R.id.btn_record);
        recordView.setOnClickListener(this);
        playView = (ImageButton) findViewById(R.id.btn_listen);
        playView.setOnClickListener(this);

        contentLayout.setBackgroundColor(Util.getDarkerColor(color));
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);

        if(Util.isBrightColor(color)) {
            ContextCompat.getDrawable(this, R.drawable.aar_ic_clear)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            ContextCompat.getDrawable(this, R.drawable.aar_ic_check)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            statusView.setTextColor(Color.BLACK);
            timerView.setTextColor(Color.BLACK);
            restartView.setColorFilter(Color.BLACK);
            recordView.setColorFilter(Color.BLACK);
            playView.setColorFilter(Color.BLACK);
        }

        initWave();
    }

    WaveSurfaceView waveSfv;
    FileToWaveView waveView;
    private void initWave() {
        waveSfv = findViewById(R.id.wavesfv);
        waveView = findViewById(R.id.waveview);

        if(waveSfv != null) {
            waveSfv.setLine_off(42);
            //解决surfaceView黑色闪动效果
            waveSfv.setZOrderOnTop(true);
            waveSfv.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
        waveView.setLine_offset(42);
    }

    /**
     * 开始录音
     */
    private void startAudio(){
        waveCanvas = new WaveCanvas();
        waveCanvas.baseLine = waveSfv.getHeight() / 2;
        waveCanvas.Start(waveSfv, mFileName, U.DATA_DIRECTORY, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return true;
            }
        });
    }

    private void  initWaveView(){
        loadFromFile();
    }

    File mFile;
    Thread mLoadSoundFileThread;
    SoundFile mSoundFile;
    boolean mLoadingKeepGoing;
    SamplePlayer mPlayer;
    /** 载入wav文件显示波形 */
    private void loadFromFile() {
        try {
            Thread.sleep(300);//让文件写入完成后再载入波形 适当的休眠下
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mFile = new File(U.DATA_DIRECTORY + mFileName + ".wav");
        mLoadingKeepGoing = true;
        // Load the sound file in a background thread
        try {
            mSoundFile = SoundFile.create(mFile.getAbsolutePath(),null);
            if (mSoundFile == null) {
                return;
            }
            mPlayer = new SamplePlayer(mSoundFile);
            mPlayer.setOnCompletionListener(this);
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        mLoadSoundFileThread = new Thread() {
            public void run() {

                if (mLoadingKeepGoing) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            finishOpeningSoundFile();
                            waveSfv.setVisibility(View.INVISIBLE);
                            waveView.setVisibility(View.VISIBLE);
                        }
                    };
                    AudioRecorderActivity.this.runOnUiThread(runnable);
                }
            }
        };
        mLoadSoundFileThread.start();
    }

    float mDensity;
    /**waveview载入波形完成*/
    private void finishOpeningSoundFile() {
        waveView.setSoundFile(mSoundFile);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;
        waveView.recomputeHeights(mDensity);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(autoStart && !isRecording){
            clickRecording();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        clickRestart();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clickRestart();
        setResult(RESULT_CANCELED);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(AndroidAudioRecorder.EXTRA_FILE_PATH, mFile.toString());
        outState.putInt(AndroidAudioRecorder.EXTRA_COLOR, color);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.audio_recorder, menu);
        saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.aar_ic_check));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCompletion() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopListening();
            }
        });
    }


    public void clickRecording() {
        stopListening();
        Util.wait(100, new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });
    }

    public void clickListen(){
        stopRecording();
        Util.wait(100, new Runnable() {
            @Override
            public void run() {
                if(isPlaying()){
                    stopListening();
                } else {
                    startListening();
                }
            }
        });
    }

    public void clickRestart(){
        if(isRecording) {
            stopRecording();
        } else if(isPlaying()) {
            stopListening();
        } else {
        }
        saveMenuItem.setVisible(false);
        statusView.setVisibility(View.INVISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.ic_record_pause);
        timerView.setText("00:00:00");
        recorderSecondsElapsed = 0;
        playerSecondsElapsed = 0;
    }

    private void startRecording() {
        isRecording = true;
        saveMenuItem.setVisible(false);
        statusView.setText(R.string.aar_recording);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.ic_recording);
        playView.setImageResource(R.drawable.ic_listen);


        if (waveCanvas == null || !waveCanvas.isRecording) {
            U.createDirectory();
            waveSfv.setVisibility(View.VISIBLE);
            waveView.setVisibility(View.INVISIBLE);
            startAudio();

        }
        timerView.setText("00:00:00");

        startTimer();
    }

    private void stopRecording() {
        isRecording = false;
        if(!isFinishing()) {
            saveMenuItem.setVisible(true);
        }
        statusView.setText(R.string.aar_paused);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.VISIBLE);
        playView.setVisibility(View.VISIBLE);
        recordView.setImageResource(R.drawable.ic_record_pause);
        playView.setImageResource(R.drawable.ic_listen);



        stopTimer();
        if (waveCanvas != null) {
            waveCanvas.Stop();
            waveCanvas = null;
        }
        initWaveView();
    }


    private void startListening(){
        try {
            if (mPlayer != null && !mPlayer.isPlaying()) {
                mPlayer.start();
            }

            timerView.setText("00:00:00");
            statusView.setText(R.string.aar_playing);
            statusView.setVisibility(View.VISIBLE);
            playView.setImageResource(R.drawable.ic_listening);

            playerSecondsElapsed = 0;
            startTimer();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void stopListening(){
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
        }

        statusView.setText("");
        statusView.setVisibility(View.INVISIBLE);
        playView.setImageResource(R.drawable.ic_listen);

        stopTimer();
    }

    private boolean isPlaying(){
        try {
            return mPlayer!=null && mPlayer.isPlaying() && !isRecording;
        } catch (Exception e){
            return false;
        }
    }

    private void startTimer(){
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    private void stopTimer(){
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private void updateTimer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isRecording) {
                    recorderSecondsElapsed++;
                    timerView.setText(Util.formatSeconds(recorderSecondsElapsed));
                } else if(isPlaying()){
                    playerSecondsElapsed++;
                    timerView.setText(Util.formatSeconds(playerSecondsElapsed));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_record) {
            clickRecording();
        } else if (v.getId() == R.id.btn_listen) {
            clickListen();
        } else if (v.getId() == R.id.btn_restart) {
            clickRestart();
        }
    }
}
