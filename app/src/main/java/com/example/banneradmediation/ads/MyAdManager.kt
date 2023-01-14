package com.example.banneradmediation.ads

import android.content.Context
import android.util.Log
import com.example.banneradmediation.BuildConfig
import com.example.banneradmediation.tools.MyLogging
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds

class MyAdManager {

    @Suppress("PrivatePropertyName")
    private val TAG = "MyAdManager"
    private val myLogger = MyLogging()

    // Google Ads main initialization
    fun initialiseAdMob(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                myLogger.logThis(TAG, String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status?.description, status?.latency
                    ),
                    Log.DEBUG
                )
            }
        }
    }

    // Meta Ads main initialization
    fun initialiseMeta(context: Context) {
        MetaManager().initMeta(context)
    }

    // Unity Ads main initialization
//    fun initialiseUnity(context: Context) {
//        UnityManager().initUnity(context)
//    }


    /**MONITOR META INITIALIZATION*/
    inner class MetaManager: AudienceNetworkAds.InitListener {
        fun initMeta(context: Context) {
            if (!AudienceNetworkAds.isInitialized(context)) {
                if (BuildConfig.DEBUG) {
                    myLogger.logThis(TAG, "META Ads DEBUG ON", Log.DEBUG)
                    // AdSettings.setDebugBuild(true)
                    // AdSettings.setTestMode(true)
                    AdSettings.turnOnSDKDebugger(context)
                }
                AudienceNetworkAds.buildInitSettings(context)
                    .withInitListener(this)
                    .initialize()
            }
        }
        override fun onInitialized(p0: AudienceNetworkAds.InitResult?) {
            myLogger.logThis(TAG, "META Ads onInitialized result: ${p0?.message}", Log.DEBUG)
        }

    }

    /**MONITOR UNITY INITIALIZATION*/
//    inner class UnityManager: IUnityAdsInitializationListener {
//        fun initUnity(context: Context) {
//            if (!UnityAds.isInitialized()) {
//                if (BuildConfig.DEBUG) {
//                    myLogger.logThis(TAG, "UNITY Ads DEBUG ON", Log.DEBUG)
//                    // UnityAds.setDebugMode(true)
//                }
//                UnityAds.initialize(context, AdConfig.unityGameId, false, this)
//            }
//        }
//        override fun onInitializationComplete() {
//            myLogger.logThis(TAG, "UNITY Ads onInitializationComplete", Log.DEBUG)
//        }
//        override fun onInitializationFailed(p0: UnityAds.UnityAdsInitializationError?, p1: String?) {
//            myLogger.logThis(TAG, "UNITY Ads onInitializationFailed message: ${p0.toString()}, $p1", Log.DEBUG)
//        }
//    }

}