package com.cs407.knot_client_android.ui.friend
data class FriendUiState(
    val isSearching: Boolean = false,
    val searchedUser: FriendUserSummary? = null,
    val searchError: String? = null,
    val incomingRequests: List<FriendRequestItem> = emptyList(),
    val friends: List<FriendUserSummary> = emptyList()
)

data class FriendUserSummary(
    val userId: Long,
    val username: String,
    val avatar: String?,
    val convId: Long?,
    val createdAtMs: Long?
)

data class FriendRequestItem(
    val requestId: Long,
    val requesterId: Long,
    val message: String,
    val timestamp: Long?,
    val requesterName: String? = null
)
