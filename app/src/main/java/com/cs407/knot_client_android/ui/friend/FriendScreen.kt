package com.cs407.knot_client_android.ui.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.cs407.knot_client_android.ui.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FriendScreen(
    navController: NavHostController,
    viewModel: FriendViewModel = viewModel()
) {
    // 1. 拿到 MainScreen 对应的 backStackEntry
    val mainBackStackEntry = remember(navController) {
        navController.getBackStackEntry(Screen.Main.route)
    }

    // 2. 基于这个 entry 取出同一个 MainViewModel 实例
    val mainViewModel: MainViewModel = viewModel(mainBackStackEntry)

    // 3. 把这个 MainViewModel 交给 FriendViewModel，用于发 WebSocket
    LaunchedEffect(mainViewModel) {
        viewModel.attachMainViewModel(mainViewModel)
    }
    val uiState by viewModel.uiState.collectAsState()

    var usernameInput by rememberSaveable { mutableStateOf("") }
    var messageInput by rememberSaveable { mutableStateOf("Hi! Let's be friends!") }

    FriendScreenContent(
        navController = navController,
        state = uiState,
        usernameInput = usernameInput,
        messageInput = messageInput,
        onUsernameChange = { usernameInput = it },
        onMessageChange = { messageInput = it },
        onSearchUser = { viewModel.searchUserByUsername(usernameInput) },
        onSendRequest = { user -> viewModel.sendFriendRequest(user.userId, messageInput) },
        onAccept = viewModel::acceptRequest,
        onReject = viewModel::rejectRequest
    )
}

@Composable
private fun FriendScreenContent(
    navController: NavHostController,
    state: FriendUiState,
    usernameInput: String,
    messageInput: String,
    onUsernameChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSearchUser: () -> Unit,
    onSendRequest: (FriendUserSummary) -> Unit,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit
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
            // 标题
            item {
                Text(
                    text = "Friend Management System",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 发送好友申请区域
            item {
                SendFriendRequestCard(
                    usernameInput = usernameInput,
                    messageInput = messageInput,
                    onUsernameChange = onUsernameChange,
                    onMessageChange = onMessageChange,
                    onSearchUser = onSearchUser,
                    searchedUser = state.searchedUser,
                    searchError = state.searchError,
                    onSendRequest = onSendRequest
                )
            }

            // 收到的好友申请列表
            item {
                IncomingRequestListCard(
                    requests = state.incomingRequests,
                    onAccept = onAccept,
                    onReject = onReject
                )
            }

            // 好友列表（先留空）
            item {
                FriendSectionCard(
                    title = "Friend list",
                    subtitle = null
                ) {
                    Text(
                        text = "Coming soon...",
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // 右下角浮动按钮：返回聊天页面
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
private fun SendFriendRequestCard(
    usernameInput: String,
    messageInput: String,
    onUsernameChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSearchUser: () -> Unit,
    searchedUser: FriendUserSummary?,
    searchError: String?,
    onSendRequest: (FriendUserSummary) -> Unit
) {
    FriendSectionCard(
        title = "Send friend request",
        subtitle = null
    ) {
        OutlinedTextField(
            value = usernameInput,
            onValueChange = onUsernameChange,
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
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
            onClick = onSearchUser,
            enabled = usernameInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search user")
        }

        if (searchError != null) {
            Text(
                text = searchError,
                color = Color(0xFFDC2626),
                fontSize = 12.sp
            )
        }

        if (searchedUser != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Found user",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Username: ${searchedUser.username}",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "User ID: ${searchedUser.userId}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onSendRequest(searchedUser) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send friend request")
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomingRequestListCard(
    requests: List<FriendRequestItem>,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    FriendSectionCard(
        title = "Incoming requests",
        subtitle = null
    ) {
        if (requests.isEmpty()) {
            Text(
                text = "No pending friend requests.",
                color = Color(0xFF6B7280),
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                requests.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.White.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 申请人（暂时只显示用户 ID，后面需要的话可以再按 ID 查用户名）
                        Text(
                            text = item.requesterName ?: "User #${item.requesterId}",
                            fontWeight = FontWeight.Medium
                        )

                        // 信息
                        Text(
                            text = item.message,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // 时间
                        Text(
                            text = "Time: ${formatTimestamp(item.timestamp)}",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )

                        // 按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onAccept(item.requestId) },
                                modifier = Modifier.weight(1f)
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
                }
            }
        }
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
