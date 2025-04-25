package com.tuk.searchble

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.getSystemService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
// MyBeaconApplication.kt
class MyBeaconApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "beacon_scan",
                "Beacon Scan Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "백그라운드에서 BLE 스캔을 유지합니다."
            }
            getSystemService<NotificationManager>()?.createNotificationChannel(channel)
        }
    }
}