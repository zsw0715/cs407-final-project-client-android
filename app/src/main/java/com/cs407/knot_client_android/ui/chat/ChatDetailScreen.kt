package com.cs407.knot_client_android.ui.chat

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.cs407.knot_client_android.data.repository.UserRepository
import com.cs407.knot_client_android.navigation.Screen
import com.cs407.knot_client_android.ui.components.FloatingActionButton
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ChatDetailScreen(
    navController: NavHostController,
    state: ChatDetailUiState,
    onDraftChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSendImage: (String) -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8F6F4), Color(0xFFF3F0FA))
                )
            )
    ) {
        CenterAlignedTopAppBar(
            title = {
                // 标题左侧增加头像：如果未来有 avatarUrl 可以替换为网络图片；
                // 目前用用户名首字母作为占位，样式参考 ChatScreen 的会话头像
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val initial = state.title.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            fontSize = 18.sp,
                            color = Color(0xFF6B7280)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = state.title, fontSize = 20.sp)
                }
            },
            navigationIcon = {
                TopBarFloatingIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = {
                        navController.navigate(Screen.Main.createRoute("CHAT")) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            actions = {
                TopBarFloatingIconButton(
                    icon = Icons.Filled.Edit,
                    contentDescription = "Edit",
                    onClick = onEditClick,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )

        // HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Color(0x11000000))

        val listState = rememberLazyListState()

        // 当前用户昵称（用于右侧消息头像），优先从 TokenStore 取；拿不到就用 "Me"
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val selfName = remember {
            com.cs407.knot_client_android.data.local.TokenStore(context).getUsername()
                ?: "Me"
        }
        // 单聊场景下，标题就是对方用户名
        val otherName = state.title

        // 头像 URL（自己 & 对方），从用户相关接口中获取
        var selfAvatarUrl by remember { mutableStateOf<String?>(null) }
        var otherAvatarUrl by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            val repo = UserRepository(context.applicationContext, baseUrl = "http://10.0.2.2:8080")
            try {
                // 自己的头像
                val settings = repo.getUserSettings()
                selfAvatarUrl = settings?.avatarUrl
            } catch (_: Exception) {
                // 忽略错误，保持占位头像
            }
            try {
                // 对方头像：按用户名查
                val info = repo.getUserInfoByUsername(otherName)
                otherAvatarUrl = info.avatarUrl
            } catch (_: Exception) {
                // 忽略错误，保持占位头像
            }
        }

        val timeFormatter = remember {
            DateTimeFormatter.ofPattern("HH:mm")
        }

        var isAttachmentPanelOpen by remember { mutableStateOf(false) }
        // 底部附件面板下滑跟随偏移（像素）
        var sheetDragOffset by remember { mutableStateOf(0f) }

        // 照片选择 & 上传：从系统相册选图 -> 上传到 S3 -> 发送图片消息
        val imageUploadRepository = remember {
            UserRepository(context.applicationContext, baseUrl = "http://10.0.2.2:8080")
        }
        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    try {
                        val resolver = context.contentResolver
                        val type = resolver.getType(uri) ?: "image/jpeg"
                        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                        if (bytes == null) return@launch

                        val uploadedUrl = imageUploadRepository.uploadAvatarToS3(bytes, type)
                        onSendImage(uploadedUrl)
                    } catch (_: Exception) {
                        // 失败时先静默处理，避免打断聊天体验
                    }
                }
            }
        }

        // 每次重新打开附件面板时，重置偏移，避免沿用上次关闭时的位置
        LaunchedEffect(isAttachmentPanelOpen) {
            if (isAttachmentPanelOpen) {
                sheetDragOffset = 0f
            }
        }

        LaunchedEffect(state.messages.size) {
            if (state.messages.isNotEmpty()) {
                listState.animateScrollToItem(state.messages.lastIndex)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.messages.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No messages yet", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                    //reverseLayout = true   // 最近的在底部
                ) {
                    itemsIndexed(
                        items = state.messages,
                        key = { index, msg -> msg.clientMsgId ?: "server-${msg.msgId}-$index" }
                    ) { index, msg ->
                        val previous = state.messages.getOrNull(index - 1)
                        val showCenterTime = previous == null ||
                                Duration.between(previous.time, msg.time).toMinutes() >= 5

                        if (showCenterTime) {
                            CenterTimeLabel(msg.time, timeFormatter)
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        MessageBubble(
                            msg = msg,
                            selfName = selfName,
                            otherName = otherName,
                            selfAvatarUrl = selfAvatarUrl,
                            otherAvatarUrl = otherAvatarUrl
                        )
                    }
                }
            }
        }

        // 底部：输入栏 <-> 附件面板 动效切换（淡入淡出，避免拖拽关闭时闪烁）
        AnimatedContent(
            targetState = isAttachmentPanelOpen,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) with
                    fadeOut(animationSpec = tween(150))
            },
            label = "bottomInputAttachmentPanel"
        ) { expanded ->
            if (!expanded) {
                // 底部输入栏：单一大圆角容器，内部左侧 + 按钮 / 中间输入框 / 右侧发送按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 42.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 左侧 + 按钮（展开附件面板）
                            IconButton(
                                onClick = { isAttachmentPanelOpen = true },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color(0xFF6B7280)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add attachment"
                                )
                            }

                            // 竖线分隔
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(Color(0xFFE5E7EB))
                            )

                            // 文本输入区域（无边框，融入容器）
                            TextField(
                                value = state.draft,
                                onValueChange = onDraftChange,
                                placeholder = { Text("Say Something?") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(0.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = Color(0xFF4A6CF7)
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // 右侧：圆形发送按钮，嵌在同一容器内
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4A6CF7),
                                shadowElevation = 0.dp
                            ) {
                                IconButton(
                                    onClick = { onSendMessage(state.draft) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color(0xFFFFFFFF),
                                        modifier = Modifier
                                            .rotate(-40f)        // 再多一点倾斜
                                            .offset(x = 2.dp, y = (-1).dp) // 向右上轻微偏移，视觉更平衡
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // 展开的附件容器：高度为屏幕 40%，距离屏幕边缘 8dp
                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val panelHeight = screenHeight * 0.4f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(panelHeight)
                        .padding(8.dp)
                        .graphicsLayer {
                            translationY = sheetDragOffset
                        }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(48.dp),
                        color = Color(0xFFF8F6F4),
                        shadowElevation = 20.dp,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        )
                        {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(isAttachmentPanelOpen) {
                                    if (isAttachmentPanelOpen) {
                                        detectVerticalDragGestures(
                                            onDragEnd = {
                                                // 向下拖动距离较大时收起面板，否则回弹
                                                if (sheetDragOffset > 120f) {
                                                    // 不立即重置 offset，保持在手指松开的视觉位置
                                                    isAttachmentPanelOpen = false
                                                } else {
                                                    // 回弹到原位
                                                    sheetDragOffset = 0f
                                                }
                                            },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                // 只关心向下拖动，让面板跟随手指
                                                if (dragAmount > 0f) {
                                                    sheetDragOffset =
                                                        (sheetDragOffset + dragAmount)
                                                            .coerceAtLeast(0f)
                                                } else if (dragAmount < 0f && sheetDragOffset > 0f) {
                                                    // 轻微向上拖动时，允许减小偏移，避免卡在中间
                                                    sheetDragOffset =
                                                        (sheetDragOffset + dragAmount)
                                                            .coerceAtLeast(0f)
                                                }
                                            }
                                        )
                                    }
                                }
                                .padding(24.dp) // inner padding 再大一些
                        ) {
                            // 附件 Grid，目前先只有一个：Photos（图片）
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 15.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                photoPickerLauncher.launch("image/*")
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(28.dp))
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(
                                                            Color(0xFF636EF1),
                                                            Color(0xFF9B8FD9)
                                                        )
                                                    )
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = Color(0xFFEEF2FF),
                                                    shape = RoundedCornerShape(18.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Image,
                                                contentDescription = "Photos",
                                                tint = Color.White,
                                                modifier = Modifier.size(50.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Photos",
                                            fontSize = 14.sp,
                                            color = Color(0xFF4B5563)
                                        )
                                    }
                                }
                            }

                            // 右上角关闭按钮，采用 FloatingActionButton 双层毛玻璃 + 弹性动效
                            FloatingActionButton(
                                icon = Icons.Filled.Close,
                                onClick = { isAttachmentPanelOpen = false },
                                modifier = Modifier.align(Alignment.TopEnd),
                                containerSize = 52.dp,
                                iconSize = 22.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    msg: MessageUi,
    selfName: String,
    otherName: String,
    selfAvatarUrl: String?,
    otherAvatarUrl: String?
) {
    val displayName = if (msg.isMine) selfName else otherName
    val avatarUrl = if (msg.isMine) selfAvatarUrl else otherAvatarUrl

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!msg.isMine) {
            MessageAvatar(displayName, avatarUrl)
            Spacer(modifier = Modifier.width(10.dp))
        }

        if (msg.msgType == 1 && msg.mediaUrl != null) {
            // 图片消息气泡
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Transparent)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = msg.mediaUrl),
                    contentDescription = "Image message",
                    modifier = Modifier
                        .width(220.dp)
                        .heightIn(min = 140.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            // 文本消息气泡
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp)) // 四周圆角，更现代的 pill 形状
                    .background(
                        if (msg.isMine) Color(0xFF636EF1)   // 品牌浅紫蓝
                        else Color.White.copy(alpha = 0.96f)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = msg.contentText,
                    color = if (msg.isMine) Color.White else Color(0xFF111827),
                    fontSize = 15.sp,                     // 稍微放大一点
                    lineHeight = 20.sp
                )
            }
        }

        if (msg.isMine) {
            Spacer(modifier = Modifier.width(10.dp))
            MessageAvatar(displayName, avatarUrl)
        }
    }
}

@Composable
private fun MessageAvatar(name: String, avatarUrl: String?) {
    if (!avatarUrl.isNullOrBlank()) {
        Image(
            painter = rememberAsyncImagePainter(model = avatarUrl),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        Box(
            modifier = Modifier
                .size(40.dp) // 头像更大一些
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun CenterTimeLabel(time: LocalDateTime, formatter: DateTimeFormatter) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time.format(formatter),
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier
                .background(
                    color = Color.White.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TopBarFloatingIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Apple-style 双阶段弹性动画，尺寸比底部 FAB 略小
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(
                targetValue = 1.15f,
                animationSpec = tween(
                    durationMillis = 170,
                    easing = LinearOutSlowInEasing
                )
            )
        } else {
            scale.animateTo(
                targetValue = 0.9f,
                animationSpec = tween(
                    durationMillis = 155,
                    easing = FastOutLinearInEasing
                )
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

    Box(modifier = modifier) {
        // 背景毛玻璃层
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .graphicsLayer {
                    renderEffect = RenderEffect
                        .createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                }
                .background(Color.White.copy(alpha = 0.65f))
        )

        // 主按钮层
        Box(
            modifier = Modifier
                .size(42.dp)
                .scale(scale.value)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE5E7EB).copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    )
                )
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = interactionSource
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isPressed) Color(0xFF636EF1) else Color(0xFF6B7280)
            )
        }
    }
}
