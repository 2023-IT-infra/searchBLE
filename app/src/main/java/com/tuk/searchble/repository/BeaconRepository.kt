package com.tuk.searchble.repository

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.tuk.searchble.model.Beacon
import com.tuk.searchble.model.isIBeacon
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BeaconRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // BLE 초기화
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val bleScanner = bluetoothAdapter?.bluetoothLeScanner

    // 스캔된 Beacon 목록을 Flow로 노출
    private val _beaconsFlow = MutableStateFlow<List<Beacon>>(emptyList())
    val beaconsFlow = _beaconsFlow.asStateFlow()
    // Beacon 객체를 저장하는 Map (key: MAC 주소)
    private val foundBeacons = mutableMapOf<String, Beacon>()

    // BLE 스캔 Callback
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val beaconId = device.address
                val scanRecord = result.scanRecord
                val deviceName: String = scanRecord?.deviceName ?: "Unknown"
                val manufacturerData = scanRecord?.manufacturerSpecificData

                // 비콘인 형식의 기기만 출력
                if (manufacturerData != null &&
                    manufacturerData.size() > 0
                    ) {

                    val manufacturerSpecificData = manufacturerData.valueAt(0)

                    if (manufacturerSpecificData.isIBeacon()) {
                        val beacon = Beacon(
                            id = beaconId,
                            name = deviceName,
                            manufacturerSpecificData = manufacturerSpecificData
                        )
                        // 기존 Beacon이 있더라도 업데이트
                        foundBeacons[beaconId] = beacon
                        _beaconsFlow.value = foundBeacons.values.toList()
                    }


                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>) {
            results.forEach { result ->
                result?.device?.let { device ->
                    val beaconId = device.address
                    val scanRecord = result.scanRecord
                    val deviceName: String = scanRecord?.deviceName ?: "Unknown"
                    val manufacturerData: ByteArray? = scanRecord?.manufacturerSpecificData?.valueAt(0)
                    val beacon = Beacon(
                        id = beaconId,
                        name = deviceName,
                        manufacturerSpecificData = manufacturerData
                    )
                    foundBeacons[beaconId] = beacon
                }
            }
            _beaconsFlow.value = foundBeacons.values.toList()
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BeaconRepository", "BLE 스캔 실패: errorCode=$errorCode")
            Toast.makeText(context, "BLE 스캔 실패: errorCode=$errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * BLE 스캔 시작
     * API 레벨에 따라 필요한 권한을 확인합니다.
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        val filters: List<ScanFilter> = emptyList()
        bleScanner?.startScan(filters, settings, scanCallback)
        Toast.makeText(context, "BLE 스캔 시작", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bleScanner?.stopScan(scanCallback)
        Toast.makeText(context, "BLE 스캔 중지", Toast.LENGTH_SHORT).show()
    }
}