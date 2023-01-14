package com.example.banneradmediation.ads

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import com.example.banneradmediation.secrets.AdConfig
import com.example.banneradmediation.tools.ComposeUtils
import com.example.banneradmediation.tools.MyLogging
import com.facebook.ads.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError


@Suppress("FunctionName", "PrivatePropertyName")
abstract class AdsProvider: AdsProviderInterface {

    @Suppress("unused")
    private val TAG = "AdsProvider"

    private val adMobTAG = "AdsProvider ADMOB"
    private val metaTAG = "AdsProvider META"
    // private val unityTAG = "AdsProvider UNITY"
    private val myLogger = MyLogging()

    fun selectAdView(
        context: Context,
        bannerKey: String,
        screenWidth: Int,
        selector: String,
        adaptiveBanner: Boolean,
        testMode: Boolean,
    ): ViewGroup {
        return when (selector) {
            AdConfig.adMob -> { setupAdMob(context, bannerKey = bannerKey, adaptiveBanner = adaptiveBanner, screenWidth, testMode = testMode) }
            AdConfig.meta -> { setupMeta(context, bannerKey = bannerKey, testMode = testMode) }
            // AdConfig.unity -> { setupUnity(context, unityBanner, isMainBanner = isMainBanner) } // Unit testMode has to be in Initialization
            else -> { setupAdMob(context, bannerKey = bannerKey, adaptiveBanner = adaptiveBanner, screenWidth, testMode = testMode) }
        }
    }

    /**Returns AdMob AdView*/
    @SuppressLint("VisibleForTests")
    private fun setupAdMob(
        context: Context,
        bannerKey: String,
        adaptiveBanner: Boolean,
        screenWidth: Int,
        testMode: Boolean
    ): com.google.android.gms.ads.AdView {
        return com.google.android.gms.ads.AdView(context).apply {
            try {
                if (adaptiveBanner) {
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidth))
                } else { setAdSize(AdSize.BANNER) }
                adUnitId = if (!testMode) { AdConfig.bannerId } else { AdConfig.bannerTestId }
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        myLogger.logThis(adMobTAG,"MyBannerAdView onAdLoaded adapter class name:" + responseInfo?.mediationAdapterClassName, Log.DEBUG)
                        Handler(Looper.getMainLooper()).postDelayed({ _onAdLoaded(ComposeUtils.dpFromPx(context, height), bannerKey) }, 500)
                    }
                    override fun onAdImpression() {
                        super.onAdImpression()
                        myLogger.logThis(adMobTAG, "MyBannerAdView onAdImpression", Log.DEBUG)
                        _onAdImpression()
                    }
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        myLogger.logThis(adMobTAG, "MyBannerAdView onAdFailedToLoad ${adError.message}", Log.ERROR)
                        myLogger.logThis(adMobTAG, "MyBannerAdView responseInfo: ${adError.responseInfo.toString()}", Log.ERROR)
                        _onAdFailedToLoad()
                    }
                    override fun onAdOpened() {
                        myLogger.logThis(adMobTAG, "MyBannerAdView onAdOpened", Log.DEBUG)
                        _onAdOpened()
                    }
                    override fun onAdClicked() {
                        myLogger.logThis(adMobTAG, "MyBannerAdView onAdClicked", Log.DEBUG)
                        _onAdClicked()
                    }
                    override fun onAdClosed() {
                        myLogger.logThis(adMobTAG, "MyBannerAdView onAdClosed", Log.DEBUG)
                        _onAdClosed()
                    }
                }
                // Request an ad
                loadAd(AdRequest.Builder().build())
            } catch (e: IllegalStateException) {
                myLogger.logThis(adMobTAG, "MyBannerAdView ${e.message.toString()}", Log.ERROR)
            }
        }
    }

    /**Returns Meta AdView*/
    private fun setupMeta(
        context: Context,
        bannerKey: String,
        testMode: Boolean
    ): AdView {
        val adId = if (!testMode) { AdConfig.metaBannerId } else { AdConfig.metaBannerTestId }
        return AdView(context, adId, com.facebook.ads.AdSize.BANNER_HEIGHT_50).apply {
            try {
                val adListener = object : com.facebook.ads.AdListener {
                    override fun onAdLoaded(p0: Ad?) {
                        myLogger.logThis(metaTAG,"MyBannerAdView onAdLoaded ID:" + p0?.placementId, Log.DEBUG)
                        Handler(Looper.getMainLooper()).postDelayed({ _onAdLoaded(ComposeUtils.dpFromPx(context, height), bannerKey) }, 500)
                    }
                    override fun onLoggingImpression(p0: Ad?) {
                        myLogger.logThis(metaTAG, "MyBannerAdView onAdImpression ID:" + p0?.placementId, Log.DEBUG)
                        _onAdImpression()
                    }
                    override fun onError(p0: Ad?, p1: AdError?) {
                        myLogger.logThis(metaTAG, "MyBannerAdView onAdFailedToLoad message: ${p1?.errorMessage}, error: ${p1?.errorCode}", Log.ERROR)
                        _onAdFailedToLoad()
                    }
                    override fun onAdClicked(p0: Ad?) {
                        myLogger.logThis(metaTAG, "MyBannerAdView onAdClicked ID:" + p0?.placementId, Log.DEBUG)
                        _onAdClicked()
                        _onAdOpened()
                    }
                }
                // Request an ad
                if (testMode) {
                    AdSettings.addTestDevice(AdConfig.testDeviceHash)
                }
                loadAd(buildLoadAdConfig().withAdListener(adListener).build())
            } catch (e: Exception) {
                myLogger.logThis(metaTAG, "MyBannerAdView ${e.message.toString()}", Log.ERROR)
            }
        }
    }

    /**Returns Unity AdView*/
//    private fun setupUnity(
//        context: Context,
//        bannerKey: String,
//    ): BannerView {
//        return BannerView(context as Activity?, AdConfig.unityPlacementId, UnityBannerSize(320, 50)).apply {
//            try {
//                listener = object : BannerView.IListener {
//                    override fun onBannerLoaded(bannerAdView: BannerView?) {
//                        myLogger.logThis(unityTAG,"MyBannerAdView onAdLoaded ID:" + bannerAdView?.placementId, Log.DEBUG)
//                        Handler(Looper.getMainLooper()).postDelayed({ _onAdLoaded(ComposeUtils.dpFromPx(context, height), bannerKey) }, 500)
//                    }
//                    override fun onBannerClick(bannerAdView: BannerView?) {
//                        myLogger.logThis(unityTAG, "MyBannerAdView onAdClicked ID:" + bannerAdView?.placementId, Log.DEBUG)
//                        _onAdClicked()
//                    }
//                    override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: BannerErrorInfo?) {
//                        myLogger.logThis(unityTAG, "MyBannerAdView onAdFailedToLoad message: ${errorInfo?.errorMessage}, error: ${errorInfo?.errorCode}", Log.ERROR)
//                        _onAdFailedToLoad()
//                    }
//                    override fun onBannerLeftApplication(bannerView: BannerView?) {
//                        myLogger.logThis(unityTAG, "MyBannerAdView onBannerLeftApplication ID:" + bannerView?.placementId, Log.DEBUG)
//                        _onAdOpened()
//                    }
//                }
//                // Request an ad
//                load()
//            } catch (e: Exception) {
//                myLogger.logThis(unityTAG, "MyBannerAdView ${e.message.toString()}", Log.ERROR)
//            }
//        }
//    }

}