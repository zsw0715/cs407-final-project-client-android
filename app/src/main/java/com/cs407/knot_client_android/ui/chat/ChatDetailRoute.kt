package com.cs407.knot_client_android.ui.chat

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.cs407.knot_client_android.data.api.MessageApi
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.ui.main.MainViewModel
import kotlinx.coroutines.flow.collect
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun ChatDetailRoute(
    navController: NavHostController,
    convId: Long,
    title: String,
    myUid: Long,
    mainVm: MainViewModel,
    baseUrl: String = "http://10.0.2.2:8080/"
) {
    val appContext = LocalContext.current

    // 创建 Retrofit + MessageApi
    val messageApi = remember(baseUrl) {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MessageApi::class.java)
    }
    val tokenStore = remember { TokenStore(appContext) }

    val vm = remember {
        ChatDetailViewModel(
            convId = convId,
            title = title,
            myUid = myUid,
            messageApi = messageApi,
            tokenStore = tokenStore,
            sendRawJson = { json -> mainVm.send(json) }
        )
    }

    val state by vm.ui.collectAsState()

    // ① 首次进入页面 -> 拉取历史消息
    LaunchedEffect(Unit) {
        vm.loadHistory()
    }

    // ② 如果你还保留 WebSocket 实时收消息，在这里收（可选）
    LaunchedEffect(Unit) {
        mainVm.incoming.collect { json: String ->
            when (extractType(json)) {
                "MSG_NEW" -> {
                    val m = parseMsgNew(json)
                    if (m.convId == convId) {
                        vm.onMsgNew(
                            fromUid = m.fromUid,
                            msgId = m.msgId,
                            content = m.contentText
                        )
                    }
                }

                "MSG_ACK" -> {
                    val m = parseMsgAck(json)
                    vm.onMsgAck(
                        msgId = m.msgId,
                        clientMsgId = m.clientMsgId
                    )
                }
            }
        }
    }

    ChatDetailScreen(
        navController = navController,
        state = state,
        onDraftChange = { text -> vm.updateDraft(text) },
        onSendMessage = { text -> vm.sendMessage(text) }
    )
}
