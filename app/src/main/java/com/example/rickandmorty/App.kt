package com.example.rickandmorty

import android.app.Application
import android.util.Log
import com.example.rickandmorty.config.di.appModule
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        val db = Firebase.firestore

        startKoin {
            androidContext(this@App)  // Передаем контекст приложения
            modules(appModule)        // Подключаем модули
        }

        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            Log.e("CRASH", "Uncaught exception", ex)
            // Можно сохранить лог или показать пользователю
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}