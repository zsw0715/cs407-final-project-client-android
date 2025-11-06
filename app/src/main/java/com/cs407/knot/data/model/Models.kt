package com.cs407.knot.data.model

data class ChatPreview(
    val id: String,
    val title: String,
    val lastMessage: String,
    val time: String,
    val avatarUrls: List<String> = emptyList(),
)


data class Friend(val id: String, val name: String, val avatarUrl: String)


data class FriendRequest(val id: String, val fromName: String, val fromAvatar: String, val preview: String)