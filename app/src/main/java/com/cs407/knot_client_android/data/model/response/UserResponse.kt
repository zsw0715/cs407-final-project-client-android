package com.cs407.knot_client_android.data.model.response

/**
 * 用户设置响应
 */
data class UserSettingsResponse(
    val success: Boolean,
    val message: String?,
    val data: UserSettings?,
    val error: String?
)

/**
 * 用户设置数据
 */
data class UserSettings(
    val nickname: String?,
    val email: String?,
    val gender: String?,
    val statusMessage: String?,
    val avatarUrl: String?,
    val birthdate: String?,
    val privacyLevel: String?,
    val discoverable: Boolean?
)


/**
 * 用户基础信息（用于按用户名查询）
 * 对应后端返回的 data 字段：
 */
data class UserInfo(
    val userid: Long,
    val username: String,
    val nickname: String?,
    val email: String?,
    val gender: String?,
    val age: Int?,
    val birthdate: String?,
    val avatarUrl: String?,
    val accountStatus: String?,
    val lastLatitude: Double?,
    val lastLongitude: Double?,
    val lastLocationUpdate: String?,
    val statusMessage: String?,
    val lastOnlineTime: String?,
    val discoverable: Boolean?,
    val privacyLevel: String?,
    val deviceId: String?,
    val createdAt: String?
)

/**
 * 好友申请视图（对应后端 FriendRequestView）
 */
data class FriendRequestView(
    val requestId: Long,
    val requesterId: Long,
    val receiverId: Long,
    val message: String,
    val status: Int,
    val createdAtMs: Long,
    val convId: Long?
)
