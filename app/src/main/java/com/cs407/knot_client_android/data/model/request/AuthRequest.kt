package com.cs407.knot_client_android.data.model.request

/**
 * 登录请求
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * 注册请求
 */
data class RegisterRequest(
    val username: String,
    val password: String
)

/**
 * 刷新 Token 请求
 */
data class RefreshTokenRequest(
    val refreshToken: String
)

