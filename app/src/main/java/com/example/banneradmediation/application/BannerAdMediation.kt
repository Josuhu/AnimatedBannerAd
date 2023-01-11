package com.example.banneradmediation.application

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.banneradmediation.koin.appModule
import com.example.banneradmediation.koin.viewModelModule
import com.google.firebase.FirebaseApp
import com.example.banneradmediation.ads.MyAdManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

// Initialize the dataStore so that it is singleton
const val storeName = "com.example.banneradmediation"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(storeName)

class BannerAdMediation: Application() {

    /**Call all application classess in here which need be initialized for whole app*/
    override fun onCreate() {
        super.onCreate()
        // Firebase must be initialised in here to call remoteConfig singleton within app
        FirebaseApp.initializeApp(this)
        // Set Ad providers except AdMob in here
        val adManager = MyAdManager()
        adManager.initialiseMeta(this)
        // adManager.initialiseUnity(this)
        // Koin
        startKoin {
            androidContext(this@BannerAdMediation)
            modules(appModule, viewModelModule)
        }
    }

}