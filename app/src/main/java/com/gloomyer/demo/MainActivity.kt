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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(javaClass.simpleName, "ContextUtils.getAppContext()${ContextUtils.getAppContext()}")
        BleRqClient.newBuilder(this)
            .setEnableLog(true)
            .setEnableLogFile(true)
            .setScanTimeout(15000)
            .setScanCallback(object : BleRqScanCallback {
                override fun isNeedConnDevice(device: ScanResult): Boolean {
                    return true;
                }

                override fun onError(error: BleRqError) {

                }

                override fun onSuccess() {
                }

            }).build().start()
    }
}