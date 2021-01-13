package com.gloomyer.blerq.log;

import android.util.Log;

import java.io.File;
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
        tag = String.valueOf(hashCode());
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
}
