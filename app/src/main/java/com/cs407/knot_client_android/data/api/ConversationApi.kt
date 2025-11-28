package com.cs407.knot_client_android.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import com.cs407.knot_client_android.data.model.response.ConversationListResp

interface ConversationApi {
    @GET("/api/conversation/list")
    suspend fun getConversationList(
        @Header("Authorization") authorization: String // "Bearer <token>"
    ): ConversationListResp
}
