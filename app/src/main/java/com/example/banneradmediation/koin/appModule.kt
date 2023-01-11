package com.example.banneradmediation.koin

import com.example.banneradmediation.MainViewModel
import com.example.banneradmediation.tools.MyLogging
import org.koin.dsl.module

val appModule = module {

    // New Instances required by the app
    factory { MyLogging() }

}

val viewModelModule = module {

    // Singletons
    single { MainViewModel() }

}