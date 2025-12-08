package com.cs407.knot_client_android.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.knot_client_android.data.model.response.MapPostNearby
import com.cs407.knot_client_android.data.repository.MapPostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val posts: List<MapPostNearby> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchResults: List<MapPostNearby> = emptyList(),
    val isSearching: Boolean = false,
    val searchErrorMessage: String? = null,
    val lastSearchQuery: String = "",
    val hasSearched: Boolean = false
)

class MapViewModel : ViewModel() {

    // repository 需要 context 初始化
    private var repository: MapPostRepository? = null

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    /**
     * 在 MapScreen 里用：
     *
     * val context = LocalContext.current
     * LaunchedEffect(Unit) { mapViewModel.init(context) }
     */
    fun init(context: Context) {
        // 防止重复初始化
        if (repository != null) return
        repository = MapPostRepository(
            context = context,
            baseUrl = "http://3.144.236.205:8080"
        )
    }

    /**
     * HTTP 拉附近帖子（V2）
     * 结果直接塞进 uiState.posts
     */
    fun loadNearbyPostsV2(
        lat: Double,
        lng: Double,
        radius: Int = 5000,
        timeRange: String = "7D",
        postType: String = "ALL",
        maxResults: Int = 200
    ) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val list = repo.getNearbyPostsV2(
                    lat = lat,
                    lng = lng,
                    radius = radius,
                    timeRange = timeRange,
                    postType = postType,
                    maxResults = maxResults
                )
                _uiState.update {
                    it.copy(
                        posts = list,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "加载附近帖子失败"
                    )
                }
            }
        }
    }

    /**
     * WebSocket 收到 MAP_POST_NEW 的时候调用：
     * mapViewModel.addOrUpdatePost(newPost)
     */
    fun addOrUpdatePost(newPost: MapPostNearby) {
        _uiState.update { state ->
            val idx = state.posts.indexOfFirst { it.mapPostId == newPost.mapPostId }
            val newList = if (idx >= 0) {
                // 已存在 → 覆盖那一条
                state.posts.toMutableList().apply {
                    this[idx] = newPost
                }
            } else {
                // 新帖子 → 追加
                state.posts + newPost
            }
            state.copy(posts = newList)
        }
    }

    /**
     * 收到 MSG_NEW（有新评论）时可以调用：
     * mapViewModel.incrementCommentCountByConvId(msgNew.convId)
     */
    fun incrementCommentCountByConvId(convId: Long) {
        _uiState.update { state ->
            val idx = state.posts.indexOfFirst { it.convId == convId }
            if (idx < 0) return@update state

            val list = state.posts.toMutableList()
            val old = list[idx]
            list[idx] = old.copy(commentCount = old.commentCount + 1)

            state.copy(posts = list)
        }
    }

    /**
     * 删除指定 mapPostId 的帖子（用于本地 UI 同步）
     */
    fun removePostById(mapPostId: Long) {
        _uiState.update { state ->
            val newList = state.posts.filterNot { it.mapPostId == mapPostId }
            state.copy(posts = newList)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 根据用户名搜索帖子，结果用于底部搜索弹窗
     */
    fun searchPostsByUsername(username: String) {
        val repo = repository ?: return
        val query = username.trim()
        if (query.isEmpty()) {
            _uiState.update {
                it.copy(
                    searchResults = emptyList(),
                    searchErrorMessage = null,
                    isSearching = false,
                    lastSearchQuery = "",
                    hasSearched = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSearching = true,
                    searchErrorMessage = null,
                    lastSearchQuery = query,
                    hasSearched = true
                )
            }
            try {
                val results = repo.getPostsByUsername(query)
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isSearching = false,
                        searchErrorMessage = null,
                        lastSearchQuery = query,
                        hasSearched = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        searchResults = emptyList(),
                        isSearching = false,
                        searchErrorMessage = e.message ?: "搜索失败",
                        lastSearchQuery = query,
                        hasSearched = true
                    )
                }
            }
        }
    }

    fun clearSearchError() {
        _uiState.update { it.copy(searchErrorMessage = null) }
    }
}
