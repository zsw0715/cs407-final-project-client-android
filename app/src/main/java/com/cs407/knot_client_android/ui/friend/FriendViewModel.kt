package com.cs407.knot_client_android.ui.friend

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.utils.SimpleWebSocketManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FriendViewModel(app: Application) : AndroidViewModel(app) {
    private val tokenStore = TokenStore(app)
    private val wsManager = SimpleWebSocketManager()
    private val gson = Gson()

    private val sendDrafts = ArrayDeque<FriendRequestDraft>()

    private val _uiState = MutableStateFlow(
        FriendUiState(
            currentUserId = tokenStore.getUserId()
        )
    )
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    init {
        observeConnectionState()
        observeIncomingMessages()
        reconnect()
    }

    fun onWsUrlChange(newUrl: String) {
        _uiState.update { it.copy(wsUrl = newUrl) }
    }

    fun toggleConnection() {
        if (_uiState.value.isConnected) {
            wsManager.disconnect()
            showBanner("已手动断开 WebSocket", false)
        } else {
            reconnect()
        }
    }

    fun reconnect() {
        viewModelScope.launch {
            val jwt = tokenStore.get()
            if (jwt.isNullOrBlank()) {
                showBanner("缺少 access token，请重新登录", true)
                return@launch
            }
            wsManager.connect(_uiState.value.wsUrl, jwt)
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(bannerMessage = null) }
    }

    private fun showBanner(text: String, isError: Boolean) {
        _uiState.update { it.copy(bannerMessage = BannerMessage(text, isError)) }
    }

    fun sendFriendRequest(receiverInput: String, message: String) {
        val receiverId = receiverInput.toLongOrNull()
        if (receiverId == null) {
            showBanner("请输入有效的用户 ID", true)
            return
        }
        if (!_uiState.value.isConnected) {
            showBanner("请先连接 WebSocket", true)
            return
        }
        val payload = mapOf(
            "type" to "FRIEND_REQUEST_SEND",
            "receiverId" to receiverId,
            "message" to message.ifBlank { "Hi, let's be friends!" }
        )
        sendDrafts.addLast(FriendRequestDraft(receiverId, message))
        wsManager.send(gson.toJson(payload))
        addLog("尝试向用户 #$receiverId 发送好友申请")
    }

    fun acceptRequest(requestId: Long) {
        val payload = mapOf(
            "type" to "FRIEND_REQUEST_ACCEPT",
            "requestId" to requestId
        )
        wsManager.send(gson.toJson(payload))
        addLog("发送接受操作 -> #$requestId")
    }

    fun rejectRequest(requestId: Long) {
        val payload = mapOf(
            "type" to "FRIEND_REQUEST_REJECT",
            "requestId" to requestId
        )
        wsManager.send(gson.toJson(payload))
        addLog("发送拒绝操作 -> #$requestId")
    }

    fun resendRequest(item: FriendRequestItem) {
        val receiverId = item.peer.userId
        if (receiverId == null) {
            showBanner("无法获取对方用户 ID", true)
            return
        }
        sendFriendRequest(receiverId.toString(), item.message)
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            wsManager.connectionState.collectLatest { connected ->
                _uiState.update { it.copy(isConnected = connected) }
                addLog(if (connected) "✅ 已连接至好友系统" else "❌ WebSocket 已断开")
            }
        }
    }

    private fun observeIncomingMessages() {
        viewModelScope.launch {
            wsManager.incoming.collect { raw ->
                addLog("⬇️ $raw")
                runCatching {
                    val obj = gson.fromJson(raw, JsonObject::class.java)
                    when (obj.get("type")?.asString) {
                        "FRIEND_REQUEST_NEW" -> handleIncomingRequest(obj)
                        "FRIEND_REQUEST_ACK" -> handleAck(obj)
                        "FRIEND_ADDED" -> handleFriendAdded(obj)
                        else -> Unit
                    }
                }.onFailure {
                    showBanner("解析消息失败: ${it.message}", true)
                }
            }
        }
    }

    private fun handleIncomingRequest(obj: JsonObject) {
        val requestId = obj.get("requestId")?.asLong ?: return
        val message = obj.get("message")?.asString ?: ""
        val timestamp = obj.get("timestamp")?.asLong
        val fromUser = obj.getAsJsonObject("fromUser")
        val userSummary = FriendUserSummary(
            userId = fromUser?.get("userId")?.asLong,
            username = fromUser?.get("username")?.asString,
            avatarUrl = fromUser?.get("avatarUrl")?.asString
        )
        val newItem = FriendRequestItem(
            requestId = requestId,
            message = message,
            timestamp = timestamp,
            status = FriendRequestStatus.Pending,
            direction = FriendRequestDirection.Incoming,
            peer = userSummary
        )
        _uiState.update { state ->
            state.copy(
                incomingRequests = (state.incomingRequests.filterNot { it.requestId == requestId } + newItem)
                    .sortedByDescending { it.timestamp ?: 0L },
                bannerMessage = BannerMessage("收到来自 ${userSummary.displayName} 的好友申请")
            )
        }
    }

    private fun handleAck(obj: JsonObject) {
        val status = obj.get("status")?.asString ?: return
        val requestId = obj.get("requestId")?.asLong ?: return
        val timestamp = obj.get("timestamp")?.asLong
        when (status.lowercase(Locale.ROOT)) {
            "sent" -> handleSendAck(requestId, timestamp)
            "accepted" -> handleAcceptAck(requestId, obj.get("convId")?.asLong)
            "rejected" -> handleRejectAck(requestId)
            else -> showBanner("收到未知状态: $status", true)
        }
    }

    private fun handleSendAck(requestId: Long, timestamp: Long?) {
        val draft = sendDrafts.removeFirstOrNull()
        val fallbackName = draft?.receiverId?.let { "User #$it" } ?: "Request #$requestId"
        val newItem = FriendRequestItem(
            requestId = requestId,
            message = draft?.message ?: "",
            timestamp = timestamp,
            status = FriendRequestStatus.Pending,
            direction = FriendRequestDirection.Outgoing,
            peer = FriendUserSummary(
                userId = draft?.receiverId,
                username = fallbackName
            )
        )
        _uiState.update { state ->
            state.copy(
                outgoingRequests = (state.outgoingRequests.filterNot { it.requestId == requestId } + newItem)
                    .sortedByDescending { it.timestamp ?: 0L },
                bannerMessage = BannerMessage("好友申请已发送，等待对方确认")
            )
        }
    }

    private fun handleAcceptAck(requestId: Long, convId: Long?) {
        _uiState.update { state ->
            state.copy(
                incomingRequests = state.incomingRequests.filterNot { it.requestId == requestId },
                bannerMessage = BannerMessage(
                    text = "已接受好友申请，自动创建会话 ${convId ?: "--"}",
                    isError = false
                )
            )
        }
    }

    private fun handleRejectAck(requestId: Long) {
        _uiState.update { state ->
            state.copy(
                incomingRequests = state.incomingRequests.filterNot { it.requestId == requestId },
                outgoingRequests = state.outgoingRequests.filterNot { it.requestId == requestId },
                bannerMessage = BannerMessage("已拒绝或取消好友申请")
            )
        }
    }

    private fun handleFriendAdded(obj: JsonObject) {
        val friendObj = obj.getAsJsonObject("friend") ?: return
        val requestId = obj.get("requestId")?.asLong
        val user = FriendUserSummary(
            userId = friendObj.get("userId")?.asLong,
            username = friendObj.get("username")?.asString,
            avatarUrl = friendObj.get("avatarUrl")?.asString
        )
        _uiState.update { state ->
            state.copy(
                outgoingRequests = requestId?.let { rid ->
                    state.outgoingRequests.filterNot { it.requestId == rid }
                } ?: state.outgoingRequests,
                friends = (state.friends.filterNot { it.userId == user.userId } + user)
                    .sortedBy { it.displayName },
                bannerMessage = BannerMessage("${user.displayName} 已成为好友，可立即聊天")
            )
        }
    }

    private fun addLog(line: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _uiState.update { state ->
            val updated = (state.logs + "[$timestamp] $line").takeLast(60)
            state.copy(logs = updated)
        }
    }

    override fun onCleared() {
        wsManager.disconnect()
        super.onCleared()
    }

    private fun <T> ArrayDeque<T>.removeFirstOrNull(): T? = if (isEmpty()) null else removeFirst()

    private data class FriendRequestDraft(
        val receiverId: Long,
        val message: String
    )
}