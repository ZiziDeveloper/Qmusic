package com.example.administrator.qmusic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.playlib.PlayJniProxy;

public class MainActivity extends AppCompatActivity {
    // Used to load the 'native-lib' library on application startup.


    PlayJniProxy mPlayJniProxy;
    private String playUrl = "http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayJniProxy = new PlayJniProxy();
        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
    }

    public void onPrepare(View view) {
        mPlayJniProxy.prepare(playUrl, 0, 0, 0);
    }

    public void onStart(View view) {
        mPlayJniProxy.start();
    }
}
