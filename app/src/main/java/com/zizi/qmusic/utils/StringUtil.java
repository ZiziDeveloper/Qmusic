package com.zizi.qmusic.utils;

public class StringUtil {
    /**
     * MM:SS 字串如 18:44 = 18分44秒
     *
     * @param time 毫秒
     * @return
     */
    public static String getMMSSString(long time) {
        return String.format("%02d:%02d", time / 60, time % 60);
    }

    public static String getHHMMSSString(long time) {
        if (time >= 3600) {
            return String.format("%02d:%02d:%02d", time / 3600, (time % 3600) / 60, time % 60);
        }
        return getMMSSString(time);
    }
}
