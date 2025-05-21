package com.tuk.searchble.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.tuk.searchble.MainActivity
import com.tuk.searchble.R
import com.tuk.searchble.repository.BeaconRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BeaconScanService : LifecycleService() {
    @Inject lateinit var beaconRepository: BeaconRepository

    companion object {
        const val ACTION_START = "ACTION_START_SCAN"
        const val ACTION_STOP  = "ACTION_STOP_SCAN"
        const val NOTIF_ID     = 1
    }

    @SuppressLint("MissingSuperCall")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> startInForeground()
            ACTION_STOP  -> {
                beaconRepository.stopScan()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return Service.START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun startInForeground() {
        // 1) Notification 준비
        val tapIntent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, tapIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )

        val notif: Notification = NotificationCompat.Builder(this, "beacon_scan")
            .setContentTitle("BLE 스캔 동작 중")
            .setContentText("비콘을 검색하고 있습니다.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)   // 실제 아이콘 리소스로 대체
            .setContentIntent(pending)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "중지",
                PendingIntent.getService(
                    this, 1,
                    Intent(this, BeaconScanService::class.java).setAction(ACTION_STOP),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        PendingIntent.FLAG_IMMUTABLE
                    else 0
                )
            )
            .build()

        // 2) 포그라운드 서비스 시작
        ContextCompat.startForegroundService(this, Intent(this, BeaconScanService::class.java).apply {
            action = ACTION_START
        })
        startForeground(NOTIF_ID, notif)

        // 3) 실제 스캔 시작
        beaconRepository.startScan()
    }

    inner class LocalBinder : Binder() {
        fun isScanning() = beaconRepository.isScanningFlow.value
    }
    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }
}