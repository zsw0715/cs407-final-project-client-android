package com.cs407.knot_client_android.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// 单条消息
data class MessageDto(
    val msgId: Long,
    val convId: Long,
    val senderId: Long,      // ✅ 对应后端 senderId
    val msgType: Int,
    val clientMsgId: String,
    val contentText: String,
    val createdAt: String    // ✅ 对应后端 createdAt
)

// 分页数据（在 "data" 里面）
data class MessagePageDto(
    val convId: Long,
    val page: Int,
    val size: Int,
    val total: Int,
    val totalPages: Int,
    val messageList: List<MessageDto>
)

// 最外层响应
data class MessagePageResp(
    val success: Boolean,
    val message: String?,
    val data: MessagePageDto?,
    val error: String?
)

interface MessageApi {

    @GET("/api/conversation/messages")
    suspend fun getMessages(
        @Header("Authorization") authorization: String,
        @Query("conversationId") conversationId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): MessagePageResp
}
