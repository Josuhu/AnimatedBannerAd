package com.example.banneradmediation.tools

import android.util.Log
import com.example.banneradmediation.BuildConfig

class MyLogging {

    /**Logging only in a debug mode, else just error logging*/
    fun logThis(tag: String, message: String, logType: Int) {
        if (BuildConfig.DEBUG) {
            when (logType) {
                Log.DEBUG -> { Log.d(tag, message) }
                Log.ERROR -> { Log.e(tag, message) }
                Log.INFO -> { Log.i(tag, message) }
                else -> { Log.d(tag, message) }
            }
        } else if (logType == Log.ERROR) {
            Log.e(tag, message)
        }
    }

}