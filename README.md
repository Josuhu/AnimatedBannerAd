# AnimatedBannerAd
Animated Jetpack Compose Banner ad with software based mediation. Choose programmatically which supplier ads are active (Admob, Meta and Unity). Use local selector values or Google Firebase RemoteConfig for Ad configuration.

In respect with Better Ads coalition requiremements.

NOTE! The test setup will not work without your google-service.Json file for Firebase remote config and Google Admob integration to show ads. Among any other add supplier such as Meta & Unity which are options in the setup.

If you wish to simplify the setup then please check the most relevant classes as below. Other enviromental functions support this in larger scale.


private fun targetViewHeight(context: Context, view: ViewGroup?, orientation: Int, screenHeight: Int, defaultHeight: Dp): Dp {
    return if (view == null) {
        // AnchoredAdaptiveBanner height is never more than max 15% of screen height.
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            (screenHeight * 0.10f).dp
        } else { (screenHeight * 0.15f).dp }
    } else {
        if (view.height > 0) {
            ComposeUtils.dpFromPx(context, view.height)
        } else { defaultHeight }
    }
}


@SuppressLint("VisibleForTests")
@Composable
fun MyBannerAdView(
    viewModel: MainViewModel,
    bannerId: String,
    bannerKey: String,
    dataStore: DataStore<Preferences>,
    viewGroup: MutableState<ViewGroup?>,
    bannerAdLoaded: MutableState<Boolean>,
    showBannerAd: MutableState<Boolean>,
    bannerJob: MutableState<Job?>
) {
    viewModel.reloadAd.value
    val scope = rememberCoroutineScope()

    /**USE REMOTE CONFIG WITH FIREBASE OR OTHER SERVICE OR REPLACE THE VALUES WITH FIXED DEFAULTS*/
    val reloadTime = RemoteConfig.getRemoteConfig().getLong(RemoteConfig.AD_RELOAD_TIME_MS)
    val selector = RemoteConfig.getRemoteConfig().getString(RemoteConfig.AD_CONFIG)
    val adaptiveBanner = RemoteConfig.getRemoteConfig().getBoolean(RemoteConfig.ADMOB_ADAPTIVE_BANNER)

    val selectedView = viewGroup.value
    val configuration = LocalConfiguration
    val screenWidth = configuration.current.screenWidthDp
    val screenHeight = configuration.current.screenHeightDp
    val orientation = configuration.current.orientation
    val defaultBannerHeight = 50.dp
    val adLoaded = bannerAdLoaded.value
    val bannerMaxHeight = targetViewHeight(LocalContext.current, selectedView, orientation, screenHeight, defaultBannerHeight)
    val targetHeight = remember { mutableStateOf(bannerMaxHeight) }
    val backgroundHeight by animateDpAsState(targetHeight.value, TweenSpec(1000))
    val showBanner = showBannerAd.value
    if (showBanner) {
        Box(modifier = Modifier
            .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AdLoadingAnimation(adLoaded, backgroundHeight)
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    if (selectedView == null) {
                        val adsManager = object : AdsProvider() {
                            override fun _onAdLoaded(height: Dp, bannerKey: String) {
                                super._onAdLoaded(height, bannerKey)
                                targetHeight.value = height
                                bannerAdLoaded.value = true
                                bannerJob.value = null
                            }

                            override fun _onAdFailedToLoad() {
                                super._onAdFailedToLoad()
                                targetHeight.value = defaultBannerHeight
                                bannerAdLoaded.value = true
                            }

                            override fun _onAdOpened() {
                                super._onAdOpened()
                                showBannerAd.value = false
                            }
                        }
                        viewGroup.value = adsManager.selectAdView(
                            context = context,
                            bannerKey = bannerKey,
                            screenWidth = screenWidth,
                            selector = selector,
                            adaptiveBanner = adaptiveBanner,
                            testMode = AdConfig.testMode
                        )
                        viewGroup.value!!
                    } else { selectedView }
                },
                update = {
                    // Force manual reload during Composition update if ad has not been loaded due time limit
                    scope.launch {
                        if (AdConfig.timeToLoadAd(bannerKey, reloadTime, dataStore) && bannerJob.value == null) {
                            bannerJob.value = launch {
                                when (it) {
                                    is AdView -> { launch(Dispatchers.Main) {
                                        myLogger.logThis(TAG, "$bannerId ADMOB reload", Log.DEBUG)
                                        it.loadAd(AdRequest.Builder().build())
                                    } }
                                    is com.facebook.ads.AdView -> { launch(Dispatchers.Main) {
                                        myLogger.logThis(TAG, "$bannerId META reload", Log.DEBUG)
                                        it.loadAd()
                                    } }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Suppress("FunctionName", "PrivatePropertyName")
open class AdsProvider: Application() {

    @Suppress("unused")
    private val TAG = "AdsProvider"

    private val adMobTAG = "AdsProvider ADMOB"
    private val metaTAG = "AdsProvider META"
    // private val unityTAG = "AdsProvider UNITY"

    private val myLogger by inject<MyLogging>(MyLogging::class.java)
    private val scopeIO = CoroutineScope(Dispatchers.IO)

    /**Override these functions within application to get responses from the selected Ad*/
    open fun _onAdLoaded(height: Dp, bannerKey: String) { scopeIO.launch { AdConfig.updateTimeSinceAdLoaded(bannerKey, dataStore) } }
    open fun _onAdImpression() {}
    open fun _onAdFailedToLoad() {}
    open fun _onAdOpened() {}
    open fun _onAdClicked() {}
    open fun _onAdClosed() {}
    @Suppress("unused")
    open fun _onAdLeftApplication() {}

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
