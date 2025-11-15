package com.cs407.knot_client_android.ui.chat

import android.content.Context
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.cs407.knot_client_android.data.api.ConversationApi
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.navigation.Screen
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun ChatRoute(
    navController: NavHostController,
    appContext: Context,
    baseUrl: String = "http://10.0.2.2:8080/"
) {
    val api = remember(baseUrl) {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConversationApi::class.java)
    }
    val vm = remember { ChatViewModel(api, TokenStore(appContext)) }

    LaunchedEffect(Unit) { vm.load() }

    val state by vm.ui.collectAsState()

    ChatScreen(
        navController = navController,
        state = state,
        onOpenConversation = { conv ->
            // 跳转到聊天详情页，把 convId 和 title 传过去
            navController.navigate(
                Screen.ChatDetail.createRoute(conv.id, conv.displayTitle)
            )
        }
    )
}
