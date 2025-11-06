package com.cs407.knot.ui.features.usersettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.knot.data.model.Friend
import com.cs407.knot.data.model.FriendRequest
import com.cs407.knot.data.repo.FakeUserRepo
import com.cs407.knot.data.repo.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class UserSettingsViewModel(private val repo: UserRepository = FakeUserRepo()) : ViewModel() {
    data class UiState(
        val avatar: String = "https://i.pravatar.cc/300?img=13",
        val requests: List<FriendRequest> = emptyList(),
        val friends: List<Friend> = emptyList()
    )


    val uiState: StateFlow<UiState> = combine(repo.friendRequests, repo.friends) { r, f -> UiState(requests = r, friends = f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())


    fun accept(req: FriendRequest) = viewModelScope.launch { repo.accept(req) }
    fun decline(req: FriendRequest) = viewModelScope.launch { repo.decline(req) }
    fun remove(friend: Friend) = viewModelScope.launch { repo.remove(friend) }
}