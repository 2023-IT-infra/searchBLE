package com.tuk.searchble.model

data class IBeaconData(
    val companyId: String,   // 25바이트 데이터일 경우에 사용 (23바이트 데이터는 "Unknown")
    val beaconType: String,  // iBeacon 타입 (예: "02")
    val length: Int,         // 데이터 길이 (예: 21)
    val uuid: String,        // 16바이트 UUID (8-4-4-4-12 형식)
    val major: Int,          // Major 값
    val minor: Int,          // Minor 값
    val txPower: Int         // TX 파워 (signed)
)

// ByteArray를 16진수 문자열로 변환 (각 바이트를 두 자리 16진수로 표시)
fun ByteArray.toHexString(): String = joinToString(separator = " ") { String.format("%02X", it) }

// IBeacon에 해당하는 형식인지 확인
fun ByteArray.isIBeacon(): Boolean {
    return when (this.size) {
        23 -> true
        else -> false
    }
}

fun ByteArray.toIBeaconData(): IBeaconData? {
    return when (this.size) {
        23 -> {
            // 23바이트 데이터: iBeacon 데이터에서 회사 ID가 포함되지 않은 경우
            // 구조:
            // [0]: Beacon 타입 (보통 0x02)
            // [1]: 데이터 길이 (보통 0x15, 즉 21)
            // [2] ~ [17]: 16바이트 UUID
            // [18] ~ [19]: Major (2바이트, big-endian)
            // [20] ~ [21]: Minor (2바이트, big-endian)
            // [22]: TX 파워 (1바이트, signed)
            val beaconType = String.format("%02X", this[0])
            val length = this[1].toInt() and 0xFF
            val uuidBytes = this.sliceArray(2 until 18)  // index 2~17 (16바이트)
            val uuidHex = uuidBytes.joinToString("") { String.format("%02X", it) }
            val formattedUUID = uuidHex.substring(0, 8) + "-" +
                    uuidHex.substring(8, 12) + "-" +
                    uuidHex.substring(12, 16) + "-" +
                    uuidHex.substring(16, 20) + "-" +
                    uuidHex.substring(20)
            val major = ((this[18].toInt() and 0xFF) shl 8) or (this[19].toInt() and 0xFF)
            val minor = ((this[20].toInt() and 0xFF) shl 8) or (this[21].toInt() and 0xFF)
            val txPower = this[22].toInt()
            // 회사 ID 정보가 없으므로 "Unknown" 또는 빈 문자열로 설정
            IBeaconData("Unknown", beaconType, length, formattedUUID, major, minor, txPower)
        }
        in 25..Int.MAX_VALUE -> {
            // 25바이트 이상: 기존 형식 (회사 ID 포함)
            val companyId = String.format("%02X%02X", this[0], this[1])
            val beaconType = String.format("%02X", this[2])
            val length = this[3].toInt() and 0xFF
            val uuidBytes = this.sliceArray(4 until 20)  // index 4~19 (16바이트)
            val uuidHex = uuidBytes.joinToString("") { String.format("%02X", it) }
            val formattedUUID = uuidHex.substring(0, 8) + "-" +
                    uuidHex.substring(8, 12) + "-" +
                    uuidHex.substring(12, 16) + "-" +
                    uuidHex.substring(16, 20) + "-" +
                    uuidHex.substring(20)
            val major = ((this[20].toInt() and 0xFF) shl 8) or (this[21].toInt() and 0xFF)
            val minor = ((this[22].toInt() and 0xFF) shl 8) or (this[23].toInt() and 0xFF)
            val txPower = this[24].toInt()
            IBeaconData(companyId, beaconType, length, formattedUUID, major, minor, txPower)
        }
        else -> null // 예상 데이터 형식이 아닐 경우 null 반환
    }
}