package com.cs407.knot_client_android.data.repository

import android.content.Context
import com.cs407.knot_client_android.data.api.RetrofitProvider
import com.cs407.knot_client_android.data.local.TokenStore

class FriendRepository(context: Context, baseUrl: String) {

    private val api = RetrofitProvider.createFriendService(baseUrl)
    private val tokenStore = TokenStore(context)

    suspend fun getIncomingRequests() =
        api.getIncomingRequests("Bearer ${tokenStore.getAccessToken()}")

    suspend fun getFriends() =
        api.getFriends("Bearer ${tokenStore.getAccessToken()}")
}
