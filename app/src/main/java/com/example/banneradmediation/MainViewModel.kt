package com.example.banneradmediation

import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.Job


@Suppress("PrivatePropertyName", "UNUSED_PARAMETER")
class MainViewModel: ViewModel() {

    // Fragments
    val mainView = 0
    val fragment = 1

    // Show fragments
    var showView = mutableStateOf(mainView)

    /**AD VIEWS*/
    val mainAdViewGroup = mutableStateOf<ViewGroup?>(null)
    val fragmentAdViewGroup = mutableStateOf<ViewGroup?>(null)
    /**SHOW PERMIT STATES*/
    var showBanner = mutableStateOf(true)
    var showFragmentBanner = mutableStateOf(true)
    /**AD IS LOADED STATES*/
    val bannerAdLoaded = mutableStateOf(false)
    val bannerFragmentAdLoaded = mutableStateOf(false)
    /**AD VIEW JOBS STATES*/
    var mainBannerJob = mutableStateOf<Job?>(null)
    var fragmentBannerJob = mutableStateOf<Job?>(null)
    /**RELOAD REQUEST STATE*/
    val reloadAd = mutableStateOf(0L)


    fun nullBannerViews() {
        mainAdViewGroup.value = null
        fragmentAdViewGroup.value = null
    }

    fun destroyBannerViews() {
        val list = listOf(mainAdViewGroup.value, fragmentAdViewGroup.value)
        list.forEach {
            when (it) {
                is AdView -> { it.destroy() }
                is com.facebook.ads.AdView -> { it.destroy() }
            }
        }
    }

    fun pauseBannerViews() {
        val list = listOf(mainAdViewGroup.value, fragmentAdViewGroup.value)
        list.forEach {
            when (it) {
                is AdView -> { it.pause() }
                is com.facebook.ads.AdView -> { /*Meta has no Pause function*/ }
            }
        }
    }

    fun resumeBannerViews() {
        val list = listOf(mainAdViewGroup.value, fragmentAdViewGroup.value)
        list.forEach {
            when (it) {
                is AdView -> { it.resume() }
                is com.facebook.ads.AdView -> { /*Meta has no Resume function*/ }
            }
        }
    }

    fun nullBannerJobs() {
        mainBannerJob.value = null
        fragmentBannerJob.value = null
    }


}