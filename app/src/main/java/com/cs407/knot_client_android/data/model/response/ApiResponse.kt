package com.cs407.knot_client_android.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 通用 API 响应包装类
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?,
    @SerializedName("error") val error: String?
)

data class FriendListResp(
    val success: Boolean,
    val message: String?,
    val data: List<FriendItemDto>?,
    val error: String?
)

data class FriendItemDto(
    val friendId: Long,
    val username: String,
    val avatar: String?,
    val createdAtMs: Long,
    val convId: Long
)

