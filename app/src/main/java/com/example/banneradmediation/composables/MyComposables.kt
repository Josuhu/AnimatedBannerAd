package com.example.banneradmediation.composables

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.banneradmediation.MainViewModel
import com.example.banneradmediation.ads.AdsProvider
import com.example.banneradmediation.firebase.RemoteConfig
import com.example.banneradmediation.secrets.AdConfig
import com.example.banneradmediation.tools.ComposeUtils
import com.example.banneradmediation.tools.MyLogging
import com.example.banneradmediation.tools.NetworkConnection
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject


private const val TAG = "MyComposables"
private val myLogger by inject<MyLogging>(MyLogging::class.java)

private val dotSize = 10.dp
private const val delayUnit = 300
private val spaceSize = 4.dp


/**AD VIEW FUNCTIONS*/
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

@Composable
fun AdLoadingAnimation(adLoaded: Boolean, height: Dp) {
    if (!adLoaded && NetworkConnection.isOnline(LocalContext.current.applicationContext)) {
        DotsFlashing(height)
    } else {
        Box(modifier = Modifier.fillMaxWidth().height(height))
    }
}

@Composable
fun DotsFlashing(height: Dp) {
    val minAlpha = 0.1f

    @Composable
    fun Dot(
        alpha: Float
    ) = Spacer(
        Modifier
            .size(dotSize)
            .alpha(alpha)
            .background(
                color = MaterialTheme.colors.primary,
                shape = CircleShape
            )
    )

    val infiniteTransition = rememberInfiniteTransition()

    @Composable
    fun animateAlphaWithDelay(delay: Int) = infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = minAlpha,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = delayUnit * 4
                minAlpha at delay with LinearEasing
                1f at delay + delayUnit with LinearEasing
                minAlpha at delay + delayUnit * 2
            }
        )
    )

    val alpha1 by animateAlphaWithDelay(0)
    val alpha2 by animateAlphaWithDelay(delayUnit)
    val alpha3 by animateAlphaWithDelay(delayUnit * 2)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .height(height)
    ) {
        Dot(alpha1)
        Spacer(Modifier.width(spaceSize))
        Dot(alpha2)
        Spacer(Modifier.width(spaceSize))
        Dot(alpha3)
    }
}