package com.cs407.knot_client_android.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import com.cs407.knot_client_android.data.model.response.MessagePageResp

/**
 * 消息 API 服务接口
 */
interface MessageApi {

    /**
     * 获取会话消息，分页
     * @param authorization Bearer token
     * @param conversationId 会话 ID
     * @param page 页码
     * @param size 每页大小
     */
    @GET("/api/conversation/messages")
    suspend fun getMessages(
        @Header("Authorization") authorization: String,
        @Query("conversationId") conversationId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): MessagePageResp
}
