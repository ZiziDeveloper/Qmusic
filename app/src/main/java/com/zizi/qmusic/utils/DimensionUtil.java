package com.zizi.qmusic.utils;

import android.app.Application;
import android.content.Context;

import com.zizi.qmusic.ZiziApplication;

/**
 * 尺寸相关的工具类
 */
public class DimensionUtil {

    private static int mDisplayHeight;
    private static int mDisplayWidth;

    /**
     * dip换算成像素数量
     *
     * @param context
     * @param dip
     * @return
     */
    public static int dipToPx(Context context, float dip) {
        if (context != null) {
            return roundUp(dip * context.getResources().getDisplayMetrics().density);
        }
        return 0;
    }

    public static int dipToPx(float dp) {
        return roundUp(dp * ZiziApplication.getContext().getResources().getDisplayMetrics().density);
    }

    public static int roundUp(float f) {
        return (int) (0.5f + f);
    }

    public static int getDisplayWidth(Context context) {
        if (mDisplayWidth <= 0) {
            mDisplayWidth = context.getResources().getDisplayMetrics().widthPixels;
        }
        return mDisplayWidth;
    }
}
