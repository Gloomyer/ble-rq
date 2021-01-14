package com.gloomyer.blerq;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.gloomyer.blerq.callback.BleRqScanCallback;
import com.gloomyer.blerq.code.BleRqError;
import com.gloomyer.blerq.exception.BleRqException;
import com.gloomyer.blerq.log.BleRqLogger;
import com.gloomyer.blerq.utils.ContextUtils;
import com.gloomyer.blerq.utils.PermissionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Time: 1/13/21
 * Author: Gloomy
 * Description: Ble rq client 必须通过builder 对象构建
 */
public class BleRqClient implements LifecycleObserver {

    private final long scanTimeout; //扫描超时时间
    private final long connTimeout; //连接超时时间
    private final ScanSettings scanSettings;
    private final List<ScanFilter> scanFilters; //扫描过滤
    private final int writeFailedRepeatCount; //写失败的时候重试次数
    private final Handler mHandler;
    private final BleRqLogger logger;
    private BleRqScanCallback scanCallback;
    private Context context;
    private BluetoothAdapter bmAdapter;
    private FragmentManager fm; //fragment manager 用于申请权限
    private BluetoothLeScanner bluetoothLeScanner;
    private InnerScanCallback innerScanCallback;

    private UUID serviceUuid;
    private UUID writeChannelUuid;
    private UUID readChannelUuid;
    private UUID notifyChannelUuid;

    private ProxyDevice device;
    private String deviceAddress;
    private String deviceName;


    BluetoothGattCharacteristic writeCharacteristic;//用于写数据的writeCharacteristic
    boolean isFirstSuccess; //标示第一次成功连接了 可以通过开始获取接口代理对象了

    private BleRqClient(long scanTimeout, long connTimeout, BleRqLogger logger, int writeFailedRepeatCount,
                        UUID serviceUuid, UUID writeChannelUuid,
                        UUID readChannelUuid, UUID notifyChannelUuid,
                        ScanSettings scanSettings, List<ScanFilter> scanFilters, BleRqScanCallback scanCallback) {
        this.scanTimeout = scanTimeout;
        this.connTimeout = connTimeout;
        this.logger = logger;
        this.writeFailedRepeatCount = writeFailedRepeatCount;
        this.scanSettings = scanSettings;
        this.scanFilters = scanFilters;
        this.scanCallback = scanCallback;
        mHandler = new Handler(Looper.getMainLooper());
    }

    private boolean isStart; //是否启动标示

    /**
     * 开始连接
     */
    public void start() {
        synchronized (this) {
            if (isStart) {
                throw new BleRqException(R.string.blerq_just_call_once_start);
            }
            isStart = true;
        }
        //先检测系统ble环境
        logger.info("start connection");
        if (context == null) {
            throw new BleRqException(R.string.blerq_not_found_context);
        }
        //是否支持ble蓝牙
        boolean standByBle = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        logger.info("standByBle: " + standByBle);
        if (!standByBle) {
            //不支持ble蓝牙
            scanCallback.onError(BleRqError.DEVICE_NOT_SUPPORT);
            return;
        }

        //获取ble操作管理对象
        BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        logger.info("found BluetoothManager:" + bm);
        if (bm == null) {
            scanCallback.onError(BleRqError.DEVICE_NOT_SUPPORT);
            return;
        }

        this.bmAdapter = bm.getAdapter();
        logger.info("found Bluetooth Adapter:" + bmAdapter);
        if (bmAdapter == null) {
            scanCallback.onError(BleRqError.DEVICE_NOT_SUPPORT);
            return;
        }
        checkPermission();
    }

    /**
     * 进行第2步 检测ble所需权限
     */
    private void checkPermission() {
        int p1 = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN);
        int p2 = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH);
        int p3 = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        int p4 = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        boolean hasPermission = p1 == PackageManager.PERMISSION_GRANTED
                && p2 == PackageManager.PERMISSION_GRANTED
                && p3 == PackageManager.PERMISSION_GRANTED
                && p4 == PackageManager.PERMISSION_GRANTED;

        logger.info("checkPermission: " +
                        "BLUETOOTH_ADMIN: {0}, BLUETOOTH: {1}, " +
                        "ACCESS_COARSE_LOCATION: {2}, ACCESS_FINE_LOCATION: {3}, " +
                        "hasPermission: {4}",
                p1, p2, p3, p4, hasPermission);

        if (fm == null && !hasPermission) {
            //没有权限 并且没有fm无法主动申请权限 报错给上层业务
            scanCallback.onError(BleRqError.DEVICE_NO_PERMISSION);
            return;
        }
        if (hasPermission) {
            openBluetooth();
        } else {
            PermissionUtils.requestPermission(fm, isGet -> {
                        logger.info("request permission isGet: " + isGet);
                        if (isGet) {
                            openBluetooth();
                        } else {
                            //权限申请失败了
                            scanCallback.onError(BleRqError.DEVICE_NO_PERMISSION);
                        }
                    }, Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }

    }

    /**
     * 第3步 打开蓝牙设备
     */
    private void openBluetooth() {
        logger.info("bmAdapter.isEnabled: {0}", bmAdapter.isEnabled());
        if (!bmAdapter.isEnabled() && fm == null) {
            //蓝牙处于关闭状态 并且fm为空无法尝试打开，给上层业务报错
            scanCallback.onError(BleRqError.DEVICE_BLUETOOTH_NOT_OPEN);
            return;
        }

        if (bmAdapter.isEnabled()) {
            scanDevice();
        } else {
            PermissionUtils.requestOpenBluetooth(fm, isOpen -> {
                if (isOpen) {
                    scanDevice();
                } else {
                    scanCallback.onError(BleRqError.DEVICE_BLUETOOTH_NOT_OPEN);
                }
            });
        }

    }

    /**
     * 第4步 开始扫描设备
     */
    private void scanDevice() {
        logger.info("start scan device");
        bluetoothLeScanner = bmAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            scanCallback.onError(BleRqError.DEVICE_BLUETOOTH_NOT_OPEN);
            return;
        }
        innerScanCallback = new InnerScanCallback();
        bluetoothLeScanner.startScan(scanFilters, scanSettings, innerScanCallback);
        mHandler.postDelayed(innerScanCallback.cancelCallback, scanTimeout);
    }

    /**
     * 第5步 开始连接设备
     */
    private void connectDevice() {
        logger.setDeviceAddress(deviceAddress);
        logger.info("connect device..");
        this.device.connect(this, context, connTimeout, scanCallback,
                serviceUuid, writeChannelUuid, readChannelUuid, notifyChannelUuid);
    }

    private final Map<Class<?>, Object> apiCache = new HashMap<>();

    public void clearApiCache() {
        apiCache.clear();
    }

    /**
     * 获取接口代理对象
     *
     * @param clazz 要获取对象的接口
     * @param <T>   对象类型
     * @return 实例化的对象
     */
    public <T> T getApi(Class<T> clazz) {
        if (!isFirstSuccess) {
            throw new BleRqException(R.string.blerq_unsuccess_not_get_api);
        }
        Object api = apiCache.get(clazz);
        if (api == null) {
            synchronized (this) {
                //noinspection ConstantConditions
                if (api == null) {
                    api = ApiProxyHelper.getApi(clazz);
                    apiCache.put(clazz, api);
                }
            }
        }
        //noinspection unchecked
        return (T) api;//ApiProxyHelper.getApi(clazz);
    }


    @SuppressWarnings({"unused", "RedundantSuppression"})
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void onDestroy(@NonNull LifecycleOwner owner) {
        onDestroy();
    }

    public void onDestroy() {
        logger.info("开始释放资源");
        logger.close();
        if (bluetoothLeScanner != null && innerScanCallback != null)
            bluetoothLeScanner.stopScan(innerScanCallback);
        if (innerScanCallback != null && mHandler != null)
            mHandler.removeCallbacks(innerScanCallback.cancelCallback);

        innerScanCallback = null;
        bluetoothLeScanner = null;
        scanCallback = null;
        bmAdapter = null;
        if (device != null) device.close();
        device = null;
    }

    private class InnerScanCallback extends ScanCallback {

        private final Runnable cancelCallback = new Runnable() {
            @Override
            public void run() {
                if (bluetoothLeScanner != null)
                    bluetoothLeScanner.stopScan(InnerScanCallback.this);
                if (scanCallback != null) {
                    logger.info("device scan timeout...");
                    scanCallback.onError(BleRqError.DEVICE_SCAN_TIMEOUT);
                }
            }
        };

        private int found = 0;

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (scanCallback != null) {
                boolean isSuccess;
                synchronized (this) {
                    if (found == 0) {
                        logger.info("onScanResult: address: {0}, name: {1}",
                                result.getDevice().getAddress(),
                                result.getDevice().getName());
                        isSuccess = scanCallback.isNeedConnDevice(callbackType, result);
                        if (isSuccess) found++;
                    } else {
                        isSuccess = false;
                    }
                }
                if (isSuccess && found == 1) {
                    if (bluetoothLeScanner != null) bluetoothLeScanner.stopScan(this);
                    if (innerScanCallback != null)
                        mHandler.removeCallbacks(innerScanCallback.cancelCallback);
                    innerScanCallback = null;
                    bluetoothLeScanner = null;

                    device = new ProxyDevice(result.getDevice(), logger);
                    deviceAddress = device.getAddress();
                    deviceName = device.getName();
                    logger.info("成功扫描到设备: name: {0}, address: {1}", deviceName, deviceAddress);
                    connectDevice();
                }
            }

        }
    }


    /**
     * 获取ClientBuilder 对象
     *
     * @param owner 宿主 如果设置了宿主 client会自动感知生命周期 来清理对象 和自动获取权限 和自动尝试打开蓝牙设备
     * @return BleRqClientBuilder
     */
    public static BleRqClientBuilder newBuilder(LifecycleOwner owner) {
        return new BleRqClientBuilder(owner);
    }

    /**
     * 获取ClientBuilder 对象
     * 使用方法需要自行解决权限 和蓝牙开关问题 对象清理也需要自己实现
     *
     * @return BleRqClientBuilder
     */
    public static BleRqClientBuilder newBuilder() {
        return new BleRqClientBuilder();
    }

    public static class BleRqClientBuilder {
        private final LifecycleOwner owner;
        private long scanTimeout = 15000; //扫描超时时间
        private long connTimeout = 5000; //扫描超时时间
        private boolean enableLog = true; //是否启用log输出
        private boolean enableLogFile = true; //是否启用输出到文件
        private Integer logFileMaxExistDay; //日志文件最多存留天数
        private File logFileDir;
        private int writeFailedRepeatCount = 3; //写失败之后的重试次数
        private ScanSettings scanSettings;
        private List<ScanFilter> scanFilters;
        private BleRqScanCallback scanCallback;

        private UUID serviceUuid;
        private UUID writeChannelUuid;
        private UUID readChannelUuid;
        private UUID notifyChannelUuid;

        /**
         * 使用此构造方法
         * 需要自行解决权限和蓝牙开关问题 对象清理也需要自己调用
         */
        public BleRqClientBuilder() {
            this(null);
        }

        /**
         * 获取ClientBuilder 对象
         *
         * @param owner 宿主 如果设置了宿主 client会自动感知生命周期 来清理对象 和自动获取权限 和自动尝试打开蓝牙设备
         */
        public static BleRqClientBuilder newBuilder(LifecycleOwner owner) {
            return new BleRqClientBuilder(owner);
        }

        public BleRqClientBuilder(LifecycleOwner owner) {
            this.owner = owner;
            ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            }
            scanSettings = scanSettingsBuilder.build();
        }

        /**
         * 设置扫描超时时间
         *
         * @param scanTimeout 超时时间
         * @return this
         */
        public BleRqClientBuilder setScanTimeout(long scanTimeout) {
            this.scanTimeout = scanTimeout;
            return this;
        }

        /**
         * 设置连接超时时间
         *
         * @param connTimeout connTimeout
         * @return this
         */
        public BleRqClientBuilder setConnTimeout(long connTimeout) {
            this.connTimeout = connTimeout;
            return this;
        }

        /**
         * 设置是否启用日志输出
         *
         * @param enableLog 是否启用日志输出
         * @return this
         */
        public BleRqClientBuilder setEnableLog(boolean enableLog) {
            this.enableLog = enableLog;
            return this;
        }

        /**
         * 是否启用输出日志到文件
         *
         * @param enableLogFile 是否启用输出日志到文件
         * @return this
         */
        public BleRqClientBuilder setEnableLogFile(boolean enableLogFile) {
            this.enableLogFile = enableLogFile;
            return this;
        }

        /**
         * 设置日志文件存储路径
         *
         * @param logFileDir 日志文件存储路径
         * @return this
         */
        public BleRqClientBuilder setLogFileDir(File logFileDir) {
            this.logFileDir = logFileDir;
            return this;
        }

        /**
         * 设置扫描设置
         *
         * @param scanSettings 扫描设置
         * @return this
         */
        public BleRqClientBuilder setScanSettings(ScanSettings scanSettings) {
            this.scanSettings = scanSettings;
            return this;
        }

        /**
         * 设置扫描过滤器
         *
         * @param scanFilters 扫描过滤器
         * @return this
         */
        public BleRqClientBuilder setScanFilters(List<ScanFilter> scanFilters) {
            this.scanFilters = scanFilters;
            return this;
        }

        /**
         * 设置扫描回调 用于确定要连接哪个设备
         *
         * @param scanCallback 设置扫描回调
         * @return this
         */
        public BleRqClientBuilder setScanCallback(BleRqScanCallback scanCallback) {
            this.scanCallback = scanCallback;
            return this;
        }

        /**
         * 设置写失败重试次数
         *
         * @param writeFailedRepeatCount 写失败重试次数
         * @return this
         */
        public BleRqClientBuilder setWriteFailedRepeatCount(int writeFailedRepeatCount) {
            this.writeFailedRepeatCount = writeFailedRepeatCount;
            return this;
        }

        /**
         * 设置服务 uuid
         *
         * @param serviceUuid serviceUuid
         * @return this
         */
        public BleRqClientBuilder setServiceUuid(UUID serviceUuid) {
            this.serviceUuid = serviceUuid;
            return this;
        }

        /**
         * 设置服务 uuid
         *
         * @param serviceUuid serviceUuid
         * @return this
         */
        public BleRqClientBuilder setServiceUuid(String serviceUuid) {
            this.serviceUuid = UUID.fromString(serviceUuid);
            return this;
        }

        /**
         * 设置 写通道 uuid
         *
         * @param writeChannelUuid 写通道 uuid
         * @return this
         */
        public BleRqClientBuilder setWriteChannelUuid(UUID writeChannelUuid) {
            this.writeChannelUuid = writeChannelUuid;
            return this;
        }

        /**
         * 设置 写通道 uuid
         *
         * @param writeChannelUuid 写通道 uuid
         * @return this
         */
        public BleRqClientBuilder setWriteChannelUuid(String writeChannelUuid) {
            this.writeChannelUuid = UUID.fromString(writeChannelUuid);
            return this;
        }

        /**
         * 设置 读通道 uuid
         *
         * @param readChannelUuid 读通道 uuid
         * @return this
         */
        public BleRqClientBuilder setReadChannelUuid(UUID readChannelUuid) {
            this.readChannelUuid = readChannelUuid;
            return this;
        }

        /**
         * 设置 读通道 uuid
         *
         * @param readChannelUuid 读通道 uuid
         * @return this
         */
        public BleRqClientBuilder setReadChannelUuid(String readChannelUuid) {
            this.readChannelUuid = UUID.fromString(readChannelUuid);
            return this;
        }

        /**
         * 设置 通知通道 uuid
         *
         * @param notifyChannelUuid 通知通道 uuid
         * @return this
         */
        public BleRqClientBuilder setNotifyChannelUuid(UUID notifyChannelUuid) {
            this.notifyChannelUuid = notifyChannelUuid;
            return this;
        }

        /**
         * 设置 通知通道 uuid
         *
         * @param notifyChannelUuid 通知通道 uuid
         * @return this
         */
        public BleRqClientBuilder setNotifyChannelUuid(String notifyChannelUuid) {
            this.notifyChannelUuid = UUID.fromString(notifyChannelUuid);
            return this;
        }

        /**
         * 设置日志文件最多存留天数
         *
         * @param logFileMaxExistDay 日志文件最多存留天数
         * @return this
         */
        public BleRqClientBuilder setLogFileMaxExistDay(Integer logFileMaxExistDay) {
            this.logFileMaxExistDay = logFileMaxExistDay;
            return this;
        }

        /**
         * 构建 ble rq client 对象
         *
         * @return ble rq client
         */
        public BleRqClient build() {
            if (scanCallback == null) {
                throw new BleRqException(R.string.blerq_must_set_scan_callback);
            }
            if (logFileDir == null) {
                logFileDir = ContextUtils.getAppContext().getExternalFilesDir("ble_rq_logs");
            }

            //初始化日志组件
            BleRqLogger logger = new BleRqLogger(enableLog, enableLogFile, logFileDir);
            if (logFileMaxExistDay != null) logger.setLogFileMaxExistDay(logFileMaxExistDay);


            if (serviceUuid == null || writeChannelUuid == null || readChannelUuid == null || notifyChannelUuid == null) {
                logger.info(R.string.blerq_must_set_all_channel);
            }
            BleRqClient manager = new BleRqClient(scanTimeout, connTimeout,
                    logger, writeFailedRepeatCount,
                    serviceUuid, writeChannelUuid, readChannelUuid, notifyChannelUuid,
                    scanSettings, scanFilters, scanCallback);

            manager.context = ContextUtils.getAppContext();
            if (owner != null) {
                owner.getLifecycle().addObserver(manager);
                if (owner instanceof Activity) {
                    manager.context = (Context) owner;
                    if (owner instanceof FragmentActivity) {
                        manager.fm = ((FragmentActivity) owner).getSupportFragmentManager();
                    }
                } else if (owner instanceof Fragment) {
                    manager.context = ((Fragment) owner).getContext();
                    manager.fm = ((Fragment) owner).getFragmentManager();
                }
            }

            return manager;
        }
    }
}
