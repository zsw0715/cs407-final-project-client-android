package com.cs407.knot_client_android.ui.friend

import androidx.compose.runtime.Immutable

enum class FriendRequestStatus { Pending, Accepted, Rejected }

enum class FriendRequestDirection { Incoming, Outgoing }

@Immutable
data class FriendUserSummary(
    val userId: Long?,
    val username: String?,
    val avatarUrl: String? = null
) {
    val displayName: String
        get() = when {
            !username.isNullOrBlank() -> username
            userId != null -> "User #$userId"
            else -> "Unknown"
        }
}

@Immutable
data class FriendRequestItem(
    val requestId: Long,
    val message: String,
    val timestamp: Long?,
    val status: FriendRequestStatus,
    val direction: FriendRequestDirection,
    val peer: FriendUserSummary,
    val convId: Long? = null
)

@Immutable
data class BannerMessage(
    val text: String,
    val isError: Boolean = false
)

@Immutable
data class FriendUiState(
    val wsUrl: String = "ws://10.0.2.2:10827/ws",
    val isConnected: Boolean = false,
    val currentUserId: Long? = null,
    val incomingRequests: List<FriendRequestItem> = emptyList(),
    val outgoingRequests: List<FriendRequestItem> = emptyList(),
    val friends: List<FriendUserSummary> = emptyList(),
    val logs: List<String> = emptyList(),
    val bannerMessage: BannerMessage? = null
)


