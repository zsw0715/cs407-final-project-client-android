package com.cs407.knot_client_android.data.model.response

data class ConversationActionData(
    val conversationId: Long,
    val userId: Long
)

data class ConversationActionResp(
    val success: Boolean,
    val message: String?,
    val data: ConversationActionData?,
    val error: String?
)
