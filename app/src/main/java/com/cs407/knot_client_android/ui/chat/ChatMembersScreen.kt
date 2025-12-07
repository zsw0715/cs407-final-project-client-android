package com.cs407.knot_client_android.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.cs407.knot_client_android.data.api.MessageApi
import com.cs407.knot_client_android.data.api.ConversationApi
import com.cs407.knot_client_android.data.api.RetrofitProvider
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.data.repository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 简单的 participant 数据类
// richer participant model backed by the conversation members API
data class Participant(
    val uid: Long? = null,
    val name: String,
    val avatarUrl: String?,
    val email: String? = null,
    val gender: String? = null,
    val statusMessage: String? = null,
    val birthdate: String? = null
)

@Composable
fun ChatMembersRoute(
    navController: NavHostController,
    convId: Long,
    title: String,
    convType: Int = 1,
    baseUrl: String = "http://10.0.101.215:8080/"
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    val messageApi = remember(baseUrl) {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MessageApi::class.java)
    }

    var participants by remember { mutableStateOf<List<Participant>>(emptyList()) }
    var isOneOnOne by remember { mutableStateOf(convType == 1) }
    var membersLoaded by remember { mutableStateOf(false) }
    val myUid = remember { tokenStore.getUserId() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(convId) {
        scope.launch {
            try {
                val token = tokenStore.getAccessToken()
                if (token == null) {
                    // no token -> mark loaded so UI shows leave button and avoids infinite loading state
                    membersLoaded = true
                    return@launch
                }
                val convApi = RetrofitProvider.createConversationService(baseUrl)
                val authHeader = "Bearer $token"

                // 首先调用 members API 获取完整成员信息（nickname, avatarUrl, email, gender, statusMessage, birthdate）
                try {
                    val membersResp = convApi.getConversationMembers(authHeader, convId.toString())
                    val list = membersResp.data
                    if (membersResp.success && !list.isNullOrEmpty()) {
                        participants = list.map { m ->
                            Participant(
                                uid = null,
                                name = m.nickname ?: "",
                                avatarUrl = m.avatarUrl,
                                email = m.email,
                                gender = m.gender,
                                statusMessage = m.statusMessage,
                                birthdate = m.birthdate
                            )
                        }
                        // 使用传入的 convType 判断是否为单聊（后端提供的更可靠）
                        // convType == 1 表示一对一聊天
                        isOneOnOne = (convType == 1)
                        membersLoaded = true
                        return@launch
                    }
                } catch (_: Exception) {
                    // 如果 members API 不可用或失败，退回到基于消息的收集策略
                }

                // fallback: collect senders from message history
                try {
                    val resp = messageApi.getMessages(authorization = authHeader, conversationId = convId, page = 1, size = 1000)
                    val msgs = resp.data?.messageList ?: emptyList()
                    val map = linkedMapOf<Long, Participant>()

                    for (m in msgs) {
                        val uid = m.senderId
                        val name = m.senderNickname ?: "User ${uid}"
                        val avatar = m.senderAvatarUrl
                        if (!map.containsKey(uid)) {
                            map[uid] = Participant(uid = uid, name = name, avatarUrl = avatar)
                        }
                    }

                    // 把自己加入（如果未在消息中出现）
                    val myUid = tokenStore.getUserId()
                    val myName = tokenStore.getUsername() ?: "Me"
                    if (myUid != null && !map.containsKey(myUid)) {
                        map[myUid] = Participant(uid = myUid, name = myName, avatarUrl = null)
                    }

                    // 尝试用 UserRepository 补全 avatarUrl（如果消息里没有）
                    val repo = UserRepository(context.applicationContext, baseUrl = baseUrl)
                    for ((id, p) in map) {
                        if (p.avatarUrl.isNullOrBlank()) {
                            try {
                                val info = repo.getUserInfoByUsername(p.name)
                                map[id] = p.copy(avatarUrl = info.avatarUrl)
                            } catch (_: Exception) {
                                // ignore
                            }
                        }
                    }

                    participants = map.values.toList()
                    // 保持使用传入的 convType 判定，不再依赖成员数
                    isOneOnOne = (convType == 1)
                    membersLoaded = true
                } catch (_: Exception) {
                    // ignore
                        membersLoaded = true
                }
            } catch (_: Exception) {
                // ignore top-level
                    membersLoaded = true
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showLeaveConfirm by remember { mutableStateOf(false) }

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // friend api for adding members (lazy load when needed)
    val friendApi = remember { RetrofitProvider.createFriendService(baseUrl) }
    var friendList by remember { mutableStateOf<List<com.cs407.knot_client_android.data.model.response.FriendItemDto>>(emptyList()) }
    var selectedFriendIds by remember { mutableStateOf<List<Long>>(emptyList()) }

    fun refreshMembers() {
        // trigger a reload by launching a coroutine to call members API again
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val token = tokenStore.getAccessToken() ?: return@launch
                val convApi = RetrofitProvider.createConversationService(baseUrl)
                val resp = convApi.getConversationMembers("Bearer $token", convId.toString())
                val list = resp.data
                if (resp.success && !list.isNullOrEmpty()) {
                    val updated = list.map { m ->
                        Participant(
                            uid = null,
                            name = m.nickname ?: "",
                            avatarUrl = m.avatarUrl,
                            email = m.email,
                            gender = m.gender,
                            statusMessage = m.statusMessage,
                            birthdate = m.birthdate
                        )
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        participants = updated
                        isOneOnOne = (list.size <= 2)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    ChatMembersScreen(
        navController = navController,
        title = title,
        participants = participants,
        isOneOnOne = isOneOnOne,
        membersLoaded = membersLoaded,
        myUid = myUid,
        onAddRequested = {
            if (isOneOnOne) {
                // show a snackbar informing add is not supported in one-on-one
                scope.launch { snackbarHostState.showSnackbar("Cannot add members to a one-on-one chat") }
            } else {
                showAddDialog = true
            }
        },
        onLeaveRequested = { showLeaveConfirm = true },
        snackbarHostState = snackbarHostState
    )

    // Add members dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add friends to group") },
            text = {
                Column {
                    // load friend list if empty
                    LaunchedEffect(Unit) {
                        try {
                            val token = tokenStore.getAccessToken() ?: return@LaunchedEffect
                            val resp = friendApi.getFriendList("Bearer $token")
                            if (resp.success && !resp.data.isNullOrEmpty()) {
                                // filter out friends who are already in this conversation and also filter out self
                                val myId = tokenStore.getUserId()
                                val existingIds = participants.mapNotNull { it.uid }.toSet()
                                val existingNames = participants.map { it.name }.toSet()
                                friendList = resp.data.filter { f ->
                                    val notSelf = myId == null || f.friendId != myId
                                    val notInById = !existingIds.contains(f.friendId)
                                    val notInByName = !existingNames.contains(f.username)
                                    notSelf && notInById && notInByName
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }

                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(friendList) { f ->
                            val checked = selectedFriendIds.contains(f.friendId)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Text(text = f.username, modifier = Modifier.weight(1f))
                                androidx.compose.material3.Checkbox(checked = checked, onCheckedChange = { c ->
                                    selectedFriendIds = if (c) selectedFriendIds + f.friendId else selectedFriendIds - f.friendId
                                })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // call joinGroup for each selected id
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        val token = tokenStore.getAccessToken() ?: return@launch
                        val convApi = RetrofitProvider.createConversationService(baseUrl)
                        // capture the ids locally to avoid race with UI clearing
                        val toAdd = selectedFriendIds
                        for (uid in toAdd) {
                            try {
                                val req = com.cs407.knot_client_android.data.model.request.ConversationActionReq(conversationId = convId.toString(), userId = uid.toString())
                                val resp = convApi.joinGroup("Bearer $token", req)
                                // ignore individual errors for now
                            } catch (_: Exception) {
                            }
                        }
                        // refresh members and close
                        refreshMembers()
                        val addedCount = toAdd.size
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            showAddDialog = false
                            selectedFriendIds = emptyList()
                            snackbarHostState.showSnackbar("Added $addedCount member${if (addedCount == 1) "" else "s"}")
                        }
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Leave confirmation
    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirm = false },
            title = { Text("Leave conversation") },
            text = { Text(if (isOneOnOne) "Leave this conversation with the other user?" else "Leave this group?") },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveConfirm = false
                    // call leaveGroup for myself
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        try {
                            val token = tokenStore.getAccessToken() ?: return@launch
                            val convApi = RetrofitProvider.createConversationService(baseUrl)
                            val uid = tokenStore.getUserId() ?: return@launch
                            val req = com.cs407.knot_client_android.data.model.request.ConversationActionReq(conversationId = convId.toString(), userId = uid.toString())
                            val resp = convApi.leaveGroup("Bearer $token", req)
                            if (resp.success) {
                                // navigate back to main
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    navController.popBackStack()
                                }
                            } else {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    snackbarHostState.showSnackbar(resp.message ?: "Leave failed")
                                }
                            }
                        } catch (_: Exception) {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Leave failed")
                            }
                        }
                    }
                }) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMembersScreen(
    navController: NavHostController,
    title: String,
    participants: List<Participant>,
    isOneOnOne: Boolean = false,
    myUid: Long? = null,
    membersLoaded: Boolean = false,
    onAddRequested: () -> Unit = {},
    onLeaveRequested: () -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    // 单聊时过滤掉自己（只在有 uid 信息时过滤；成员 API 返回的数据可能没有 uid，这种情况显示所有）
    val displayedParticipants = if (isOneOnOne && myUid != null && participants.any { it.uid != null }) {
        participants.filter { it.uid != myUid }
    } else {
        participants
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = if (isOneOnOne && displayedParticipants.isNotEmpty()) displayedParticipants[0].name else title,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // actions moved to below the members list to be more discoverable
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            LazyColumn(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                items(displayedParticipants) { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!p.avatarUrl.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = p.avatarUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = p.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?", fontSize = 18.sp, color = Color(0xFF6B7280))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 昵称
                        Text(
                            text = p.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 邮箱
                        if (!p.email.isNullOrBlank()) {
                            Text(
                                text = "📧 ${p.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // 性别
                        if (!p.gender.isNullOrBlank()) {
                            Text(
                                text = "👤 ${p.gender}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // 签名/状态消息
                        if (!p.statusMessage.isNullOrBlank()) {
                            Text(
                                text = "✏️ ${p.statusMessage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // 生日
                        if (!p.birthdate.isNullOrBlank()) {
                            Text(
                                text = "🎂 ${p.birthdate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
            }

            // buttons area below members list
            // per request: always show both buttons (no conditional visibility)
            val showAddButton = true
            val showLeaveButton = true

            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (showAddButton) {
                    Button(
                        onClick = onAddRequested,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text("Add members")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (showLeaveButton) {
                    Button(
                        onClick = onLeaveRequested,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text("Leave conversation", color = Color.White)
                    }
                }
            }
        }
    }
}
