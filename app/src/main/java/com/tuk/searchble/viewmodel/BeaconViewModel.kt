package com.tuk.searchble.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuk.searchble.model.Beacon
import com.tuk.searchble.model.MajorChangeEvent
import com.tuk.searchble.service.processBeacon
import com.tuk.searchble.repository.BeaconRepository
import com.tuk.searchble.service.BeaconScanService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BeaconViewModel @Inject constructor(
    private val beaconRepository: BeaconRepository,
    @ApplicationContext private val context: Context
) : ViewModel(), IBeaconViewModel {

    // 스캔된 Beacon 목록
    override val beaconListFlow: StateFlow<List<Beacon>> = beaconRepository.beaconsFlow

    // major 값 변경 이벤트를 기록하는 리스트
    val majorChangeEvents = mutableStateListOf<MajorChangeEvent>()

    // 각 Beacon의 마지막 major 값은 여기서 체크하지 않고, 모든 이벤트를 기록함 (필요하다면 중복 체크 로직 추가 가능)
    init {
        viewModelScope.launch {
            beaconListFlow.collect { beaconList ->
                // 각 Beacon을 처리하여 변경 이벤트가 발생한 경우에만 새 이벤트를 필터링합니다.
                val newEvents = beaconList.mapNotNull { beacon ->
                    processBeacon(majorChangeEvents, beacon)
                }
                if (newEvents.isNotEmpty()) {
                    majorChangeEvents.addAll(newEvents)
                }
            }
        }
    }

    override fun startScan() {
        Intent(context, BeaconScanService::class.java).also {
            it.action = BeaconScanService.ACTION_START
            ContextCompat.startForegroundService(context, it)
        }
    }

    override fun stopScan() {
        Intent(context, BeaconScanService::class.java).also {
            it.action = BeaconScanService.ACTION_STOP
            context.stopService(it)
        }
    }

    /**
     * major 값 변경 이벤트들을 CSV 파일로 저장합니다.
     * 파일은 앱의 외부 파일 디렉토리에 "major_log.csv"라는 이름으로 생성됩니다.
     * CSV 파일에는 "Timestamp,BeaconID,Major" 형식으로 각 이벤트를 기록합니다.
     */
    override fun saveMajorChangeEventsToUri(uri: Uri, macAddress: String): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.writer().use { writer ->
                        writer.write("Timestamp,BeaconID,Major\n")
                        majorChangeEvents.forEach { event ->
                            if (event.beaconId != macAddress) return@forEach
                            val line = "${event.timestamp},${event.beaconId},${event.major}\n"
                            writer.write(line)
                        }
                    }
                } ?: throw Exception("OutputStream is null")
                Log.d("BeaconViewModel", "CSV 저장 성공: $uri")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("BeaconViewModel", "CSV 파일 저장 실패", e)
                Result.failure(e)
            }
        }
    }

    override fun onCleared() {
        beaconRepository.stopScan()
        super.onCleared()
    }
}