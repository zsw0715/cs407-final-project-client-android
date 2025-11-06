package com.cs407.knot.data.repo

import com.cs407.knot.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class FakeChatRepo : ChatRepository {
    private val _data = MutableStateFlow(
        listOf(
            ChatPreview("1","Fu Liye","Frostpunk 那个游戏不要太离谱！","11:15", listOf("https://i.pravatar.cc/150?img=1")),
            ChatPreview("2","Li Yuqiao","桥老板最近哪里发财呢？","09:33", listOf("https://i.pravatar.cc/150?img=4")),
            ChatPreview("3","Group Chat 1","When will GTA6 come out?","08:12", listOf("https://i.pravatar.cc/150?img=7","https://i.pravatar.cc/150?img=10"))
        )
    )
    override val chats = _data.asStateFlow()
}


class FakeUserRepo : UserRepository {
    private val _req = MutableStateFlow(listOf(FriendRequest("r1","Lily","https://i.pravatar.cc/150?img=15","Hello?")))
    private val _friends = MutableStateFlow(listOf(Friend("f1","Fu Liye","https://i.pravatar.cc/150?img=5")))
    override val friendRequests = _req.asStateFlow()
    override val friends = _friends.asStateFlow()
    override suspend fun accept(req: FriendRequest) { _req.value = _req.value - req; _friends.value = _friends.value + Friend(req.id, req.fromName, req.fromAvatar) }
    override suspend fun decline(req: FriendRequest) { _req.value = _req.value - req }
    override suspend fun remove(friend: Friend) { _friends.value = _friends.value - friend }
}