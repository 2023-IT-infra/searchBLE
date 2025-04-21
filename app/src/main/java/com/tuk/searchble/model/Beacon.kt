package com.tuk.searchble.model

data class Beacon(
    val id: String,               // MAC 주소
    val name: String,
    val manufacturerSpecificData: ByteArray? = null  // 광고 데이터에서 추출한 제조사 특정 데이터
)