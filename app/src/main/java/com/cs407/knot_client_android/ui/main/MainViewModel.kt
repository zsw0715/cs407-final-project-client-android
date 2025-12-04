package com.cs407.knot_client_android.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.utils.SimpleWebSocketManager
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val tokenStore = TokenStore(app)
    val wsManager = SimpleWebSocketManager()
    val incoming = wsManager.incoming

    fun connectIfNeeded() {
        viewModelScope.launch {
            val jwt = tokenStore.get()
            if (jwt != null) {
                // Production server
                wsManager.connect("ws://3.144.236.205:10827/ws", jwt)
            }
        }
    }

    fun send(json: String) = wsManager.send(json)

    override fun onCleared() {
        wsManager.disconnect()
        super.onCleared()
    }

}
