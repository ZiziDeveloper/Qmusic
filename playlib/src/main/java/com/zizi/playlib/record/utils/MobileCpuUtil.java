package com.zizi.playlib.record.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.regex.Pattern;

public class MobileCpuUtil {
    public static String getMaxCpuFreq() {// 获取cpu最大频率
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = { "/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream is = process.getInputStream();
            byte[] te = new byte[24];
            while (is.read(te) != -1) {
                result += new String(te);
            }
            float core = Float.valueOf(result);
            core = core / 1000;
            result = (int) core + "Mhz";
            is.close();
        } catch (Exception e) {
            result = "512Mhz";//若检测不出cpu频率，默认为512Mhz
        }
        return result.trim();

    }

    public static String getMinCpuFreq() {// 获取cpu最小频率
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = { "/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq" };
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream is = process.getInputStream();
            byte[] te = new byte[24];
            while (is.read(te) != -1) {
                result += new String(te);
            }
            float core = Float.valueOf(result);
            System.out.println("------>coure" + core);
            core = core / 1000;
            result = (int) core + " Mhz";
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    public static int getNumCores() {// 获取cpu核心数
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }

        }
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static String getNumCoure()// 转换核心数
    {
        String result = "";
        if (getNumCores() == 1) {
            result = "单核";
        } else if (getNumCores() == 2) {
            result = "双核";
        } else if (getNumCores() == 4) {
            result = "四核";
        } else {
            result = "你手机为劣质手机,无法检测!";
        }
        return result;
    }
}
