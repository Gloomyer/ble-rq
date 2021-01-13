package com.gloomyer.blerq.log;

import android.util.Log;

import androidx.annotation.StringRes;

import com.gloomyer.blerq.utils.ContextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description:
 */
public class BleRqLogger {
    /**
     * 日志文件最大存留天数
     */
    private static final int LOG_FILE_MAX_EXIST_DAY = 3;

    private final boolean enableConsole;
    private final boolean enableFile;
    private final File fileDir;
    private final String tag;

    private int logFileMaxExistDay = LOG_FILE_MAX_EXIST_DAY;

    private String deviceAddress;
    private FileOutputStream fos;

    public BleRqLogger(boolean enableConsole, boolean enableFile, File fileDir) {
        this.enableConsole = enableConsole;
        this.enableFile = enableFile;
        this.fileDir = fileDir;
        tag = "BLE_RQ_" + hashCode();
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setLogFileMaxExistDay(int logFileMaxExistDay) {
        this.logFileMaxExistDay = logFileMaxExistDay;
    }

    public void info(String msg, Object... args) {
        String message;
        if (args.length == 0) {
            message = msg;
        } else {
            message = MessageFormat.format(msg, args);
        }
        consoleWrite(message);
        fileWrite(message);
    }

    public void info(@StringRes int msg) {
        String message = ContextUtils.getAppContext().getResources().getString(msg);
        consoleWrite(message);
        fileWrite(message);
    }

    public void info(Exception e) {
        String message = getStackTraceInfo(e);
        consoleWrite(message);
        fileWrite(message);
    }

    private void consoleWrite(String message) {
        if (enableConsole) {
            Log.i(tag, currentTime() + "," + deviceAddress + ", " + message);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void fileWrite(String message) {
        if (enableFile) {
            if (fos == null) {
                synchronized (this) {
                    if (fos == null) {
                        clearOldLogFile();
                        try {
                            if (!fileDir.exists() || !fileDir.isDirectory()) fileDir.mkdirs();
                            fos = new FileOutputStream(new File(fileDir, System.currentTimeMillis() + ".log"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
            try {
                fos.write((currentTime() + "," + deviceAddress + ", " + message + "\r\n").getBytes());
                fos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 清理过期的的日志文件
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void clearOldLogFile() {
        File[] files = fileDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if ((System.currentTimeMillis() - file.lastModified()) > (logFileMaxExistDay * 1000 * 60 * 60 * 24)) {
                    file.delete();
                }
            }
        }
    }

    private String currentTime() {
        Calendar calendar = Calendar.getInstance();
        StringBuilder time = new StringBuilder();
        {
            time.append(calendar.get(Calendar.YEAR)).append("/")
                    .append(calendar.get(Calendar.MONTH) + 1).append("/")
                    .append(calendar.get(Calendar.DAY_OF_MONTH))
                    .append(" ")
                    .append(calendar.get(Calendar.HOUR_OF_DAY)).append(":")
                    .append(calendar.get(Calendar.MINUTE)).append(":")
                    .append(calendar.get(Calendar.SECOND)).append(".")
                    .append(calendar.get(Calendar.MILLISECOND));
        }
        return time.toString();
    }

    public void close() {
        if (fos != null) {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fos = null;
        }
    }

    /**
     * 获取错误的信息
     *
     * @param throwable throwable
     * @return throwable
     */
    private String getStackTraceInfo(final Throwable throwable) {
        PrintWriter pw = null;
        Writer writer = new StringWriter();
        try {
            pw = new PrintWriter(writer);
            throwable.printStackTrace(pw);
        } catch (Exception e) {
            return "";
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        return writer.toString();
    }

}
