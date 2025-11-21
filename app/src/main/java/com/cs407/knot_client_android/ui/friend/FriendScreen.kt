package com.cs407.knot_client_android.ui.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cs407.knot_client_android.navigation.Screen
import com.cs407.knot_client_android.ui.components.FloatingActionButton
import com.cs407.knot_client_android.ui.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendScreen(
    navController: NavHostController,
    viewModel: FriendViewModel = viewModel()
) {
    val mainBackStackEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(Screen.Main.route)
    }
    val mainViewModel: MainViewModel = viewModel(mainBackStackEntry)

    LaunchedEffect(mainViewModel) {
        viewModel.attachMainViewModel(mainViewModel)
    }
    val uiState by viewModel.uiState.collectAsState()

    var usernameInput by rememberSaveable { mutableStateOf("") }
    var messageInput by rememberSaveable { mutableStateOf("Hi! Let's be friends!") }
    var friendSearchQuery by rememberSaveable { mutableStateOf("") }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showIncomingSheet by rememberSaveable { mutableStateOf(false) }

    FriendScreenContent(
        navController = navController,
        state = uiState,
        usernameInput = usernameInput,
        messageInput = messageInput,
        friendSearchQuery = friendSearchQuery,
        isAddSheetOpen = showAddSheet,
        isIncomingSheetOpen = showIncomingSheet,
        onUsernameChange = { usernameInput = it },
        onMessageChange = { messageInput = it },
        onFriendSearchChange = { friendSearchQuery = it },
        onSearchUser = { viewModel.searchUserByUsername(usernameInput) },
        onSendRequest = { user -> viewModel.sendFriendRequest(user.userId, messageInput) },
        onAccept = viewModel::acceptRequest,
        onReject = viewModel::rejectRequest,
        onOpenAddSheet = { showAddSheet = true },
        onCloseAddSheet = { showAddSheet = false },
        onOpenIncomingSheet = { showIncomingSheet = true },
        onCloseIncomingSheet = { showIncomingSheet = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendScreenContent(
    navController: NavHostController,
    state: FriendUiState,
    usernameInput: String,
    messageInput: String,
    friendSearchQuery: String,
    isAddSheetOpen: Boolean,
    isIncomingSheetOpen: Boolean,
    onUsernameChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onFriendSearchChange: (String) -> Unit,
    onSearchUser: () -> Unit,
    onSendRequest: (FriendUserSummary) -> Unit,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onOpenAddSheet: () -> Unit,
    onCloseAddSheet: () -> Unit,
    onOpenIncomingSheet: () -> Unit,
    onCloseIncomingSheet: () -> Unit
) {
    val backgroundBrush = Brush.verticalGradient(
        listOf(Color(0xFFF8F6F4), Color(0xFFF3F0FA))
    )
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val incomingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredFriends = remember(friendSearchQuery, state.friends) {
        if (friendSearchQuery.isBlank()) {
            state.friends
        } else {
            state.friends.filter {
                it.username.contains(friendSearchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Friends",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Manage connections & requests",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionBubble(
                        icon = Icons.Outlined.PersonAdd,
                        label = "Add",
                        onClick = onOpenAddSheet
                    )
                    ActionBubble(
                        icon = Icons.Outlined.Notifications,
                        label = "Requests",
                        showBadge = state.incomingRequests.isNotEmpty(),
                        onClick = onOpenIncomingSheet
                    )
                }
            }

            FriendSectionCard(
                title = "Friend list",
                subtitle = if (state.friends.isEmpty()) "Invite people to grow your circle" else "Swipe through your people"
            ) {
                OutlinedTextField(
                    value = friendSearchQuery,
                    onValueChange = onFriendSearchChange,
                    placeholder = { Text("Search friends...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.friends.isEmpty()) {
                    EmptyState()
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        filteredFriends.forEach { friend ->
                            FriendRow(friend)
                        }
                        if (filteredFriends.isEmpty()) {
                            Text(
                                text = "No matches found.",
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

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

    if (isAddSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = onCloseAddSheet,
            sheetState = addSheetState,
            containerColor = Color.White.copy(alpha = 0.96f)
        ) {
            AddFriendSheetContent(
                usernameInput = usernameInput,
                messageInput = messageInput,
                isSearching = state.isSearching,
                searchedUser = state.searchedUser,
                searchError = state.searchError,
                onUsernameChange = onUsernameChange,
                onMessageChange = onMessageChange,
                onSearchUser = onSearchUser,
                onSendRequest = onSendRequest,
                onClose = onCloseAddSheet
            )
        }
    }

    if (isIncomingSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = onCloseIncomingSheet,
            sheetState = incomingSheetState,
            containerColor = Color.White.copy(alpha = 0.96f)
        ) {
            IncomingRequestsSheet(
                requests = state.incomingRequests,
                onAccept = onAccept,
                onReject = onReject,
                onClose = onCloseIncomingSheet
            )
        }
    }
}

@Composable
private fun ActionBubble(
    icon: ImageVector,
    label: String,
    showBadge: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BadgedBox(
            badge = {
                if (showBadge) {
                    Badge(containerColor = Color(0xFFEF4444))
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFEEF2FF), Color(0xFFE0F2FE))
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE5E7EB),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF4B5563)
                )
            }
        }
        Text(text = label, fontSize = 12.sp, color = Color(0xFF6B7280))
    }
}

@Composable
private fun FriendRow(friend: FriendUserSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarBubble(friend.username)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = friend.username,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF111827)
            )
            val subtitle = friend.createdAtMs?.let { "Added ${formatTimestamp(it)}" } ?: "Friend"
            Text(
                text = subtitle,
                color = Color(0xFF6B7280),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AvatarBubble(name: String) {
    val initial = name.firstOrNull()?.uppercaseChar() ?: '?'
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF8BB9FF), Color(0xFF6366F1))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AddFriendSheetContent(
    usernameInput: String,
    messageInput: String,
    isSearching: Boolean,
    searchedUser: FriendUserSummary?,
    searchError: String?,
    onUsernameChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSearchUser: () -> Unit,
    onSendRequest: (FriendUserSummary) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SheetHeader(title = "Add Friend", onClose = onClose)

        Text(
            text = "Send a friendly note with your request.",
            color = Color(0xFF6B7280),
            fontSize = 13.sp
        )

        OutlinedTextField(
            value = usernameInput,
            onValueChange = onUsernameChange,
            label = { Text("Username") },
            placeholder = { Text("@username") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = messageInput,
            onValueChange = onMessageChange,
            label = { Text("Message (optional)") },
            placeholder = { Text("Hi! We met at...") },
            shape = RoundedCornerShape(16.dp),
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSearchUser,
            enabled = usernameInput.isNotBlank() && !isSearching,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (isSearching) "Searching..." else "Find user")
        }

        if (searchError != null) {
            Text(
                text = searchError,
                color = Color(0xFFDC2626),
                fontSize = 12.sp
            )
        }

        if (searchedUser != null) {
            val user = searchedUser
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FD)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AvatarBubble(user.username)
                        Column {
                            Text(
                                text = user.username,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "User ID: ${user.userId}",
                                color = Color(0xFF6B7280),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Button(
                        onClick = {
                            onSendRequest(user)
                            onClose()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Send request")
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomingRequestsSheet(
    requests: List<FriendRequestItem>,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SheetHeader(
            title = "Incoming Requests",
            badgeCount = requests.size,
            onClose = onClose
        )

        if (requests.isEmpty()) {
            Text(
                text = "No pending requests. You're all caught up!",
                color = Color(0xFF6B7280),
                fontSize = 13.sp
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                requests.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AvatarBubble(item.requesterName ?: "User")
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = item.requesterName ?: "User #${item.requesterId}",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = item.message,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color(0xFF4B5563)
                                    )
                                }
                            }
                            Text(
                                text = formatTimestamp(item.timestamp),
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onAccept(item.requestId) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Accept")
                                }
                                OutlinedButton(
                                    onClick = { onReject(item.requestId) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFEF4444)
                                    )
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
}

@Composable
private fun SheetHeader(
    title: String,
    badgeCount: Int? = null,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if ((badgeCount ?: 0) > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Badge(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                ) {
                    Text(text = badgeCount.toString())
                }
            }
        }
        IconButton(onClick = onClose) {
            Icon(Icons.Outlined.Close, contentDescription = "Close")
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
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                subtitle?.let {
                    Text(text = it, fontSize = 13.sp, color = Color(0xFF6B7280))
                }
            }
            content()
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "No friends yet.",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )
        Text(
            text = "Use the add button above to send a request.",
            color = Color(0xFF6B7280),
            fontSize = 13.sp
        )
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0) return "--"
    return try {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) {
        timestamp.toString()
    }
}
