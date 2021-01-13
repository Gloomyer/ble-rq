package com.gloomyer.blerq.log;

import java.io.File;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description:
 */
public class BleRqLogger {
    private boolean enableConsole;
    private boolean enableFile;
    private File fileDir;

    public void setEnableConsole(boolean enableConsole) {
        this.enableConsole = enableConsole;
    }

    public void setEnableFile(boolean enableFile) {
        this.enableFile = enableFile;
    }

    public void setFileDir(File fileDir) {
        this.fileDir = fileDir;
    }

    public void info() {

    }
}
