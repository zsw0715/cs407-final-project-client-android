package com.cs407.knot_client_android.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import com.cs407.knot_client_android.data.model.response.ConversationListResp
import com.cs407.knot_client_android.data.model.response.CreateGroupResp

interface ConversationApi {
    @GET("/api/conversation/list")
    suspend fun getConversationList(
        @Header("Authorization") authorization: String // "Bearer <token>"
    ): ConversationListResp

    @POST("/api/conversation/createGroup")
    suspend fun createGroupConversation(
        @Header("Authorization") authorization: String,
        @Query("groupName") groupName: String,
        // 后端是 "6,8" 格式，使用 String
        @Query("memberIds") memberIds: String
    ): CreateGroupResp
}
