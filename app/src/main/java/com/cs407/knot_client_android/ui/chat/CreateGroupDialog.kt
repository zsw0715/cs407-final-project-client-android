package com.cs407.knot_client_android.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


// 单个可选好友的 UI 数据
data class SelectableFriendUi(
    val id: Long,
    val name: String,
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Text("Select members", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                ) {
                    items(friends) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toggleFriend(friend.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(friend.name)
                            Spacer(Modifier.weight(1f))
                            Checkbox(
                                checked = friend.selected,
                                onCheckedChange = { toggleFriend(friend.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canCreate,
                onClick = {
                    val ids = friends.filter { it.selected }.map { it.id }
                    onConfirm(groupName.trim(), ids)
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
