package com.cs407.knot_client_android.ui.chat

import com.cs407.knot_client_android.data.model.response.ConversationDto
import com.cs407.knot_client_android.data.model.response.ConversationListResp

// UI 模型
data class ConversationUi(
    val id: Long,
    val convType: Int,
    val creatorId: Long,
    val displayTitle: String,
    val avatarUrl: String?,
    val lastMsgPreview: String?,
    val lastMsgTimeIso: String?
)

// DTO -> UI
fun ConversationDto.toUi(): ConversationUi = ConversationUi(
    id = convId,
    convType = convType,
    creatorId = creatorId,
    // 先支持单聊：优先展示对端用户名；否则回退到 title / 创建者 ID
    displayTitle = when (convType) {
        1 -> otherUserName ?: (title?.takeIf { it.isNotBlank() } ?: creatorId.toString())
        else -> title?.takeIf { it.isNotBlank() } ?: creatorId.toString()
    },
    // 单聊优先使用 otherUserAvatar，将来群聊可以用 groupAvatar
    avatarUrl = when (convType) {
        1 -> otherUserAvatar
        else -> groupAvatar
    },
    lastMsgPreview = lastMsgPreview,
    lastMsgTimeIso = lastMsgTime
)

fun ConversationListResp.toUiList(): List<ConversationUi> =
    (data ?: emptyList()).map { it.toUi() }

