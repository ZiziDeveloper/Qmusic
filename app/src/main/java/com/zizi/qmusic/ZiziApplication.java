package com.zizi.qmusic;

import android.app.Application;
import android.content.Context;

import com.zizi.playlib.record.utils.ApplicationUtil;

public class ZiziApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        ApplicationUtil.init(context);
    }

    public static Context getContext() {
        return context;
    }

}
