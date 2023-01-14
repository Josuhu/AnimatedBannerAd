package com.example.banneradmediation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.example.banneradmediation.ads.MyAdManager
import com.example.banneradmediation.application.dataStore
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


    /**IMPLEMENT COMPOSE DRIVEN FUNCTIONS IN HERE*/
    inner class ComposeManager: MainCompose() {
        override fun navigateTo(intent: Int) {
            viewModel.showView.value = intent
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**FETCH AND ACTIVATE REMOTECONFIG*/
        RemoteConfig.setFirebaseAdConfig()
        /**GOOGLE ADS MAIN INITIALISATION*/
        MyAdManager().initialiseAdMob(this)

        /**HANDLE ONBACKPRESSED()*/
        // Handles the onBackPressed() override function
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.showView.value != viewModel.mainView) {
                    viewModel.showView.value = viewModel.mainView
                    return
                }
                finish()
            }
        })
        val compose = ComposeManager()
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
                            compose.MainFragfment(
                                viewModel = viewModel,
                                dataStore = dataStore,
                                onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                            )
                        }
                        /**SECOND FRAGMENT*/
                        viewModel.fragment -> {
                            compose.Fragment(
                                viewModel = viewModel,
                                dataStore = dataStore,
                                onBackPressed = { onBackPressedDispatcher.onBackPressed() }
                            )
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
        viewModel.nullBannerJobs()
        super.onStop()
    }

    override fun onDestroy() {
        viewModel.destroyBannerViews()
        viewModel.nullBannerViews()
        super.onDestroy()
    }

}