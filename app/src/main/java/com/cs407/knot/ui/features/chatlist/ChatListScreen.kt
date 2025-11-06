package com.cs407.knot.ui.features.chatlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cs407.knot.data.model.ChatPreview
import com.cs407.knot.ui.components.*


@Composable
fun ChatListScreen(
    modifier: Modifier = Modifier,
    vm: ChatListViewModel = viewModel(),
    onOpenChat: (ChatPreview) -> Unit = {},
    onOpenMe: () -> Unit = {}
) {
    val state by vm.uiState.collectAsState()
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(title = "Knot Chat", left = {
                FilledTonalIconButton(onClick = { /* search */ }) { Icon(Icons.Rounded.Search, null) }
            })
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(state.chats, key = { it.id }) { chat -> ChatRow(chat) { onOpenChat(chat) } }
            }
        }
//        BottomBar(
//            items = listOf(
//                BottomItem(Icons.Rounded.Map, "Map"),
//                BottomItem(Icons.AutoMirrored.Rounded.Chat, "Chat", selected = true),
//                BottomItem(Icons.Rounded.AccountCircle, "Me"),
//                BottomItem(Icons.Rounded.AddCircle, "Add")
//            ),
//            modifier = Modifier.align(Alignment.BottomCenter)
//        )
    }
}


@Composable
private fun ChatRow(chat: ChatPreview, onClick: () -> Unit) {
    CardContainer(modifier = Modifier.fillMaxWidth().clickable{}, padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GroupAvatar(chat.avatarUrls)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(chat.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text(chat.time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                }
                Spacer(Modifier.height(4.dp))
                Text(chat.lastMessage, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(.75f))
            }
        }
    }
}
