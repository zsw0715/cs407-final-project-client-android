package com.cs407.knot_client_android.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cs407.knot_client_android.ui.components.FloatingActionButton
import coil.compose.rememberAsyncImagePainter


// 单个可选好友的 UI 数据
data class SelectableFriendUi(
    val id: Long,
    val name: String,
    val avatarUrl: String? = null,
    val selected: Boolean = false
)

@Composable
fun CreateGroupDialog(
    friendsRaw: List<SelectableFriendUi>,                 // 👈 从外部传进来
    onDismiss: () -> Unit,
    onConfirm: (groupName: String, memberIds: List<Long>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }

    // 以外部传进来的列表为初始值，本地维护一个可选中状态的副本
    var friends by remember(friendsRaw) { mutableStateOf(friendsRaw) }

    fun toggleFriend(id: Long) {
        friends = friends.map { f ->
            if (f.id == id) f.copy(selected = !f.selected) else f
        }
    }

    val canCreate = groupName.isNotBlank() && friends.any { it.selected }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(48.dp),
            color = Color(0xFFF8F6F4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 顶部标题 + 关闭按钮，风格模仿 LocationPickerDialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Group",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1B1F)
                    )

                    // 使用浮动按钮样式的关闭按钮（毛玻璃 + 弹性缩放），小一号
                    FloatingActionButton(
                        icon = Icons.Default.Close,
                        onClick = onDismiss,
                        modifier = Modifier,
                        containerSize = 42.dp,
                        iconSize = 20.dp
                    )
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    singleLine = true,
                    placeholder = { Text("Group name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF636EF1),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                Text("Select members", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                    ) {
                        items(friends) { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.95f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { toggleFriend(friend.id) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 头像：有 avatarUrl 用网络图片，否则用首字母占位（仿 ChatDetailScreen）
                                if (!friend.avatarUrl.isNullOrBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = friend.avatarUrl),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    val initial = friend.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
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

                                Spacer(Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = friend.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Checkbox(
                                    checked = friend.selected,
                                    onCheckedChange = { toggleFriend(friend.id) }
                                )
                            }

                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val ids = friends.filter { it.selected }.map { it.id }
                        onConfirm(groupName.trim(), ids)
                    },
                    enabled = canCreate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF636EF1),
                        disabledContainerColor = Color(0xFF9B9B9B).copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "Create group",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

            }
        }
    }
}


//@Composable
//fun CreateGroupDialog(
//    onDismiss: () -> Unit,
//    onConfirm: (groupName: String, memberIds: List<Long>) -> Unit
//) {
//    var groupName by remember { mutableStateOf("") }
//
//    // TODO：这里用的是临时写死的好友列表，方便你先跑通流程；
//    // 之后你可以从 FriendApi 真实拉取数据填进来。
//    val friends = remember {
//        mutableStateListOf(
//            SelectableFriendUi(id = 6L, name = "Friend 6"),
//            SelectableFriendUi(id = 8L, name = "Friend 8"),
//            // 可以继续加测试数据
//        )
//    }
//
//    fun toggleFriend(id: Long) {
//        val index = friends.indexOfFirst { it.id == id }
//        if (index >= 0) {
//            friends[index] = friends[index].copy(selected = !friends[index].selected)
//        }
//    }
//
//    val canCreate = groupName.isNotBlank() && friends.any { it.selected }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text("New Group") },
//        text = {
//            Column {
//                OutlinedTextField(
//                    value = groupName,
//                    onValueChange = { groupName = it },
//                    label = { Text("Group name") },
//                    singleLine = true,
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(Modifier.height(12.dp))
//
//                Text("Select members", fontWeight = FontWeight.SemiBold)
//                Spacer(Modifier.height(8.dp))
//
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .heightIn(max = 220.dp)
//                ) {
//                    items(friends) { friend ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable { toggleFriend(friend.id) }
//                                .padding(vertical = 4.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(friend.name)
//                            Spacer(Modifier.weight(1f))
//                            Checkbox(
//                                checked = friend.selected,
//                                onCheckedChange = { toggleFriend(friend.id) }
//                            )
//                        }
//                    }
//                }
//
//            }
//        },
//        confirmButton = {
//            TextButton(
//                enabled = canCreate,
//                onClick = {
//                    val ids = friends.filter { it.selected }.map { it.id }
//                    onConfirm(groupName.trim(), ids)
//                }
//            ) {
//                Text("Create")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) {
//                Text("Cancel")
//            }
//        }
//    )
//}
