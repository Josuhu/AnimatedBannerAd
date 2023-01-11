package com.example.banneradmediation.tools

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import org.koin.java.KoinJavaComponent.inject


object NetworkConnection {

    private const val TAG = "NetworkConnection"
    private val myLogger by inject<MyLogging>(MyLogging::class.java)

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                myLogger.logThis(TAG, "NetworkCapabilities.TRANSPORT_CELLULAR", Log.DEBUG)
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                myLogger.logThis(TAG, "NetworkCapabilities.TRANSPORT_WIFI", Log.DEBUG)
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                myLogger.logThis(TAG, "NetworkCapabilities.TRANSPORT_ETHERNET", Log.DEBUG)
                return true
            }
        }
        return false
    }
}