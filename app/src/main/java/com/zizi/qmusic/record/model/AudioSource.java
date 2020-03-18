package com.zizi.qmusic.record.model;

import android.media.MediaRecorder;

/**
 * <pre>
 *     author : qiuyayong
 *     e-mail : qiuyayong@lizhi.fm
 *     time   : 2020/03/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public enum AudioSource {
    MIC,
    CAMCORDER;

    public int getSource(){
        switch (this){
            case CAMCORDER:
                return MediaRecorder.AudioSource.CAMCORDER;
            default:
                return MediaRecorder.AudioSource.MIC;
        }
    }
}
