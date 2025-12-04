package com.cs407.knot_client_android.data.model.response

// 单条消息
data class MessageDto(
    val msgId: Long,
    val convId: Long,
    val senderId: Long,
    val msgType: Int,
    val clientMsgId: String,
    val contentText: String,
    val createdAt: String,
    val mediaUrl: String? = null   // 图片等富媒体 URL，可为空
)

// 分页数据
data class MessagePageDto(
    val convId: Long,
    val page: Int,
    val size: Int,
    val total: Int,
    val totalPages: Int,
    val messageList: List<MessageDto>
)

// 最外层响应
data class MessagePageResp(
    val success: Boolean,
    val message: String?,
    val data: MessagePageDto?,
    val error: String?
)
