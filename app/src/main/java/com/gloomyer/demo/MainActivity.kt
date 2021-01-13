package com.gloomyer.demo

import android.bluetooth.le.ScanResult
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gloomyer.blerq.BleRqClient
import com.gloomyer.blerq.callback.BleRqScanCallback
import com.gloomyer.blerq.code.BleRqError
import com.gloomyer.blerq.utils.ContextUtils

class MainActivity : AppCompatActivity() {
    var index = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(javaClass.simpleName, "ContextUtils.getAppContext()${ContextUtils.getAppContext()}")
        BleRqClient.newBuilder(this)
            .setEnableLog(true)
            .setEnableLogFile(true)
            .setLogFileMaxExistDay(5)
            .setScanTimeout(10000)
            .setScanCallback(object : BleRqScanCallback {

                override fun isNeedConnDevice(callbackType: Int, device: ScanResult): Boolean {
                    return "10:84:DE:E6:06:41".equals(device.device.address, true)
                }

                override fun onError(error: BleRqError) {
                }

                override fun onSuccess(client: BleRqClient) {

                }

            }).build().start()
    }
}