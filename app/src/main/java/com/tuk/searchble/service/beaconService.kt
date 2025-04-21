package com.tuk.searchble.service

import com.tuk.searchble.model.Beacon
import com.tuk.searchble.model.MajorChangeEvent
import com.tuk.searchble.model.toIBeaconData

/**
 * 각 Beacon 데이터를 처리하여 iBeaconData를 추출한 후,
 * 해당 Beacon의 이전 major 값과 비교하여 변경이 있을 경우에만 이벤트를 생성합니다.
 *
 * @param majorChangeEvents 변경 이벤트가 저장된 MutableList
 * @param beacon 처리할 Beacon 데이터
 * @return Beacon의 major 값이 변경되었으면 생성된 MajorChangeEvent, 그렇지 않으면 null
 */
fun processBeacon(
    majorChangeEvents: MutableList<MajorChangeEvent>,
    beacon: Beacon
): MajorChangeEvent? {
    val iBeaconData = beacon.manufacturerSpecificData?.toIBeaconData() ?: return null

    return if (isMajorChanged(majorChangeEvents, beacon.id, iBeaconData.major)) {
        createMajorChangeEvent(beacon.id, iBeaconData.major)
    } else {
        null
    }
}

/**
 * 주어진 majorChangeEvents 리스트에서 해당 beaconId의 마지막 이벤트를 조회한 후,
 * 새로운 major 값(newMajor)과 비교하여 변경 여부를 판단합니다.
 *
 * @param majorChangeEvents 현재까지 저장된 이벤트 리스트
 * @param beaconId 현재 처리 중인 Beacon의 ID
 * @param newMajor 새로 읽은 major 값
 * @return 해당 beacon의 이전 major 값과 다르면 true, 그렇지 않으면 false
 */
private fun isMajorChanged(
    majorChangeEvents: List<MajorChangeEvent>,
    beaconId: String,
    newMajor: Int
): Boolean {
    val lastEventForBeacon = majorChangeEvents.lastOrNull { it.beaconId == beaconId }
    return lastEventForBeacon == null || lastEventForBeacon.major != newMajor
}

/**
 * beacon id와 현재 major 값을 이용하여 MajorChangeEvent를 생성합니다.
 *
 * @param beaconId Beacon ID
 * @param major 현재 major 값
 * @return 생성된 MajorChangeEvent
 */
private fun createMajorChangeEvent(beaconId: String, major: Int): MajorChangeEvent {
    return MajorChangeEvent(
        beaconId = beaconId,
        timestamp = System.currentTimeMillis(),
        major = major
    )
}