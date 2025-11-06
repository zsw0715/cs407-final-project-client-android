package com.cs407.knot.ui.features.usersettings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cs407.knot.data.model.Friend
import com.cs407.knot.data.model.FriendRequest
import com.cs407.knot.ui.components.*


@Composable
fun UserSettingsScreen(
    vm: UserSettingsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by vm.uiState.collectAsState()


    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopBar(title = "User", left = {
                FilledTonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) }
            })
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AsyncImage(model = state.avatar, contentDescription = null, modifier = Modifier.size(108.dp))
                    }
                }
                item { SectionHeader("Settings") }
                item { SettingsCard(onUser = { /* TODO */ }, onAppearance = { /* TODO */ }, onLogout = { /* TODO */ }) }
                item { SectionHeader("Friends Request") }
                item { FriendRequestList(state.requests, vm::accept, vm::decline) }
                item { SectionHeader("Friends List") }
                item { FriendList(state.friends, vm::remove) }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
//        BottomBar(
//            items = listOf(
//                BottomItem(Icons.Rounded.Map, "Map"),
//                BottomItem(Icons.AutoMirrored.Rounded.Chat, "Chat"),
//                BottomItem(Icons.Rounded.AccountCircle, "Me", selected = true),
//                BottomItem(Icons.Rounded.AddCircle, "Add")
//            ),
//            modifier = Modifier.align(Alignment.BottomCenter)
//        )
    }
}


@Composable
private fun SettingsCard(onUser: () -> Unit, onAppearance: () -> Unit, onLogout: () -> Unit) {
    CardContainer(tonal = true) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Settings, null); Spacer(Modifier.width(12.dp)); Text("User Settings", Modifier.weight(1f)); Icon(Icons.Rounded.ArrowForward, null)
        }
        HorizontalDivider(Modifier.padding(vertical = 6.dp))
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.LightMode, null); Spacer(Modifier.width(12.dp)); Text("Appearance", Modifier.weight(1f)); Icon(Icons.Rounded.ArrowForward, null)
        }
        HorizontalDivider(Modifier.padding(vertical = 6.dp))
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null); Spacer(Modifier.width(12.dp)); Text("Logout", Modifier.weight(1f)); Icon(Icons.Rounded.ArrowForward, null)
        }
    }
}


@Composable
private fun FriendRequestList(requests: List<FriendRequest>, onAccept: (FriendRequest) -> Unit, onDecline: (FriendRequest) -> Unit) {
    if (requests.isEmpty()) { EmptyHint("No new friend requests"); return }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        requests.forEach { req ->
            CardContainer(padding = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(req.fromAvatar, null, modifier = Modifier.size(60.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("Friend Request", style = MaterialTheme.typography.titleMedium); Text("\"${req.preview}\"") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { onAccept(req) }) { Icon(Icons.Rounded.Handshake, null) }
                        OutlinedButton(onClick = { onDecline(req) }) { Icon(Icons.Rounded.Close, null) }
                    }
                }
            }
        }
    }
}


@Composable
private fun FriendList(friends: List<Friend>, onRemove: (Friend) -> Unit) {
    if (friends.isEmpty()) { EmptyHint("No friends yet"); return }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        friends.forEach { f ->
            CardContainer(padding = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(f.avatarUrl, null, modifier = Modifier.size(60.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(f.name, Modifier.weight(1f))
                    FilledIconButton(onClick = { onRemove(f) }) { Icon(Icons.Rounded.Close, null) }
                }
            }
        }
    }
}