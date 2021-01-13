package com.gloomyer.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gloomyer.blerq.utils.ContextUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(javaClass.simpleName, "ContextUtils.getAppContext()${ContextUtils.getAppContext()}")
    }
}