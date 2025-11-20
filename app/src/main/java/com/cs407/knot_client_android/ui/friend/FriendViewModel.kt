package com.cs407.knot_client_android.ui.friend

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.knot_client_android.data.repository.FriendRepository
import com.cs407.knot_client_android.data.repository.UserRepository
import com.cs407.knot_client_android.ui.main.MainViewModel
import com.cs407.knot_client_android.utils.SimpleWebSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FriendViewModel(application: Application) : AndroidViewModel(application) {

    private val baseUrl = "http://10.0.2.2:8080"
    private var wsManager: SimpleWebSocketManager? = null

    private val userRepository = UserRepository(
        application.applicationContext,
        baseUrl
    )

    private val friendRepository = FriendRepository(
        application.applicationContext,
        baseUrl
    )

    private val gson = Gson()

    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
        loadIncomingRequests()
    }

    fun attachMainViewModel(mainVm: MainViewModel) {
        this.wsManager = mainVm.wsManager
    }

    /** 按用户名搜索用户 */
    fun searchUserByUsername(username: String) {
        if (username.isBlank()) {
            _uiState.update {
                it.copy(
                    searchError = "Username cannot be empty.",
                    searchedUser = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchError = null, searchedUser = null) }
            try {
                val info = userRepository.getUserInfoByUsername(username.trim())

                val summary = FriendUserSummary(
                    userId = info.userid,
                    username = info.username,
                    avatar = info.avatarUrl,
                    convId = null,
                    createdAtMs = null
                )

                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchedUser = summary,
                        searchError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchedUser = null,
                        searchError = e.message ?: "Failed to search user."
                    )
                }
            }
        }
    }

    /** 加载好友列表（HTTP） */
    fun loadFriends() {
        viewModelScope.launch {
            try {
                val resp = friendRepository.getFriends()
                if (resp.success && resp.data != null) {
                    _uiState.update { it.copy(friends = resp.data) }
                }
            } catch (_: Exception) {
            }
        }
    }

    /** 加载收到的好友申请列表（HTTP） */
    fun loadIncomingRequests() {
        viewModelScope.launch {
            try {
                val resp = friendRepository.getIncomingRequests()
                if (resp.success && resp.data != null) {
                    _uiState.update { it.copy(incomingRequests = resp.data) }
                }
            } catch (_: Exception) {
            }
        }
    }

    /** 发送好友请求（WebSocket） */
    fun sendFriendRequest(receiverId: Long, message: String?) {
        val payload = FriendRequestSendPayload(
            receiverId = receiverId,
            message = message?.takeIf { it.isNotBlank() }
        )
        sendWebSocketJson(payload)
    }

    /**
     * 接受好友请求（WebSocket + 本地移除这一条）
     *
     * 注意：requestId 是后端生成的“好友申请记录 ID”，和 userId 不同。
     */
    fun acceptRequest(requestId: Long) {
        val payload = FriendRequestActionPayload(
            type = "FRIEND_REQUEST_ACCEPT",
            requestId = requestId
        )
        sendWebSocketJson(payload)

        _uiState.update {
            it.copy(
                incomingRequests = it.incomingRequests.filterNot { req ->
                    req.requestId == requestId
                }
            )
        }
    }

    /** 拒绝好友请求（WebSocket + 本地移除这一条） */
    fun rejectRequest(requestId: Long) {
        val payload = FriendRequestActionPayload(
            type = "FRIEND_REQUEST_REJECT",
            requestId = requestId
        )
        sendWebSocketJson(payload)

        _uiState.update {
            it.copy(
                incomingRequests = it.incomingRequests.filterNot { req ->
                    req.requestId == requestId
                }
            )
        }
    }

    /** 统一序列化并通过 MainViewModel 的 WebSocket 发送 */
    private fun sendWebSocketJson(payload: Any) {
        val json = gson.toJson(payload)
        Log.d("FriendViewModel", "Sending WS payload: $json")
        val manager = wsManager
        if (manager == null) {
            Log.w("FriendViewModel", "wsManager is null, cannot send WS message")
            return
        }
        manager.send(json)
    }
}

/** 发送好友申请的 payload */
data class FriendRequestSendPayload(
    val type: String = "FRIEND_REQUEST_SEND",
    val receiverId: Long,
    val message: String?
)

/** 接受 / 拒绝好友申请的 payload */
data class FriendRequestActionPayload(
    val type: String,
    val requestId: Long
)
