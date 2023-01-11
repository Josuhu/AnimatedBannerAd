package com.example.banneradmediation.secrets

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.banneradmediation.tools.PrefsDataStore


@Suppress("unused")
object AdConfig {

    /**SET THIS TO ENABLE AD SERVING IN TEST MODE*/
    const val testMode = false
    const val mediationTestSuite = false

    /**CUSTOM ADS RELOADER PARAMS*/ // 1s = 1000ms
    const val reloadTime10 = 10000L
    const val reloadTime30 = 30000L
    const val reloadTime60 = 60000L
    const val reloadTime90 = 90000L
    const val reloadTime120 = 120000L
    const val reloadTime150 = 150000L
    const val reloadTime180 = 180000L

    /**SELECTOR*/
    const val adMob = "ADMOB"
    const val unity = "UNITY"
    const val meta = "META"

    /**ADMOB*/
    const val bannerId = "Your Id here"
    const val bannerTestId = "ca-app-pub-3940256099942544/6300978111"
    /**META*/
    const val metaBannerId = "Your Id here"
    const val metaBannerTestId = "IMG_16_9_APP_INSTALL#Your Id here"
    const val testDeviceHash = "Your test hash in here from Logcat"
    /**UNITY*/
    // const val unityGameId = "Your Id here"
    // const val unityPlacementId = "You Banner Id here"

    /**TRACK RELOAD BANNER ADS WITH TIME SINCE LAST LOADED AD SUCCESS
     * USE THESE ONLY IF IN ADSPROVIDER YOU NEED TO MANUALLY UPDATE ADS REFRESH.
     * FOR SOME REASON ADVIEW CAN FREEZE AND DOES NOT REFRESH at onResume() WITHOUT HELP*/

    suspend fun timeToLoadAd(bannerKey: String, timeToLoad: Long, dataStore: DataStore<Preferences>): Boolean {
        val maxTime = if (timeToLoad <= 0) { reloadTime90 } else { timeToLoad }
        val lastLoadedTime = PrefsDataStore.getAdLoadedTime(bannerKey, dataStore)
        return System.currentTimeMillis() - lastLoadedTime > maxTime
    }

    suspend fun updateTimeSinceAdLoaded(bannerKey: String, dataStore: DataStore<Preferences>) {
        PrefsDataStore.saveAdLoadedTime(System.currentTimeMillis(), bannerKey, dataStore)
    }

}
