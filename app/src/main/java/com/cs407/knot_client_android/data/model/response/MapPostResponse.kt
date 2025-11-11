package com.cs407.knot_client_android.data.model.response

/**
 * 附近帖子 API 响应
 */
data class NearbyMapPostsResponse(
    val success: Boolean,
    val message: String,
    val data: List<MapPostNearby>?,
    val error: String?
)

/**
 * 附近帖子数据模型（匹配后端返回结构）
 */
data class MapPostNearby(
    val mapPostId: Long,
    val convId: Long,
    val title: String,
    val description: String,
    val mediaUrls: List<String>?,
    val locLat: Double,
    val locLng: Double,
    val locName: String,
    val distance: Double,
    val creatorId: Long,
    val creatorUsername: String,
    val creatorAvatar: String?,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val postType: String,
    val createdAtMs: Long
)

