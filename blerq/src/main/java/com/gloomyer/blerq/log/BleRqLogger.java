package com.gloomyer.blerq.log;

import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description:
 */
public class BleRqLogger {
    private boolean enableConsole;
    private boolean enableFile;
    private File fileDir;
    private String tag;

    public void setEnableConsole(boolean enableConsole) {
        this.enableConsole = enableConsole;
    }

    public void setEnableFile(boolean enableFile) {
        this.enableFile = enableFile;
    }

    public void setFileDir(File fileDir) {
        this.fileDir = fileDir;
    }

    public BleRqLogger() {
        tag = "BLE_RQ_" + hashCode();
    }

    public void info(String msg, Object... args) {
        String message;
        if (args.length == 0) {
            message = msg;
        } else {
            message = MessageFormat.format(msg, args);
        }
        if (enableConsole) {
            Log.i(tag, message);
        }
    }

    public void info(Exception e) {
        String message = getStackTraceInfo(e);
        if (enableConsole) {
            Log.i(tag, message);
        }
    }


    public void close() {

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
