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

