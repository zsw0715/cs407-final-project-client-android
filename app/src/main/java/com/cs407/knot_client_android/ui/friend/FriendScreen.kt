package com.cs407.knot_client_android.ui.friend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cs407.knot_client_android.navigation.Screen
import com.cs407.knot_client_android.ui.components.FloatingActionButton
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FriendScreen(
    navController: NavHostController,
    viewModel: FriendViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var receiverIdInput by rememberSaveable { mutableStateOf("") }
    var messageInput by rememberSaveable {
        mutableStateOf("Hi, I'm User1. Let's be friends!")
    }

    FriendScreenContent(
        navController = navController,
        state = uiState,
        receiverIdInput = receiverIdInput,
        messageInput = messageInput,
        onReceiverIdChange = { receiverIdInput = it },
        onMessageChange = { messageInput = it },
        onSendRequest = {
            viewModel.sendFriendRequest(receiverIdInput, messageInput)
            receiverIdInput = ""
        },
        onAccept = viewModel::acceptRequest,
        onReject = viewModel::rejectRequest,
        onResend = viewModel::resendRequest,
        onRemoveFriend = viewModel::removeFriend,
        onToggleConnection = viewModel::toggleConnection,
        onWsUrlChange = viewModel::onWsUrlChange,
        onReconnect = viewModel::reconnect,
        onBannerDismiss = viewModel::clearBanner
    )
}

@Composable
private fun FriendScreenContent(
    navController: NavHostController,
    state: FriendUiState,
    receiverIdInput: String,
    messageInput: String,
    onReceiverIdChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendRequest: () -> Unit,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onResend: (FriendRequestItem) -> Unit,
    onRemoveFriend: (FriendUserSummary) -> Unit,
    onToggleConnection: () -> Unit,
    onWsUrlChange: (String) -> Unit,
    onReconnect: () -> Unit,
    onBannerDismiss: () -> Unit
) {
    val backgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFFF8F6F4), Color(0xFFF3F0FA))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {

        LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Friend Management System",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedVisibility(visible = state.bannerMessage != null) {
                        state.bannerMessage?.let { banner ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (banner.isError) Color(0xFFFFE4E6) else Color(0xFFE0F7FA),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = banner.text,
                                        color = if (banner.isError) Color(0xFFB42318) else Color(0xFF026E78),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Dismiss",
                                        modifier = Modifier
                                            .padding(start = 12.dp)
                                            .clickable { onBannerDismiss() }
                                            .padding(4.dp),
                                        color = Color(0xFF6366F1),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                ConnectionCard(
                    state = state,
                    onToggleConnection = onToggleConnection,
                    onWsUrlChange = onWsUrlChange,
                    onReconnect = onReconnect
                )
            }

            item {
                SendRequestCard(
                    receiverIdInput = receiverIdInput,
                    messageInput = messageInput,
                    onReceiverIdChange = onReceiverIdChange,
                    onMessageChange = onMessageChange,
                    onSendRequest = onSendRequest,
                    enabled = state.isConnected
                )
            }

            item {
                RequestListCard(
                    title = "Incoming requests",
                    subtitle = null,
                    emptyHint = "No pending friend requests.",
                    requests = state.incomingRequests,
                    actionContent = { item ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onAccept(item.requestId) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Accept")
                            }
                            OutlinedButton(
                                onClick = { onReject(item.requestId) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reject")
                            }
                        }
                    }
                )
            }

            item {
                RequestListCard(
                    title = "Requests I sent",
                    subtitle = null,
                    emptyHint = "You haven't sent any requests yet.",
                    requests = state.outgoingRequests,
                    actionContent = { item ->
                        if (item.status == FriendRequestStatus.Pending) {
                            OutlinedButton(onClick = { onResend(item) }) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Resend")
                            }
                        }
                    }
                )
            }

            item {
                FriendSectionCard(
                    title = "Friend list",
                    subtitle = null
                ) {
                    if (state.friends.isEmpty()) {
                        Text(
                            text = "No friends yet. Anyone you connect with shows up here.",
                            color = Color(0xFF6B7280),
                            fontSize = 13.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.friends.forEach { friend ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.7f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = friend.displayName,
                                                fontWeight = FontWeight.Medium
                                            )
                                            friend.userId?.let {
                                                Text(
                                                    text = "User ID: $it",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF6B7280)
                                                )
                                            }
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Ready to chat",
                                                fontSize = 12.sp,
                                                color = Color(0xFF10B981)
                                            )
                                            OutlinedButton(onClick = { onRemoveFriend(friend) }) {
                                                Text("Remove")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                FriendSectionCard(
                    title = "Realtime log",
                    subtitle = null
                ) {
                    val lastLogs = state.logs.takeLast(6).reversed()
                    if (lastLogs.isEmpty()) {
                        Text(
                            text = "Connect to the socket to start seeing AUTH / ACK / push events.",
                            color = Color(0xFF6B7280),
                            fontSize = 13.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            lastLogs.forEach { log ->
                                Text(
                                    text = log,
                                    fontSize = 12.sp,
                                    color = Color(0xFF111827),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // 右下角浮动按钮 - 返回聊天页面

        FloatingActionButton(
            icon = Icons.Outlined.Email,
            onClick = {
                navController.navigate(Screen.Main.createRoute("CHAT")) {
                    popUpTo(Screen.Main.createRoute("MAP")) { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 30.dp)
        )
    }
}

@Composable
private fun ConnectionCard(
    state: FriendUiState,
    onToggleConnection: () -> Unit,
    onWsUrlChange: (String) -> Unit,
    onReconnect: () -> Unit
) {
    FriendSectionCard(
        title = "Connection",
        subtitle = null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.isConnected) "Status: Connected" else "Status: Disconnected",
                fontWeight = FontWeight.Medium,
                color = if (state.isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
            )
            AssistChip(
                onClick = {},
                label = { Text(text = state.currentUserId?.let { "UID $it" } ?: "Signed out") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFE0E7FF)
                )
            )
        }
        OutlinedTextField(
            value = state.wsUrl,
            onValueChange = onWsUrlChange,
            label = { Text("WebSocket URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onToggleConnection,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isConnected) Color(0xFFEF4444) else Color(0xFF6366F1)
                )
            ) {
                Text(if (state.isConnected) "Disconnect" else "Connect")
            }
            OutlinedButton(
                onClick = onReconnect,
                modifier = Modifier.weight(1f)
            ) {
                Text("Reconnect")
            }
        }
    }
}

@Composable
private fun SendRequestCard(
    receiverIdInput: String,
    messageInput: String,
    onReceiverIdChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSendRequest: () -> Unit,
    enabled: Boolean
) {
    FriendSectionCard(
        title = "Send request",
        subtitle = null
    ) {
        OutlinedTextField(
            value = receiverIdInput,
            onValueChange = onReceiverIdChange,
            label = { Text("Receiver user ID") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = messageInput,
            onValueChange = onMessageChange,
            label = { Text("Message (optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            minLines = 2
        )
        Button(
            onClick = onSendRequest,
            enabled = enabled && receiverIdInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send")
        }
    }
}

@Composable
private fun RequestListCard(
    title: String,
    subtitle: String?,
    emptyHint: String,
    requests: List<FriendRequestItem>,
    actionContent: @Composable (FriendRequestItem) -> Unit
) {
    FriendSectionCard(title = title, subtitle = subtitle) {
        if (requests.isEmpty()) {
            Text(text = emptyHint, color = Color(0xFF6B7280), fontSize = 13.sp)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                requests.forEachIndexed { index, item ->
                    RequestCard(item = item, actionContent = { actionContent(item) })
                    if (index != requests.lastIndex) {
                        Divider(color = Color(0xFFE5E7EB))
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    item: FriendRequestItem,
    actionContent: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = item.peer.displayName, fontWeight = FontWeight.Medium)
                Text(
                    text = "Message: ${item.message}",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = if (item.direction == FriendRequestDirection.Incoming) "Awaiting your action" else "Waiting on peer",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (item.direction == FriendRequestDirection.Incoming) Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                        contentDescription = null
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (item.direction == FriendRequestDirection.Incoming) Color(0xFFFFF7ED) else Color(0xFFE0E7FF)
                )
            )
        }
        Text(
            text = "Time: ${formatTimestamp(item.timestamp)}",
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF)
        )
        actionContent()
    }
}

@Composable
private fun FriendSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            subtitle?.let {
                Text(text = it, fontSize = 13.sp, color = Color(0xFF6B7280))
            }
            content()
        }
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0) return "--"
    return try {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        timestamp.toString()
    }
}

@Preview(showBackground = true)
@Composable
fun FriendScreenPreview() {
    val sampleState = FriendUiState(
        isConnected = true,
        incomingRequests = listOf(
            FriendRequestItem(
                requestId = 1,
                message = "Hi, I'm User1. Let's be friends!",
                timestamp = System.currentTimeMillis(),
                status = FriendRequestStatus.Pending,
                direction = FriendRequestDirection.Incoming,
                peer = FriendUserSummary(userId = 1, username = "user1")
            )
        ),
        outgoingRequests = listOf(
            FriendRequestItem(
                requestId = 2,
                message = "Let's chat!",
                timestamp = System.currentTimeMillis(),
                status = FriendRequestStatus.Pending,
                direction = FriendRequestDirection.Outgoing,
                peer = FriendUserSummary(userId = 3, username = "user3")
            )
        ),
        friends = listOf(
            FriendUserSummary(userId = 2, username = "user2")
        ),
        logs = listOf("[12:00:00] ✅ Connected", "[12:00:02] ⬇️ FRIEND_REQUEST_NEW")
    )
    FriendScreenContent(
        navController = rememberNavController(),
        state = sampleState,
        receiverIdInput = "2",
        messageInput = "Let's go!",
        onReceiverIdChange = {},
        onMessageChange = {},
        onSendRequest = {},
        onAccept = { _ -> },
        onReject = { _ -> },
        onResend = { _ -> },
        onRemoveFriend = { _ -> },
        onToggleConnection = {},
        onWsUrlChange = {},
        onReconnect = {},
        onBannerDismiss = {}
    )
}