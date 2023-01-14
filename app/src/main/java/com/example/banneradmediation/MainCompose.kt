package com.example.banneradmediation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.banneradmediation.composables.MyBannerAdView
import com.example.banneradmediation.tools.PrefsDataStore

abstract class MainCompose: MainInterface {

    @Composable
    fun MainFragfment(
        viewModel: MainViewModel,
        dataStore: DataStore<Preferences>,
        onBackPressed: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
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
                onClick = { navigateTo(viewModel.fragment) },
            ) {
                Text(text = "Main view adapting with banner size")
            }
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = { onBackPressed() },
            ) {
                Text(text = "BackButton")
            }
        }
    }

    @Composable
    fun Fragment(
        viewModel: MainViewModel,
        dataStore: DataStore<Preferences>,
        onBackPressed: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
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
                onClick = { navigateTo(viewModel.mainView) },
            ) {
                Text(text = "Fragment view adapting with banner size")
            }
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = { onBackPressed() },
            ) {
                Text(text = "BackButton")
            }
        }
    }

}