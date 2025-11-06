package com.cs407.knot.data.repo

import com.cs407.knot.data.model.*
import kotlinx.coroutines.flow.Flow


interface ChatRepository {
    val chats: Flow<List<ChatPreview>>
}


interface UserRepository {
    val friendRequests: Flow<List<FriendRequest>>
    val friends: Flow<List<Friend>>
    suspend fun accept(req: FriendRequest)
    suspend fun decline(req: FriendRequest)
    suspend fun remove(friend: Friend)
}