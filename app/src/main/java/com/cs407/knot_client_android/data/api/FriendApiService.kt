package com.cs407.knot_client_android.data.api

import com.cs407.knot_client_android.data.model.response.ApiResponse
import com.cs407.knot_client_android.ui.friend.FriendRequestItem
import com.cs407.knot_client_android.ui.friend.FriendUserSummary
import retrofit2.http.GET
import retrofit2.http.Header

interface FriendApiService {

    @GET("/api/friends/request/list")
    suspend fun getIncomingRequests(
        @Header("Authorization") authorization: String
    ): ApiResponse<List<FriendRequestItem>>

    @GET("/api/friends/list")
    suspend fun getFriends(
        @Header("Authorization") authorization: String
    ): ApiResponse<List<FriendUserSummary>>
}
