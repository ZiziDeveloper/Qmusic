package com.zizi.qmusic.record.model;

import android.media.AudioFormat;

/**
 * <pre>
 *     author : qiuyayong
 *     e-mail : qiuyayong@lizhi.fm
 *     time   : 2020/03/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public enum AudioChannel {
    STEREO,
    MONO;

    public int getChannel(){
        switch (this){
            case MONO:
                return AudioFormat.CHANNEL_IN_MONO;
            default:
                return AudioFormat.CHANNEL_IN_STEREO;
        }
    }
}
