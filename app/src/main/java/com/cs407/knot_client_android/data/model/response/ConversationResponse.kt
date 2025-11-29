package com.cs407.knot_client_android.data.model.response

data class ConversationDto(
    val convId: Long,
    val convType: Int,
    val title: String?,
    val creatorId: Long,
    val otherUserId: Long?,
    val otherUserName: String?,
    val otherUserAvatar: String?,
    val selfUserId: Long?,
    val selfUserName: String?,
    val selfUserAvatar: String?,
    val groupAvatar: String?,
    val memberCount: Int?,
    val lastMsgPreview: String?,
    val lastMsgType: Int?,
    val lastMsgTime: String?,
    val updatedAt: String?
)

data class ConversationListResp(
    val success: Boolean,
    val message: String?,
    val data: List<ConversationDto>?,
    val error: String?
)
