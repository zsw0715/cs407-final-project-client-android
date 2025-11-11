package com.cs407.knot_client_android.data.model.request

/**
 * 获取附近帖子的请求参数
 */
data class NearbyRequest(
    val lat: Double,
    val lng: Double,
    val zoomLevel: Int,
    val timeRange: String = "7D",  // 时间范围：1D, 7D, 30D
    val postType: String = "ALL",  // 帖子类型：ALL, REQUEST, COMMENT
    val maxResults: Int = 100      // 最大结果数
)

