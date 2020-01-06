package com.zizi.qmusic;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zizi.playlib.PlayJniProxy;
import com.zizi.qmusic.componets.waveComponent.draw.WaveCanvas;
import com.zizi.qmusic.componets.waveComponent.utils.SamplePlayer;
import com.zizi.qmusic.componets.waveComponent.utils.SoundFile;
import com.zizi.qmusic.componets.waveComponent.view.WaveSurfaceView;
import com.zizi.qmusic.componets.waveComponent.view.WaveformView;
import com.zizi.qmusic.qmusic.R;
import com.zizi.qmusic.utils.MusicSimilarityUtil;
import com.zizi.qmusic.utils.U;

import java.io.File;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
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


    WaveSurfaceView waveSfv;
    Button switchBtn;
    TextView status;
    WaveformView waveView;
    Button playBtn;
    Button scoreBtn;



    private static final int FREQUENCY = 16000;// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static final int CHANNELCONGIFIGURATION = AudioFormat.CHANNEL_IN_MONO;// 设置单声道声道
    private static final int AUDIOENCODING = AudioFormat.ENCODING_PCM_16BIT;// 音频数据格式：每个样本16位
    public final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;// 音频获取源
    private int recBufSize;// 录音最小buffer大小
    private AudioRecord audioRecord;
    private WaveCanvas waveCanvas;
    private String mFileName = "test";//文件名

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWave();
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

    private void initWave() {
        waveSfv = findViewById(R.id.wavesfv);
        switchBtn = findViewById(R.id.switchbtn);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waveCanvas == null || !waveCanvas.isRecording) {
                    status.setText("录音中...");
                    switchBtn.setText("停止录音");
                    waveSfv.setVisibility(View.VISIBLE);
                    waveView.setVisibility(View.INVISIBLE);
                    initAudio();
                    startAudio();

                } else {
                    status.setText("停止录音");
                    switchBtn.setText("开始录音");
                    waveCanvas.Stop();
                    waveCanvas = null;
                    initWaveView();
                }
            }
        });
        status = findViewById(R.id.status);
        waveView = findViewById(R.id.waveview);
        playBtn = findViewById(R.id.play);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(0);//播放 从头开始播放
            }
        });
        scoreBtn = findViewById(R.id.socreaudio);
        scoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float sim = 0;
                try {
                    // new FileInputStream(new File(DATA_DIRECTORY + mFileName + ".wav"))
                    sim = MusicSimilarityUtil.getScoreByCompareFile(getResources().getAssets().open("coku1.wav"), getResources().getAssets().open("coku2.wav"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this,sim+"",Toast.LENGTH_LONG).show();
            }
        });

        if(waveSfv != null) {
            waveSfv.setLine_off(42);
            //解决surfaceView黑色闪动效果
            waveSfv.setZOrderOnTop(true);
            waveSfv.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
        waveView.setLine_offset(42);
        initPermission();
    }

    Handler updateTime = new Handler() {
        public void handleMessage(Message msg) {
            updateDisplay();
            updateTime.sendMessageDelayed(new Message(), 10);
        };
    };

    /**更新upd
     * ateview 中的播放进度*/
    private void updateDisplay() {
        int now = mPlayer.getCurrentPosition();// nullpointer
        int frames = waveView.millisecsToPixels(now);
        waveView.setPlayback(frames);//通过这个更新当前播放的位置
        if (now >= mPlayEndMsec ) {
            waveView.setPlayFinish(1);
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.pause();
                updateTime.removeMessages(UPDATE_WAV);
            }
        }else{
            waveView.setPlayFinish(0);
        }
        waveView.invalidate();//刷新真个视图
    }

    private int mPlayStartMsec;
    private int mPlayEndMsec;
    private final int UPDATE_WAV = 100;
    /**播放音频，@param startPosition 开始播放的时间*/
    private synchronized void onPlay(int startPosition) {
        if (mPlayer == null)
            return;
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
            updateTime.removeMessages(UPDATE_WAV);
        }
        mPlayStartMsec = waveView.pixelsToMillisecs(startPosition);
        mPlayEndMsec = waveView.pixelsToMillisecsTotal();
        mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
                waveView.setPlayback(-1);
                updateDisplay();
                updateTime.removeMessages(UPDATE_WAV);
                Toast.makeText(getApplicationContext(),"播放完成",Toast.LENGTH_LONG).show();
            }
        });
        mPlayer.seekTo(mPlayStartMsec);
        mPlayer.start();
        Message msg = new Message();
        msg.what = UPDATE_WAV;
        updateTime.sendMessage(msg);
    }

    /**
     * 开始录音
     */
    private void startAudio(){
        waveCanvas = new WaveCanvas();
        waveCanvas.baseLine = waveSfv.getHeight() / 2;
        waveCanvas.Start(audioRecord, recBufSize, waveSfv, mFileName, U.DATA_DIRECTORY, new Handler.Callback() {
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
        mLoadSoundFileThread = new Thread() {
            public void run() {
                try {
                    mSoundFile = SoundFile.create(mFile.getAbsolutePath(),null);
                    if (mSoundFile == null) {
                        return;
                    }
                    mPlayer = new SamplePlayer(mSoundFile);
                } catch (final Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (mLoadingKeepGoing) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            finishOpeningSoundFile();
                            waveSfv.setVisibility(View.INVISIBLE);
                            waveView.setVisibility(View.VISIBLE);
                        }
                    };
                    MainActivity.this.runOnUiThread(runnable);
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

    /**
     * 初始化权限
     */
    public void initPermission(){
//        MainActivityPermissionsDispatcher.initAudioWithCheck(this);
    }

    /**
     * 初始化录音  申请录音权限
     */
    @NeedsPermission({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    public void initAudio(){
        recBufSize = AudioRecord.getMinBufferSize(FREQUENCY,
                CHANNELCONGIFIGURATION, AUDIOENCODING);// 录音组件
        audioRecord = new AudioRecord(AUDIO_SOURCE,// 指定音频来源，这里为麦克风
                FREQUENCY, // 16000HZ采样频率
                CHANNELCONGIFIGURATION,// 录制通道
                AUDIO_SOURCE,// 录制编码格式
                recBufSize);// 录制缓冲区大小 //先修改
        U.createDirectory();
    }

    @OnShowRationale({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    void showRationaleForRecord(final PermissionRequest request){
        new AlertDialog.Builder(this)
                .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage("是否开启录音权限")
                .show();
    }
    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    void showRecordDenied(){
        Toast.makeText(MainActivity.this,"拒绝录音权限将无法进行挑战",Toast.LENGTH_LONG).show();
    }

    @OnNeverAskAgain({Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE})
    void onRecordNeverAskAgain() {
        new AlertDialog.Builder(this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 2016/11/10 打开系统设置权限
                        dialog.cancel();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage("您已经禁止了录音权限,是否现在去开启")
                .show();
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
