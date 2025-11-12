package com.cs407.knot_client_android.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // 柔和背景渐变
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFF8F6F4), Color(0xFFF3F0FA))
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        // 顶部透明栏
        TransparentHeaderBar()

        Spacer(Modifier.height(8.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Error", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            state.conversations.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "This is a Chat page",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = bottomPaddingForFab)
                ) {
                    items(state.conversations, key = { it.id }) { item ->
                        ConversationCard(item, onOpenConversation)
                    }
                }
            }
        }
    }
}

/* 透明顶部栏 */
@Composable
private fun TransparentHeaderBar() {
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
                .border(width = 1.5.dp, color = Color(0xFFBDBDBD), shape = RoundedCornerShape(50)),
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
}

/* 单条会话卡片：头像 + 名称 + 最后一条消息 + 时间 */
@Composable
private fun ConversationCard(
    item: ConversationUi,
    onClick: (ConversationUi) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(12.dp)
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
                    text = item.displayTitle,
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

/* 时间格式化函数（ISO → HH:mm）*/
private fun formatTimeShort(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val odt = OffsetDateTime.parse(iso)
        odt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: DateTimeParseException) {
        ""
    }
}
