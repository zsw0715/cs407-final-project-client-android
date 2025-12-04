package com.cs407.knot_client_android.data.model

data class SharedPostPayload(
    val mapPostId: Long,
    val convId: Long,
    val creatorId: Long,
    val title: String,
    val description: String?,
    val coverUrl: String?,
    val creatorUsername: String,
    val locName: String?,
    val locLat: Double,
    val locLng: Double
)

data class SharedPostNavigation(
    val payload: SharedPostPayload,
    val requestId: Long = System.currentTimeMillis()
)
