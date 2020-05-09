package com.zizi.playlib.nativeUtils;

/**
 * 文件直接读写short数组
 */
public class FileUtilJniProxy {
    static {
        System.loadLibrary("PlayNative");
    }

    public native int nativeOpenFile(String filename);

    public native void nativeClose(int fd);

    public native boolean nativeWriteFile(int fd, int startWrite, short[] array, int offset, int length);

    public native boolean nativeReadFile(int fd, int startRead, short[] array, int offset, int length);

}
