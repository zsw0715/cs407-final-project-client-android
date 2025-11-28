package com.cs407.knot_client_android.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.knot_client_android.data.api.ConversationApi
import com.cs407.knot_client_android.data.model.response.ConversationListResp
import com.cs407.knot_client_android.data.local.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val conversations: List<ConversationUi> = emptyList()
)

class ChatViewModel(
    private val api: ConversationApi,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _ui = MutableStateFlow(ChatUiState())
    val ui: StateFlow<ChatUiState> = _ui

    fun load() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val token = tokenStore.getAccessToken()
                if (token.isNullOrBlank()) {
                    _ui.value = ChatUiState(
                        loading = false,
                        error = "Not logged in"
                    )
                    return@launch
                }
                val resp: ConversationListResp = api.getConversationList("Bearer $token")
                val list = resp.toUiList()
                _ui.value = ChatUiState(loading = false, conversations = list)
            } catch (e: Exception) {
                _ui.value = ChatUiState(loading = false, error = e.message ?: "Unknown error")
            }
        }
    }
}
