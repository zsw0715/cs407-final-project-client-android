package com.cs407.knot_client_android.data.api

import com.cs407.knot_client_android.data.model.response.UserSettingsResponse
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * 用户 API 服务接口
 */
interface UserApiService {
    /**
     * 获取用户设置
     * @param authorization Bearer token
     */
    @GET("/api/user/settings")
    suspend fun getUserSettings(
        @Header("Authorization") authorization: String
    ): UserSettingsResponse
}

