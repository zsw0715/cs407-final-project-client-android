package com.cs407.knot_client_android.data.repository

import android.content.Context
import com.cs407.knot_client_android.data.api.RetrofitProvider
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.data.model.request.LoginRequest
import com.cs407.knot_client_android.data.model.request.RegisterRequest
import com.cs407.knot_client_android.data.model.request.RefreshTokenRequest
import com.cs407.knot_client_android.utils.JwtUtils

/**
 * 认证仓库
 * 负责处理登录、注册等认证相关业务逻辑
 */
class AuthRepository(context: Context, baseUrl: String) {
    private val apiService = RetrofitProvider.createAuthService(baseUrl)
    private val tokenStore = TokenStore(context)

    /**
     * 登录
     */
    suspend fun login(username: String, password: String) {
        val response = apiService.login(LoginRequest(username, password))
        if (response.success && response.data != null) {
            tokenStore.save(response.data.accessToken, response.data.refreshToken)
        } else {
            error(response.message ?: "Login failed")
        }
    }

    /**
     * 注册并自动登录
     */
    suspend fun registerThenLoginIfNeeded(username: String, password: String) {
        val response = apiService.register(RegisterRequest(username, password))
        if (response.success) {
            login(username, password)
        } else {
            error(response.message ?: "Register failed")
        }
    }

    /**
     * 获取 Access Token
     */
    fun getAccessToken(): String? = tokenStore.getAccessToken()

    /**
     * 获取 Refresh Token
     */
    fun getRefreshToken(): String? = tokenStore.getRefreshToken()

    /**
     * 刷新 Token
     * @return 成功返回 true，失败返回 false
     */
    suspend fun refreshToken(): Boolean {
        return try {
            val refreshToken = tokenStore.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                return false
            }
            
            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.success && response.data != null) {
                tokenStore.save(response.data.accessToken, response.data.refreshToken)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 尝试自动登录
     * 检查本地 token，如果 AT 有效则直接成功，如果 AT 过期但 RT 有效则刷新
     * @return 成功返回 true（可以直接进入主页），失败返回 false（需要重新登录）
     */
    suspend fun tryAutoLogin(): Boolean {
        val accessToken = tokenStore.getAccessToken()
        val refreshToken = tokenStore.getRefreshToken()
        
        // 没有任何 token
        if (accessToken.isNullOrBlank() && refreshToken.isNullOrBlank()) {
            return false
        }
        
        // AT 未过期，直接成功
        if (!JwtUtils.isTokenExpired(accessToken)) {
            return true
        }
        
        // AT 过期，但有 RT，尝试刷新
        if (!refreshToken.isNullOrBlank()) {
            return refreshToken()
        }
        
        // 都过期了
        return false
    }

    /**
     * 清除登录状态
     */
    fun logout() {
        tokenStore.clear()
    }
}

