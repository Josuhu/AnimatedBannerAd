package com.example.banneradmediation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.banneradmediation.ads.MyAdManager
import com.example.banneradmediation.application.dataStore
import com.example.banneradmediation.composables.MyBannerAdView
import com.example.banneradmediation.firebase.RemoteConfig
import com.example.banneradmediation.secrets.AdConfig
import com.example.banneradmediation.tools.MyLogging
import com.example.banneradmediation.tools.PrefsDataStore
import com.example.banneradmediation.ui.theme.BannerAdMediationTheme
import com.google.android.ads.mediationtestsuite.MediationTestSuite
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get


class MainActivity : ComponentActivity() {

    @Suppress("PrivatePropertyName")
    private val TAG = "MainActivity"
    private val scopeIO = CoroutineScope(Dispatchers.IO)

    /**KOIN MODULES*/
    private val viewModel = get<MainViewModel>()
    private val myLogger = get<MyLogging>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**FETCH AND ACTIVATE REMOTECONFIG*/
        RemoteConfig.setFirebaseAdConfig()
        /**GOOGLE ADS MAIN INITIALISATION*/
        MyAdManager().initialiseAdMob(this)

        setContent {
            val showFragment = viewModel.showView.value
            BannerAdMediationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    when (showFragment) {
                        /**MAIN FRAGMENT*/
                        viewModel.mainView -> {
                            Column(modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                MyBannerAdView(
                                    viewModel = viewModel,
                                    bannerId = "MainBanner",
                                    bannerKey = PrefsDataStore.bannerAdLoadedKey,
                                    dataStore = dataStore,
                                    viewGroup = viewModel.mainAdViewGroup,
                                    bannerAdLoaded = viewModel.bannerAdLoaded,
                                    showBannerAd = viewModel.showBanner,
                                    bannerJob = viewModel.mainBannerJob
                                )
                                TextButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    onClick = { viewModel.showView.value = viewModel.fragment },
                                ) {
                                    Text(text = "Main view adapting with banner size")
                                }
                            }
                        }
                        /**SECOND FRAGMENT*/
                        viewModel.fragment -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                MyBannerAdView(
                                    viewModel = viewModel,
                                    bannerId = "FragmentBanner",
                                    bannerKey = PrefsDataStore.fragmentBannerAdLoadedKey,
                                    dataStore = dataStore,
                                    viewGroup = viewModel.fragmentAdViewGroup,
                                    bannerAdLoaded = viewModel.bannerFragmentAdLoaded,
                                    showBannerAd = viewModel.showFragmentBanner,
                                    bannerJob = viewModel.fragmentBannerJob
                                )
                                TextButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    onClick = { viewModel.showView.value = viewModel.mainView },
                                ) {
                                    Text(text = "Fragment view adapting with banner size")
                                }
                            }
                        }
                    }

                }
            }
        }
        /**MEDIATION TEST SUITE IF ENABLED IN ADCONFIG*/
        // Set your test devices. Check your logcat output for the hashed device ID
        if (AdConfig.mediationTestSuite) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdConfig.testDeviceHash)).build())
            MediationTestSuite.launch(this)
        }
    }

    override fun onStart() {
        super.onStart()
        /**ACTIVATE NEW REMOTECONFIG VALUES*/
        RemoteConfig.activateNewValues()
        /**FORCE AD RELOAD IF TIME TO LOAD*/
        scopeIO.launch {
            val reloadTime = RemoteConfig.getRemoteConfig().getLong(RemoteConfig.AD_RELOAD_TIME_MS)
            val keys = listOf(PrefsDataStore.bannerAdLoadedKey, PrefsDataStore.fragmentBannerAdLoadedKey)
            keys.forEach { key ->
                if (AdConfig.timeToLoadAd(key, reloadTime, dataStore)) {
                    Handler(Looper.getMainLooper()).postDelayed( {
                        myLogger.logThis(TAG, "onStart reload request for Ads", Log.DEBUG)
                        viewModel.reloadAd.value++ }, 500)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeBannerViews()
    }

    override fun onPause() {
        viewModel.pauseBannerViews()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.nullBannerJobs()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroyBannerViews()
        viewModel.nullBannerViews()
    }

}