package com.example.banneradmediation.tools

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.inject
import java.io.IOException
import java.lang.ClassCastException


object PrefsDataStore {

    private const val TAG = "PREFS DATASTORE"
    private val myLogger by inject<MyLogging>(MyLogging::class.java)

    const val bannerAdLoadedKey = "BANNER_ADS_LOADED_KEY"
    const val fragmentBannerAdLoadedKey = "FRAGMENT_BANNER_ADS_LOADED_KEY"

    suspend fun saveAdLoadedTime(time: Long, key: String, dataStore: DataStore<Preferences>): Boolean {
        try {
            dataStore.edit { it[longPreferencesKey(key)] = time }
            return true
        } catch (e: ClassCastException) {
            myLogger.logThis(TAG, "saveAdLoadedTime ${e.message.toString()}", Log.ERROR)
        } catch (e: IOException) {
            myLogger.logThis(TAG, "saveAdLoadedTime ${e.message.toString()}", Log.ERROR)
        } catch (e: Exception) {
            myLogger.logThis(TAG, "saveAdLoadedTime ${e.message.toString()}", Log.ERROR)
        }
        return false
    }

    suspend fun getAdLoadedTime(key: String, dataStore: DataStore<Preferences>): Long {
        try {
            val value: Flow<Long?> = dataStore.data.map { it[longPreferencesKey(key)] }
            return value.first() ?: 0
        } catch (e: ClassCastException) {
            myLogger.logThis(TAG, "getAdLoadedTime ${e.message.toString()}", Log.ERROR)
        } catch (e: IOException) {
            myLogger.logThis(TAG, "getAdLoadedTime ${e.message.toString()}", Log.ERROR)
        } catch (e: NoSuchElementException) {
            myLogger.logThis(TAG, "getAdLoadedTime ${e.message.toString()}", Log.ERROR)
        }
        return 0
    }

}