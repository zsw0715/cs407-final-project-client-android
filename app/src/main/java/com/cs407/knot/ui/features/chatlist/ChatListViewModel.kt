package com.cs407.knot.ui.features.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.knot.data.model.ChatPreview
import com.cs407.knot.data.repo.ChatRepository
import com.cs407.knot.data.repo.FakeChatRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class ChatListViewModel(
    private val repo: ChatRepository = FakeChatRepo() // replace with DI
) : ViewModel() {
    data class UiState(val chats: List<ChatPreview> = emptyList())
    val uiState: StateFlow<UiState> = repo.chats
        .map { UiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())
}