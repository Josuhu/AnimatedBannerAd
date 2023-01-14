package com.example.banneradmediation.ads

import androidx.compose.ui.unit.Dp

interface AdsProviderInterface {

    /**Override these functions within application to get responses from the selected Ad*/
    fun _onAdLoaded(height: Dp, bannerKey: String)
    fun _onAdImpression()
    fun _onAdFailedToLoad()
    fun _onAdOpened()
    fun _onAdClicked()
    fun _onAdClosed()
    // fun _onAdLeftApplication()

}