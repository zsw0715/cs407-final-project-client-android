package com.cs407.knot_client_android.data.api

import com.cs407.knot_client_android.data.model.response.UserSettingsResponse
import com.cs407.knot_client_android.data.model.request.UpdateUserSettingsRequest
import com.cs407.knot_client_android.data.model.response.ApiResponse
import com.cs407.knot_client_android.data.model.response.FriendRequestView
import com.cs407.knot_client_android.data.model.response.UserInfo
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.Query

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

    /**
     * 更新用户设置
     * @param authorization Bearer token
     * @param request 更新请求体
     */
    @PUT("/api/user/settings")
    suspend fun updateUserSettings(
        @Header("Authorization") authorization: String,
        @Body request: UpdateUserSettingsRequest
    ): UserSettingsResponse


    /**
     * 按用户名获取用户信息
     */
    @GET("/api/user/info")
    suspend fun getUserInfoByUsername(
        @Header("Authorization") authorization: String,
        @Query("username") username: String
    ): ApiResponse<UserInfo>

    /**
     * 获取收到的好友申请列表
     */
    @GET("/api/friends/request/list")
    suspend fun getFriendRequests(
        @Header("Authorization") authorization: String
    ): ApiResponse<List<FriendRequestView>>


}

