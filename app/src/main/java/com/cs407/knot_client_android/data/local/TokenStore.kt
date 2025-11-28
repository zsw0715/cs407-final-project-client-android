package com.cs407.knot_client_android.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Token 存储类
 * 负责保存和获取认证相关的 token 和用户信息
 */
class TokenStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    /** 保存 access token、refresh token 以及可选的用户 ID 和用户名 */
    fun save(
        accessToken: String,
        refreshToken: String,
        userId: Long? = null,
        username: String? = null
    ) {
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            if (userId != null) {
                putLong("user_id", userId)
            }
            if (username != null) {
                putString("username", username)
            }
        }.apply()
    }

    /** 单独获取 access token */
    fun getAccessToken(): String? = prefs.getString("access_token", null)

    /** 单独获取 refresh token */
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    /** 如果你只想要 access token，也可以保留这个简化别名 */
    fun get(): String? = getAccessToken()

    /** 获取用户 ID，如果不存在则返回 null */
    fun getUserId(): Long? =
        if (prefs.contains("user_id")) prefs.getLong("user_id", -1L) else null

    /** 获取用户名，如果不存在则返回 null */
    fun getUsername(): String? = prefs.getString("username", null)

    /** 清除所有存储的 token 和用户信息 */
    fun clear() {
        prefs.edit().clear().apply()
    }
}
