package com.tuk.searchble.viewmodel

import android.content.Context
import android.net.Uri
import com.tuk.searchble.model.Beacon
import kotlinx.coroutines.flow.StateFlow

interface IBeaconViewModel {
    val beaconListFlow: StateFlow<List<Beacon>>

    fun startScan()
    fun stopScan()
    fun saveMajorChangeEventsToUri(uri: Uri, macAddress: String): Result<Unit>
}