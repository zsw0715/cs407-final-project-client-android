package com.cs407.knot_client_android.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.data.model.SharedPostNavigation
import com.cs407.knot_client_android.data.model.SharedPostPayload
import com.cs407.knot_client_android.utils.SimpleWebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val tokenStore = TokenStore(app)
    val wsManager = SimpleWebSocketManager()
    val incoming = wsManager.incoming
    private val _sharedPostRequest = MutableStateFlow<SharedPostNavigation?>(null)
    val sharedPostRequest: StateFlow<SharedPostNavigation?> = _sharedPostRequest.asStateFlow()
    private val _sharedPostNavigationActive = MutableStateFlow(false)
    val sharedPostNavigationActive: StateFlow<Boolean> = _sharedPostNavigationActive.asStateFlow()

    fun connectIfNeeded() {
        viewModelScope.launch {
            val jwt = tokenStore.get()
            if (jwt != null) {
                // 模拟器用 10.0.2.2；真机换成电脑局域网 IP
                wsManager.connect("ws://10.0.2.2:10827/ws", jwt)
            }
        }
    }

    fun send(json: String) = wsManager.send(json)

    fun requestSharedPostNavigation(payload: SharedPostPayload) {
        _sharedPostNavigationActive.value = true
        _sharedPostRequest.value = SharedPostNavigation(payload)
    }

    fun completeSharedPostNavigation() {
        _sharedPostNavigationActive.value = false
        _sharedPostRequest.value = null
    }

    override fun onCleared() {
        wsManager.disconnect()
        super.onCleared()
    }

}
