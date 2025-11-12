package com.cs407.knot_client_android.ui.chat

import android.content.Context
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cs407.knot_client_android.data.api.ConversationApi
import com.cs407.knot_client_android.data.local.TokenStore
import kotlinx.coroutines.flow.collectLatest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun ChatRoute(
    navController: NavHostController,
    appContext: Context,
    baseUrl: String = "http://10.0.2.2:8080" // 改成你的
) {
    // 轻量创建 Retrofit + Api
    val api = remember(baseUrl) {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConversationApi::class.java)
    }
    val vm = remember { ChatViewModel(api, TokenStore(appContext)) }

    // 拉取数据
    LaunchedEffect(Unit) { vm.load() }

    val state by vm.ui.collectAsState()
    ChatScreen(navController = navController, state = state)
}
