package com.zizi.qmusic;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * <pre>
 *     author : qiuyayong
 *     e-mail : qiuyayong@lizhi.fm
 *     time   : 2020/03/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Util {

    public static void requestPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
        }
    }
}
