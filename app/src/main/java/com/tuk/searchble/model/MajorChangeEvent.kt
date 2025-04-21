package com.tuk.searchble.model

data class MajorChangeEvent(
    val beaconId: String,
    val timestamp: Long,
    val major: Int
)