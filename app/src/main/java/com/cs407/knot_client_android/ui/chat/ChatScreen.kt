package com.cs407.knot_client_android.ui.chat

import android.R.attr.onClick
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
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

@Composable
fun ChatScreen(
    navController: NavHostController,
    state: ChatUiState,
    bottomPaddingForFab: Dp = 84.dp,
    onOpenConversation: (ConversationUi) -> Unit = {}
) {
    // 搜索相关的本地 UI 状态
    var isSearching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val conversations = state.conversations
    val filteredList = remember(conversations, isSearching, query) {
        if (!isSearching || query.isBlank()) {
            conversations
        } else {
            conversations.filter {
                it.displayTitle.contains(query, ignoreCase = true)
            }
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
        Spacer(Modifier.height(16.dp))
        // 顶部：普通模式 / 搜索模式 切换
        TransparentHeaderBar(
            isSearching = isSearching,
            query = query,
            onQueryChange = { query = it },
            onStartSearch = { isSearching = true },
            onCloseSearch = {
                isSearching = false
                query = ""
            }
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
                    text = if (isSearching && query.isNotBlank()) "No results"
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
    }
}

/* 顶部栏：普通模式 / 搜索模式 */
@Composable
private fun TransparentHeaderBar(
    isSearching: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onStartSearch: () -> Unit,
    onCloseSearch: () -> Unit
) {
    if (!isSearching) {
        // 普通模式：左圆形搜索按钮 + 右 Knot Chat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.6f))
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFFBDBDBD),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onStartSearch() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "Knot Chat",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Cursive
            )
        }
    } else {
        // 搜索模式：左关闭按钮 + 搜索文本框
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseSearch) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close search"
                )
            }

            Spacer(Modifier.width(4.dp))

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.95f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                    disabledContainerColor = Color.White.copy(alpha = 0.9f),

                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,

                    cursorColor = Color(0xFF666666)
                )
            )

        }
    }
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
            .clickable { onOpenConversation(item) }  // 之后需要跳转时再打开
    ) {
        val avatar = item.avatarUrl ?: "https://picsum.photos/seed/${item.id}/96/96"
        Image(
            painter = rememberAsyncImagePainter(model = avatar),
            contentDescription = null,
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )

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
