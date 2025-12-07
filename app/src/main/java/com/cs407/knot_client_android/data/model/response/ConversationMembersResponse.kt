package com.cs407.knot_client_android.data.model.response

data class ConversationMemberDto(
    val nickname: String?,
    val email: String?,
    val gender: String?,
    val statusMessage: String?,
    val avatarUrl: String?,
    val birthdate: String?,
    val privacyLevel: String?,
    val discoverable: Boolean?
)

data class ConversationMembersResp(
    val success: Boolean,
    val message: String?,
    val data: List<ConversationMemberDto>?,
    val error: String?
)
