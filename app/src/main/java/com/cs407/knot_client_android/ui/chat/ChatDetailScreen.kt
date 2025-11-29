package com.cs407.knot_client_android.ui.chat

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cs407.knot_client_android.navigation.Screen
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavHostController,
    state: ChatDetailUiState,
    onDraftChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
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
        val context = androidx.compose.ui.platform.LocalContext.current
        val selfName = remember {
            com.cs407.knot_client_android.data.local.TokenStore(context).getUsername()
                ?: "Me"
        }
        // 单聊场景下，标题就是对方用户名
        val otherName = state.title

        val timeFormatter = remember {
            DateTimeFormatter.ofPattern("HH:mm")
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
                            otherName = otherName
                        )
                    }
                }
            }
        }

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
                    // 左侧 + 按钮（未来可扩展为图片/附件）
                    IconButton(
                        onClick = { /* TODO: attach file */ },
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
    }
}

@Composable
private fun MessageBubble(
    msg: MessageUi,
    selfName: String,
    otherName: String
) {
    val displayName = if (msg.isMine) selfName else otherName

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!msg.isMine) {
            MessageAvatar(displayName)
            Spacer(modifier = Modifier.width(10.dp))
        }

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

        if (msg.isMine) {
            Spacer(modifier = Modifier.width(10.dp))
            MessageAvatar(displayName)
        }
    }
}

@Composable
private fun MessageAvatar(name: String) {
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
