package com.tuk.searchble.viewmodel

import android.net.Uri
import com.tuk.searchble.model.Beacon

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeBeaconViewModel : IBeaconViewModel {
    override val beaconListFlow: StateFlow<List<Beacon>> = MutableStateFlow(
        listOf(
            Beacon(
                id = "10:06:1C:9F:36:C2",
                name = "ESP32_BEACON",
                manufacturerSpecificData = byteArrayOf(
                0x4C.toByte(), 0x00.toByte(), // 회사 ID: 4C00
                0x02.toByte(),               // Beacon 타입: 02
                0x15.toByte(),               // 데이터 길이: 15 (21)
                // UUID: E2 C5 6D B5 DF FB 48 D2 B0 60 D0 F5 A7 10 96 E0
                0xE2.toByte(), 0xC5.toByte(), 0x6D.toByte(), 0xB5.toByte(),
                0xDF.toByte(), 0xFB.toByte(), 0x48.toByte(), 0xD2.toByte(),
                0xB0.toByte(), 0x60.toByte(), 0xD0.toByte(), 0xF5.toByte(),
                0xA7.toByte(), 0x10.toByte(), 0x96.toByte(), 0xE0.toByte(),
                // Major: 0x0001
                0x00.toByte(), 0x01.toByte(),
                // Minor: 0x0002
                0x00.toByte(), 0x02.toByte(),
                // TX 파워: 0xC5 (signed -59)
                0xC5.toByte()
            )),
        )
    )

    override fun startScan() {
        // 프리뷰에서는 동작하지 않아도 됨
    }

    override fun stopScan() {
        // 프리뷰에서는 동작 않함
    }

    override fun saveMajorChangeEventsToUri(uri: Uri, macAddress: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}