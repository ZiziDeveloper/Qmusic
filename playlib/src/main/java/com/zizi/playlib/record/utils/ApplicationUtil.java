package com.zizi.playlib.record.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ApplicationUtil {

    private static String TAG = "ApplicationUtil";

    private static Context context;

    private static Application mApplication;

    private static String packageName = "com.zizi.qmusic";
    /**
     * 初始化方法.
     *
     * @param con Context
     */
    public static void init(Context con) {
        context = con;
        packageName = con.getPackageName();
    }

    public static void setApplication(Application application){
        mApplication = application;
    }

    public static Context getContext() {
        return context;
    }

    public static Application getApplication(){
        return mApplication;
    }

    public static String getPackageName() {
        return packageName;
    }

}
