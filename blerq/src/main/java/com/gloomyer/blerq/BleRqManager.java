package com.gloomyer.blerq;

import android.app.Activity;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.gloomyer.blerq.callback.BleRqScanCallback;
import com.gloomyer.blerq.exception.BleRqException;
import com.gloomyer.blerq.log.BleRqLogger;

import java.io.File;
import java.util.List;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: Ble rq manager 需要通过builder 对象构建
 */
public class BleRqManager implements LifecycleObserver {

    private long scanTimeout = 15000; //扫描超时时间
    private boolean enableLog; //是否启用log输出
    private boolean enableLogFile; //是否启用输出到文件
    private ScanSettings scanSettings;
    private List<ScanFilter> scanFilters;
    private BleRqLogger logger;
    private BleRqScanCallback scanCallback;
    private Context uiContext;

    public BleRqManager(long scanTimeout, boolean enableLog, boolean enableLogFile, BleRqLogger logger,
                        ScanSettings scanSettings, List<ScanFilter> scanFilters, BleRqScanCallback scanCallback) {
        this.scanTimeout = scanTimeout;
        this.enableLog = enableLog;
        this.enableLogFile = enableLogFile;
        this.logger = logger;
        this.scanSettings = scanSettings;
        this.scanFilters = scanFilters;
        this.scanCallback = scanCallback;
    }

    public static class BleRqManagerBuilder {
        private final LifecycleOwner owner;
        private long scanTimeout = 15000; //扫描超时时间
        private boolean enableLog = true; //是否启用log输出
        private boolean enableLogFile = true; //是否启用输出到文件
        private ScanSettings scanSettings;
        private List<ScanFilter> scanFilters;
        private BleRqScanCallback scanCallback;
        private BleRqLogger logger;

        public static BleRqManagerBuilder newBuilder(LifecycleOwner owner) {
            return new BleRqManagerBuilder(owner);
        }

        public BleRqManagerBuilder(LifecycleOwner owner) {
            ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            }
            scanSettings = scanSettingsBuilder.build();
            this.owner = owner;
            logger = new BleRqLogger();
            logger.setEnableConsole(enableLog);
            logger.setEnableFile(enableLogFile);

        }

        /**
         * 设置扫描超时时间
         *
         * @param scanTimeout 超时时间
         * @return this
         */
        public BleRqManagerBuilder setScanTimeout(long scanTimeout) {
            this.scanTimeout = scanTimeout;
            return this;
        }

        /**
         * 设置是否启用日志输出
         *
         * @param enableLog 是否启用日志输出
         * @return this
         */
        public BleRqManagerBuilder setEnableLog(boolean enableLog) {
            this.enableLog = enableLog;
            logger.setEnableConsole(enableLog);
            return this;
        }

        /**
         * 是否启用输出日志到文件
         *
         * @param enableLogFile 是否启用输出日志到文件
         * @return this
         */
        public BleRqManagerBuilder setEnableLogFile(boolean enableLogFile) {
            this.enableLogFile = enableLogFile;
            logger.setEnableFile(enableLogFile);
            return this;
        }

        /**
         * 设置日志文件存储路径
         *
         * @param logFileDir 日志文件存储路径
         * @return this
         */
        public BleRqManagerBuilder setLogFileDir(File logFileDir) {
            logger.setFileDir(logFileDir);
            return this;
        }

        /**
         * 设置扫描设置
         *
         * @param scanSettings 扫描设置
         * @return this
         */
        public BleRqManagerBuilder setScanSettings(ScanSettings scanSettings) {
            this.scanSettings = scanSettings;
            return this;
        }

        /**
         * 设置扫描过滤器
         *
         * @param scanFilters 扫描过滤器
         * @return this
         */
        public BleRqManagerBuilder setScanFilters(List<ScanFilter> scanFilters) {
            this.scanFilters = scanFilters;
            return this;
        }

        /**
         * 设置扫描回调 用于确定要连接哪个设备
         *
         * @param scanCallback 设置扫描回调
         * @return this
         */
        public BleRqManagerBuilder setScanCallback(BleRqScanCallback scanCallback) {
            this.scanCallback = scanCallback;
            return this;
        }

        public BleRqManager build() {
            if (scanCallback == null) {
                throw new BleRqException(R.string.blerq_must_set_scan_callback);
            }
            BleRqManager manager = new BleRqManager(scanTimeout, enableLog, enableLogFile, logger,
                    scanSettings, scanFilters, scanCallback);
            owner.getLifecycle().addObserver(manager);
            if (owner instanceof Activity) {
                manager.uiContext = (Context) owner;
            } else if (owner instanceof Fragment) {
                manager.uiContext = ((Fragment) owner).getContext();
            } else {
                throw new BleRqException(R.string.blerq_not_normal_owner);
            }
            return manager;
        }
    }
}
