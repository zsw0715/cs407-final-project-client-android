package com.cs407.knot_client_android.ui.chat

import android.R.attr.onClick
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.cs407.knot_client_android.data.api.RetrofitProvider
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.navigation.Screen
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch


@Composable
fun ChatScreen(
    navController: NavHostController,
    state: ChatUiState,
    bottomPaddingForFab: Dp = 84.dp,
    onOpenConversation: (ConversationUi) -> Unit = {},
    onCreateGroupConversation: () -> Unit = {}
) {
    // 只保留搜索内容本身，不再有 isSearching 模式
    var query by remember { mutableStateOf("") }

    // 控制 Dialog 显示
    var showCreateDialog by remember { mutableStateOf(false) }

    var friendList by remember {mutableStateOf<List<SelectableFriendUi>>(emptyList())}
    var friendListLoaded by remember { mutableStateOf(false) }

    // 上下文、TokenStore、ConversationApi
    val context = LocalContext.current
    val appContext = context.applicationContext
    val tokenStore = remember { TokenStore(appContext) }

    // Production server address
    val baseUrl = "http://3.144.236.205:8080"
    val friendApi = remember { RetrofitProvider.createFriendService(baseUrl) }
    val conversationApi = remember { RetrofitProvider.createConversationService(baseUrl) }

    val scope = rememberCoroutineScope()

    val conversations = state.conversations
    val filteredList = remember(conversations, query) {
        if (query.isBlank()) {
            conversations
        } else {
            conversations.filter {
                it.displayTitle.contains(query, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val token = tokenStore.getAccessToken()
            if (token.isNullOrBlank()) return@LaunchedEffect

            val resp = friendApi.getFriendList("Bearer $token")

            if (resp.success && !resp.data.isNullOrEmpty()) {
                friendList = resp.data.map { dto ->
                    SelectableFriendUi(
                        id = dto.friendId,
                        name = dto.username,
                        avatarUrl = dto.avatar
                    )
                }
            } else {
                // TODO: 可以在这里做一些错误提示（比如用 Snackbar）
            }
        } catch (e: Exception) {
            // TODO: 网络异常时的处理
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFF8F6F4), Color(0xFFF3F0FA))
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Spacer(Modifier.height(30.dp))

        // 顶部标题栏
        TransparentHeaderBar(
            onAddGroup = { showCreateDialog = true }
        )

        Spacer(Modifier.height(8.dp))

        // 搜索单独一栏放在列表前面
        ConversationSearchBar(
            query = query,
            onQueryChange = { query = it }
        )

        Spacer(Modifier.height(8.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Error", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            filteredList.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (query.isNotBlank()) "No results"
                    else "This is a Chat page",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = bottomPaddingForFab)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        ConversationCard(item, onOpenConversation)
                    }
                }
            }
        }
//        if (showCreateDialog) {
//            CreateGroupDialog(
//                friendsRaw = friendList,   // 把从后端拿到的好友列表塞进去
//                onDismiss = { showCreateDialog = false },
//                onConfirm = { groupName, memberIds ->
//                    scope.launch {
//                        try {
//                            val token = tokenStore.getAccessToken()
//                            if (token.isNullOrBlank()) {
//                                showCreateDialog = false
//                                return@launch
//                            }
//
//                            val authHeader = "Bearer $token"
//                            val memberIdStr = memberIds.joinToString(",")
//
//                            // 这里调用你已经写好的 createGroupConversation
//                            val resp = conversationApi.createGroupConversation(
//                                authorization = authHeader,
//                                groupName = groupName,
//                                memberIds = memberIdStr
//                            )
//
//                            if (resp.success && resp.data != null) {
//                                showCreateDialog = false
//                                navController.navigate(
//                                    Screen.ChatDetail.createRoute(
//                                        convId = resp.data.id,
//                                        title = groupName
//                                    )
//                                )
//                            } else {
//                                // TODO: 失败提示
//                            }
//                        } catch (e: Exception) {
//                            // TODO: 网络错误提示
//                        }
//                    }
//                }
//            )
//        }

//        if (showCreateDialog) {
//            CreateGroupDialog(
//                onDismiss = { showCreateDialog = false },
//                onConfirm = { groupName, memberIds ->
//                    scope.launch {
//                        try {
//                            val token = tokenStore.getAccessToken()
//                            if (token.isNullOrBlank()) {
//                                // 简单处理：没有 token 直接关闭，对实际项目可以 Toast 提示
//                                showCreateDialog = false
//                                return@launch
//                            }
//
//                            val authHeader = "Bearer $token"
//                            val memberIdStr = memberIds.joinToString(",")
//
//                            val resp = conversationApi.createGroupConversation(
//                                authorization = authHeader,
//                                groupName = groupName,
//                                memberIds = memberIdStr
//                            )
//
//                            if (resp.success && resp.data != null) {
//                                showCreateDialog = false
//                                // 直接跳转到新群聊的 ChatDetail
//                                navController.navigate(
//                                    Screen.ChatDetail.createRoute(
//                                        convId = resp.data.id,
//                                        title = groupName
//                                    )
//                                )
//                            } else {
//                                // 可以在这里加 Toast / Snackbar 提示错误
//                            }
//                        } catch (e: Exception) {
//                            // 网络错误，视情况提示
//                        }
//                    }
//                }
//            )
//        }
    }
    if (showCreateDialog) {
        CreateGroupDialog(
            friendsRaw = friendList,           // 即使为空，Dialog 也能正常显示
            onDismiss = { showCreateDialog = false },
            onConfirm = { groupName, memberIds ->
                scope.launch {
                    try {
                        val token = tokenStore.getAccessToken()
                        if (token.isNullOrBlank()) {
                            showCreateDialog = false
                            return@launch
                        }

                        val authHeader = "Bearer $token"
                        val memberIdStr = memberIds.joinToString(",")

                        val resp = conversationApi.createGroupConversation(
                            authorization = authHeader,
                            groupName = groupName,
                            memberIds = memberIdStr
                        )

                        if (resp.success && resp.data != null) {
                            showCreateDialog = false
                            // 直接跳到新群聊
                            navController.navigate(
                                Screen.ChatDetail.createRoute(
                                    convId = resp.data.id,
                                    title = groupName
                                )
                            )
                        } else {
                            // TODO: 可以这里弹 Toast / Snackbar 提示错误
                        }
                    } catch (e: Exception) {
                        // TODO: 网络错误提示
                    }
                }
            }
        )
    }
}

/* 顶部栏：普通模式 / 搜索模式 */
@Composable
private fun TransparentHeaderBar(
    onAddGroup: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var menuExpanded by remember { mutableStateOf(false) }

        // 按钮动画状态（沿用你原来的）
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale = remember { Animatable(1f) }

        LaunchedEffect(isPressed) {
            if (isPressed) {
                scale.animateTo(
                    targetValue = 1.2f,
                    animationSpec = tween(durationMillis = 170, easing = LinearOutSlowInEasing)
                )
            } else {
                scale.animateTo(
                    targetValue = 0.88f,
                    animationSpec = tween(durationMillis = 155, easing = FastOutLinearInEasing)
                )
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }

        Box(contentAlignment = Alignment.Center) {
            // 毛玻璃背景层
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(50))
                    .graphicsLayer {
                        renderEffect = RenderEffect
                            .createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
                            .asComposeRenderEffect()
                    }
                    .background(Color.White.copy(alpha = 0.65f))
            )

            // 主按钮：图标改成 +
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .scale(scale.value)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { menuExpanded = true }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Menu",
                    tint = if (isPressed) Color(0xFF636EF1) else Color(0xFF666666),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 现在菜单里只有“Add group conversation”
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        menuExpanded = false
                        onAddGroup()
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Add group conversation")
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "Knot Chat",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Cursive
        )
    }
}

@Composable
private fun ConversationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "search",
                fontSize = 16.sp,
                color = Color(0xFF888888)
            )
        },
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 16.sp
        ),
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF666666),
                modifier = Modifier.size(20.dp)
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,

            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            cursorColor = Color(0xFF666666)
        )
    )
}

/* 单条会话卡片：头像 + 名称 + 最后一条消息 + 时间 */
@Composable
private fun ConversationCard(
    item: ConversationUi,
    onOpenConversation: (ConversationUi) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(12.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                // onClick = { onOpenConversation(item) }
            ) {
                onOpenConversation(item)
            }  // 之后需要跳转时再打开
    ) {
        val avatarUrl = item.avatarUrl
        if (avatarUrl != null) {
            // 有头像地址：正常加载网络头像
            Image(
                painter = rememberAsyncImagePainter(model = avatarUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // 无头像：用名字首字母生成圆形头像（仿照 FriendSelectionItem）
            val initial = item.displayTitle.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.displayTitle,   // 这里现在当成用户名来搜索
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatTimeShort(item.lastMsgTimeIso),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.lastMsgPreview.orEmpty(),
                fontSize = 14.sp,
                maxLines = 1
            )
        }
    }
}

/* 时间格式化：支持 "2025-11-09T21:15:21" 以及将来可能带时区的格式 */
private fun formatTimeShort(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        // 当前后端是 "yyyy-MM-dd'T'HH:mm:ss"
        val ldt = LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        ldt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: DateTimeParseException) {
        try {
            val odt = OffsetDateTime.parse(iso)
            odt.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (_: Exception) {
            ""
        }
    }
}
