@file:Suppress("MemberVisibilityCanBePrivate")

package com.example.banneradmediation.firebase

import android.util.Log
import com.example.banneradmediation.BuildConfig.DEBUG
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.example.banneradmediation.secrets.AdConfig
import com.example.banneradmediation.tools.MyLogging


object RemoteConfig {

    private const val TAG = "RemoteConfig"
    private val myLogger = MyLogging()

    // Configurable Keys which are used from Firebase
    const val AD_CONFIG = "AD_CONFIG"
    const val ADMOB_ADAPTIVE_BANNER = "ADMOB_ADAPTIVE_BANNER"
    const val AD_RELOAD_TIME_MS = "AD_RELOAD_TIME_MS"

    // Configurables hashMap
    private val defaultConfValues = hashMapOf<String, Any>(
        AD_CONFIG to AdConfig.adMob,
        ADMOB_ADAPTIVE_BANNER to true,
        AD_RELOAD_TIME_MS to AdConfig.reloadTime90
    )

    /**CALL THIS AS EARLY AS POSSIBLE AT onCreate()*/
    fun setFirebaseAdConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (DEBUG) {
                0 // Quick access for debug testing
            } else { 43200 } // Update frequency for normal use. Dont use more less than 60*60*12=12h to avoid throttling.
        }
        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(defaultConfValues)
            fetchAndActivate().addOnCompleteListener {
                if (it.isSuccessful) {
                    myLogger.logThis(TAG, "setFirebaseAdConfig Complete", Log.DEBUG)
                } else { myLogger.logThis(TAG, "setFirebaseAdConfig Not Completed", Log.DEBUG) }
            }
        }
    }

    /**REFRESH AT onStart()*/
    fun activateNewValues() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.activate().addOnCompleteListener {
            if (it.isSuccessful) {
                myLogger.logThis(TAG, "activateNewValues Complete", Log.DEBUG)
            } else { myLogger.logThis(TAG, "activateNewValues Not Completed", Log.DEBUG) }
        }
    }

    /**Get instance of remoteConfig*/
    fun getRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance()
    }

}