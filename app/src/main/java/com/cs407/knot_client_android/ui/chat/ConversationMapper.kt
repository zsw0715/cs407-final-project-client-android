package com.cs407.knot_client_android.ui.chat

import com.cs407.knot_client_android.data.api.ConversationDto
import com.cs407.knot_client_android.data.api.ConversationListResp

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
    displayTitle = (title?.takeIf { it.isNotBlank() } ?: creatorId.toString()),
    avatarUrl = avatarUrl,
    lastMsgPreview = lastMsgPreview,
    lastMsgTimeIso = lastMsgTime
)

fun ConversationListResp.toUiList(): List<ConversationUi> =
    (data ?: emptyList()).map { it.toUi() }

