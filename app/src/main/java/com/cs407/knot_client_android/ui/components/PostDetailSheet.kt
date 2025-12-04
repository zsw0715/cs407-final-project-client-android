package com.cs407.knot_client_android.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
//import androidx.compose.material.icons.outlined.ChatBubbleOutline
//import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.material.icons.outlined.Face
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cs407.knot_client_android.R
import com.cs407.knot_client_android.data.api.RetrofitProvider
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.data.model.SharedPostPayload
import com.cs407.knot_client_android.data.model.response.ConversationMessage
import com.cs407.knot_client_android.data.model.response.MapPostDetailResponse
import com.cs407.knot_client_android.data.model.response.MapPostNearby
import com.cs407.knot_client_android.data.model.WebSocketMessage
import com.cs407.knot_client_android.data.model.MessageNewMessage
import com.cs407.knot_client_android.data.model.MapPostLikeAckMessage
import com.cs407.knot_client_android.data.model.MapPostLikeUpdateMessage
import com.cs407.knot_client_android.ui.chat.MsgSend
import com.cs407.knot_client_android.ui.chat.MsgSendLoc
import com.cs407.knot_client_android.ui.chat.toJson
import com.cs407.knot_client_android.ui.main.MainViewModel
import com.google.gson.Gson
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Comment Data Class (converted from ConversationMessage)
data class Comment(
    val commentId: Long,
    val username: String,
    val avatarUrl: String?,
    val content: String,
    val timestamp: String,
    val likeCount: Int
)

enum class ShareTargetType {
    GROUP, CONTACT
}

data class ShareTargetUi(
    val id: Long,
    val name: String,
    val subtitle: String? = null,
    val avatarUrl: String? = null,
    val type: ShareTargetType
)

enum class ShareTargetFilter {
    ALL, GROUPS, CONTACTS
}

@Composable
fun PostDetailSheet(
    post: MapPostNearby?,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokenStore = remember(context) { TokenStore(context) }
    val baseUrl = remember { "http://3.144.236.205:8080" }
    val mapPostApi = remember { RetrofitProvider.createMapPostService(baseUrl) }
    val friendApi = remember { RetrofitProvider.createFriendService(baseUrl) }
    val conversationApi = remember { RetrofitProvider.createConversationService(baseUrl) }
    val shareLoaderScope = rememberCoroutineScope()
    val gson = remember { Gson() }

    // 状态管理
    var postDetail by remember { mutableStateOf<MapPostDetailResponse?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoadingDetail by remember { mutableStateOf(false) }
    var isLoadingComments by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var localCommentCount by remember { mutableStateOf(0) }
    var localLikeCount by remember { mutableStateOf(0) }
    var isLikedByMe by remember { mutableStateOf(false) }

    // 分页状态
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    var isLoadingMoreComments by remember { mutableStateOf(false) }
    val hasMorePages by remember { derivedStateOf { currentPage < totalPages } }

    var isShareDialogVisible by remember { mutableStateOf(false) }
    var shareTargets by remember { mutableStateOf<List<ShareTargetUi>>(emptyList()) }
    var shareTargetsLoading by remember { mutableStateOf(false) }
    var shareTargetsError by remember { mutableStateOf<String?>(null) }

    suspend fun loadShareTargets() {
        if (shareTargetsLoading) return
        shareTargetsLoading = true
        shareTargetsError = null
        try {
            val token = tokenStore.getAccessToken()
            if (token.isNullOrBlank()) {
                shareTargets = emptyList()
                shareTargetsError = "请先登录后再分享"
                shareTargetsLoading = false
                return
            }
            val authHeader = "Bearer $token"

            val friendResult = kotlin.runCatching { friendApi.getFriendList(authHeader) }
            val conversationResult = kotlin.runCatching { conversationApi.getConversationList(authHeader) }

            val aggregated = mutableListOf<ShareTargetUi>()

            friendResult.getOrNull()
                ?.takeIf { it.success && !it.data.isNullOrEmpty() }
                ?.let { resp ->
                    aggregated += resp.data.orEmpty().map { dto ->
                        ShareTargetUi(
                            id = dto.convId,
                            name = dto.username,
                            subtitle = "Friend",
                            avatarUrl = dto.avatar,
                            type = ShareTargetType.CONTACT
                        )
                    }
                }

            conversationResult.getOrNull()
                ?.takeIf { it.success && !it.data.isNullOrEmpty() }
                ?.let { resp ->
                    aggregated += resp.data.orEmpty()
                        .filter { it.convType != 1 }
                        .map { dto ->
                            val subtitle = dto.memberCount?.let { count ->
                                "Group · $count members"
                            } ?: "Group chat"
                            ShareTargetUi(
                                id = dto.convId,
                                name = dto.title?.takeIf { it.isNotBlank() }
                                    ?: dto.otherUserName
                                    ?: "Group ${dto.convId}",
                                subtitle = subtitle,
                                avatarUrl = dto.groupAvatar,
                                type = ShareTargetType.GROUP
                            )
                        }
                }

            shareTargets = aggregated.distinctBy { it.type to it.id }

            if (shareTargets.isEmpty()) {
                val friendMsg = friendResult.exceptionOrNull()?.message
                    ?: friendResult.getOrNull()?.message
                    ?: friendResult.getOrNull()?.error
                val conversationMsg = conversationResult.exceptionOrNull()?.message
                    ?: conversationResult.getOrNull()?.message
                    ?: conversationResult.getOrNull()?.error
                shareTargetsError = friendMsg ?: conversationMsg
            } else {
                shareTargetsError = null
            }
        } catch (e: Exception) {
            shareTargets = emptyList()
            shareTargetsError = e.message ?: "加载分享列表失败"
        } finally {
            shareTargetsLoading = false
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            loadShareTargets()
        }
    }

    fun sharePostToChat(target: ShareTargetUi) {
        val detail = postDetail
        if (detail == null) {
            Toast.makeText(context, "post loading", Toast.LENGTH_SHORT).show()
            return
        }

        val clientMsgId = "share-${System.currentTimeMillis()}"
        val cover = detail.mediaUrls?.firstOrNull()
        val payload = SharedPostPayload(
            mapPostId = detail.mapPostId,
            convId = detail.convId,
            creatorId = detail.creatorId,
            title = detail.title,
            description = detail.description,
            coverUrl = cover,
            creatorUsername = detail.creatorUsername,
            locName = detail.locName,
            locLat = detail.locLat,
            locLng = detail.locLng
        )
        val payloadJson = gson.toJson(payload)
        val contentText = "shared post：${detail.title}"

        val message = MsgSend(
            convId = target.id,
            clientMsgId = clientMsgId,
            msgType = 6,
            contentText = contentText,
            mediaUrl = cover,
            mediaThumbUrl = cover,
            mediaMetaJson = payloadJson,
            loc = MsgSendLoc(
                lat = detail.locLat,
                lng = detail.locLng,
                name = detail.locName,
                accuracy = null
            )
        )

        try {
            mainViewModel.send(message.toJson())
            Toast
                .makeText(
                    context,
                    "Shared to ${target.name}",
                    Toast.LENGTH_SHORT
                )
                .show()
        } catch (e: Exception) {
            Toast
                .makeText(
                    context,
                    "share failed：${e.message ?: "未知错误"}",
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    // 加载帖子详情和评论
    LaunchedEffect(post?.mapPostId, isVisible) {
        if (isVisible && post != null) {
            // 加载帖子详情
            isLoadingDetail = true
            errorMessage = null
            try {
                val tokenStore = TokenStore(context)
                val token = tokenStore.getAccessToken()
                val apiService = RetrofitProvider.createMapPostService("http://3.144.236.205:8080")
                
                val response = apiService.getMapPostDetail("Bearer $token", post.mapPostId)
                if (response.success && response.data != null) {
                    postDetail = response.data
                    localCommentCount = response.data.commentCount
                    localLikeCount = response.data.likeCount
                    isLikedByMe = response.data.isLikedByCurrentUser

                    // 加载评论（第一页）
                    isLoadingComments = true
                    try {
                        val commentsResponse = apiService.getConversationMessages(
                            token = "Bearer $token",
                            conversationId = response.data.convId,
                            page = 1,
                            size = 5  // 每页5条
                        )
                        if (commentsResponse.success && commentsResponse.data != null) {
                            // 保存分页信息
                            currentPage = commentsResponse.data.page
                            totalPages = commentsResponse.data.totalPages
                            
                            // 转换 ConversationMessage 到 Comment
                            comments = commentsResponse.data.messageList.map { msg: ConversationMessage ->
                                Comment(
                                    commentId = msg.msgId,
                                    username = msg.senderNickname ?: "User ${msg.senderId}",
                                    avatarUrl = msg.senderAvatarUrl,
                                    content = msg.contentText ?: "",
                                    timestamp = formatTimestamp(msg.createdAt),
                                    likeCount = 0
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isLoadingComments = false
                    }
                } else {
                    errorMessage = response.message ?: "获取帖子详情失败"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message ?: "网络错误"
            } finally {
                isLoadingDetail = false
            }
        }
    }
    
    // 🔔 监听 WebSocket 消息（实时接收新评论 / 点赞更新）
    LaunchedEffect(isVisible, postDetail?.convId) {
        postDetail?.let { currentPostDetail ->
            if (isVisible) {
                mainViewModel.wsManager.rawMessages.collect { message ->
                    message?.let {
                        try {
                            val gson = Gson()
                            val baseMessage = gson.fromJson(it, WebSocketMessage::class.java)

                            when (baseMessage.type) {
                                "MSG_NEW" -> {
                                    val msgNew = gson.fromJson(it, MessageNewMessage::class.java)

                                    // 只处理当前对话的消息
                                    if (msgNew.convId == currentPostDetail.convId) {
                                        // 创建新评论（目前后端 MSG_NEW 未携带头像信息，这里 avatarUrl 先置为 null）
                                        val newComment = Comment(
                                            commentId = msgNew.msgId,
                                            username = "User ${msgNew.fromUid}",
                                            avatarUrl = null,
                                            content = msgNew.contentText ?: "",
                                            timestamp = "just now",
                                            likeCount = 0
                                        )

                                        // 添加到评论列表开头
                                        comments = listOf(newComment) + comments

                                        // 本地评论数 +1
                                        localCommentCount += 1
                                    }
                                }

                                "MAP_POST_LIKE_ACK" -> {
                                    val ack = gson.fromJson(it, MapPostLikeAckMessage::class.java)
                                    if (ack.mapPostId == currentPostDetail.mapPostId) {
                                        localLikeCount = ack.likeCount
                                        isLikedByMe = ack.liked
                                    }
                                }

                                "MAP_POST_LIKE_UPDATE" -> {
                                    val update = gson.fromJson(it, MapPostLikeUpdateMessage::class.java)
                                    if (update.mapPostId == currentPostDetail.mapPostId) {
                                        localLikeCount = update.likeCount
                                        // 是否自己点赞由 ACK 决定，这里只同步计数
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
    
    // 加载下一页评论的函数
    val loadMoreComments: suspend () -> Unit = {
        postDetail?.let { currentPostDetail ->
            if (!isLoadingMoreComments && hasMorePages) {
                isLoadingMoreComments = true
                try {
                    val tokenStore = TokenStore(context)
                    val token = tokenStore.getAccessToken()
                    val apiService = RetrofitProvider.createMapPostService("http://3.144.236.205:8080")
                    
                    val commentsResponse = apiService.getConversationMessages(
                        token = "Bearer $token",
                        conversationId = currentPostDetail.convId,
                        page = currentPage + 1,
                        size = 5
                    )
                
                if (commentsResponse.success && commentsResponse.data != null) {
                    // 更新分页信息
                    currentPage = commentsResponse.data.page
                    totalPages = commentsResponse.data.totalPages
                    
                    // 追加新评论到列表
                    val newComments = commentsResponse.data.messageList.map { msg: ConversationMessage ->
                        Comment(
                            commentId = msg.msgId,
                            username = msg.senderNickname ?: "User ${msg.senderId}",
                            avatarUrl = msg.senderAvatarUrl,
                            content = msg.contentText ?: "",
                            timestamp = formatTimestamp(msg.createdAt),
                            likeCount = 0
                        )
                    }
                    comments = comments + newComments
                }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoadingMoreComments = false
                }
            }
        }
    }

    // 点赞点击逻辑（乐观更新 + 发送 WebSocket）
    val onToggleLike: () -> Unit = like@{
        val detail = postDetail ?: return@like
        val mapPostId = detail.mapPostId
        val newLiked = !isLikedByMe

        // 本地乐观更新
        isLikedByMe = newLiked
        localLikeCount = (localLikeCount + if (newLiked) 1 else -1).coerceAtLeast(0)

        // 构建并发送 MAP_POST_LIKE 消息
        val message = mapOf(
            "type" to "MAP_POST_LIKE",
            "clientReqId" to "like-$mapPostId-${System.currentTimeMillis()}",
            "mapPostId" to mapPostId,
            "liked" to newLiked
        )
        val gson = Gson()
        val json = gson.toJson(message)
        mainViewModel.send(json)
    }

    PostDetailSheetContent(
        post = post,
        postDetail = postDetail,
        comments = comments,
        localCommentCount = localCommentCount,
        localLikeCount = localLikeCount,
        isLikedByMe = isLikedByMe,
        currentPage = currentPage,
        isVisible = isVisible,
        isLoadingDetail = isLoadingDetail,
        isLoadingComments = isLoadingComments,
        isLoadingMoreComments = isLoadingMoreComments,
        hasMorePages = hasMorePages,
        errorMessage = errorMessage,
        onDismiss = onDismiss,
        mainViewModel = mainViewModel,
        onLoadMoreComments = loadMoreComments,
        onToggleLike = onToggleLike,
        onShareClick = { isShareDialogVisible = true },
        modifier = modifier
    )

    if (isShareDialogVisible) {
        PostShareDialog(
            targets = shareTargets,
            isLoading = shareTargetsLoading,
            errorMessage = shareTargetsError,
            onDismiss = { isShareDialogVisible = false },
            onRetry = {
                shareLoaderScope.launch {
                    loadShareTargets()
                }
            },
            onConfirm = { target ->
                isShareDialogVisible = false
                sharePostToChat(target)
            }
        )
    }
}

// 格式化时间戳
private fun formatTimestamp(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(timestamp)
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        when {
            days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
            else -> "just now"
        }
    } catch (e: Exception) {
        timestamp
    }
}

@Composable
private fun PostDetailSheetContent(
    post: MapPostNearby?,
    postDetail: MapPostDetailResponse?,
    comments: List<Comment>,
    localCommentCount: Int,
    localLikeCount: Int,
    isLikedByMe: Boolean,
    currentPage: Int,
    isVisible: Boolean,
    isLoadingDetail: Boolean,
    isLoadingComments: Boolean,
    isLoadingMoreComments: Boolean,
    hasMorePages: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    mainViewModel: MainViewModel,
    onLoadMoreComments: suspend () -> Unit,
    onToggleLike: () -> Unit,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    val context = LocalContext.current

    // 测量内容高度
    var contentHeightPx by remember { mutableStateOf(0) }
    val contentHeight = with(density) { contentHeightPx.toDp() }
    
    // 输入框状态
    var commentText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var shouldFocusInput by remember { mutableStateOf(false) }
    
    // 两个高度状态：半展开(动态)、全展开(94%)
    // 半展开高度：内容高度 + padding，但不超过屏幕的 70%
    val halfExpandedHeight = remember(contentHeight) {
        if (contentHeight > 0.dp) {
            (contentHeight + 48.dp).coerceAtMost(screenHeight * 0.7f)
        } else {
            screenHeight * 0.5f  // 默认值，在测量完成前使用
        }
    }
    val fullExpandedHeight = screenHeight * 0.94f  // 全展开：94%
    
    // 动画状态
    val animatedHeight = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    
    // 记录拖动起始高度
    var dragStartHeight by remember { mutableStateOf(0f) }
    
    // 监听 isVisible 和 halfExpandedHeight 变化，触发动画
    LaunchedEffect(isVisible, halfExpandedHeight) {
        if (isVisible) {
            // 展开到半展开状态（动态高度）
            animatedHeight.animateTo(
                targetValue = halfExpandedHeight.value,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            // 收起
            animatedHeight.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    // 拖动结束后的处理 - 支持三个状态：关闭(0)、半展开(50%)、全展开(94%)
    fun snapToTarget() {
        coroutineScope.launch {
            val current = animatedHeight.value
            
            // 定义三个吸附点
            val snapPoints = listOf(
                0f,                           // 关闭
                halfExpandedHeight.value,     // 50%
                fullExpandedHeight.value      // 94%
            )
            
            // 找到最接近的吸附点
            val target = snapPoints.minByOrNull { kotlin.math.abs(it - current) } ?: halfExpandedHeight.value
            
            animatedHeight.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = 0.70f,
                    stiffness = 120f
                )
            )
            
            // 如果吸附到关闭状态，通知外部
            if (target == 0f) {
                onDismiss()
            }
        }
    }
    
    // 点击评论按钮的处理
    fun onCommentClick() {
        coroutineScope.launch {
            // 如果不是全展开状态，先展开到全展开
            if (animatedHeight.value < fullExpandedHeight.value) {
                animatedHeight.animateTo(
                    targetValue = fullExpandedHeight.value,
                    animationSpec = spring(
                        dampingRatio = 0.70f,
                        stiffness = 120f
                    )
                )
            }
            // 设置标志以请求焦点
            shouldFocusInput = true
        }
    }
    
    // 当需要聚焦输入框时，请求焦点
    LaunchedEffect(shouldFocusInput) {
        if (shouldFocusInput) {
            kotlinx.coroutines.delay(300) // 等待动画完成
            focusRequester.requestFocus()
            shouldFocusInput = false
        }
    }
    
    // 关闭按钮触发的关闭动画
    fun closeWithAnimation() {
        coroutineScope.launch {
            animatedHeight.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessLow
                )
            )
            // 动画完成后通知外部
            onDismiss()
        }
    }
    
    // 监听动画高度，当接近 0 时自动同步状态
    LaunchedEffect(animatedHeight.value) {
        if (animatedHeight.value < 5f && isVisible) {
            onDismiss()
        }
    }
    
    // 当前高度
    val currentHeight = animatedHeight.value.dp
    
    // 判断是否处于第二阶段（半展开到全展开）
    val isPhase2 = animatedHeight.value > halfExpandedHeight.value
    
    // 动态 padding：16dp (半展开) -> 0dp (全展开)
    val currentPadding = if (isPhase2) {
        val phase2Progress = ((animatedHeight.value - halfExpandedHeight.value) / 
                              (fullExpandedHeight.value - halfExpandedHeight.value)).coerceIn(0f, 1f)
        8.dp * (1f - phase2Progress)
    } else {
        8.dp
    }
    
    // 动态圆角：51.dp (半展开) -> 42.dp (全展开)
    val currentCornerRadius = if (isPhase2) {
        val phase2Progress = ((animatedHeight.value - halfExpandedHeight.value) / 
                              (fullExpandedHeight.value - halfExpandedHeight.value)).coerceIn(0f, 1f)
        51.dp - 9.dp * phase2Progress
    } else {
        51.dp
    }
    
    // Sheet 容器
    if (animatedHeight.value > 0f && post != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(currentHeight)
                .padding(start = currentPadding, end = currentPadding, bottom = currentPadding)
        ) {
            // 毛玻璃背景层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(currentCornerRadius))
                    .graphicsLayer {
                        renderEffect = RenderEffect
                            .createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
                            .asComposeRenderEffect()
                    }
                    .background(Color.White.copy(alpha = 0.65f))
            )
            
            // 主容器
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.dp,
                        Color(0xFFE5E7EB).copy(alpha = 0.6f),
                        RoundedCornerShape(currentCornerRadius)
                    )
                    .clip(RoundedCornerShape(currentCornerRadius))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .clickable(
                        enabled = true,
                        onClick = {}, // 消费点击事件，防止穿透
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8F6F4))
                        .clip(RoundedCornerShape(currentCornerRadius))
                        .padding(horizontal = 28.dp)
                ) {
                    Spacer(Modifier.height(28.dp))
                    
                    // 可滚动内容
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 帖子内容区域（始终显示）- 带拖动手势和高度测量
                        item {
                            if (isLoadingDetail) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(64.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        color = Color(0xFF636EF1)
                                    )
                                }
                            } else if (errorMessage != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = errorMessage,
                                        fontSize = 14.sp,
                                        color = Color(0xFFE53E3E)
                                    )
                                }
                            } else if (postDetail != null) {
                                PostContentSection(
                                    postDetail = postDetail,
                                    post = post,
                                    localCommentCount = localCommentCount,
                                    localLikeCount = localLikeCount,
                                    isLikedByMe = isLikedByMe,
                                    onCommentClick = { onCommentClick() },
                                    onToggleLike = onToggleLike,
                                    onShareClick = onShareClick,
                                    onDrag = { dragAmount ->
                                        // 实时跟随手指
                                        val newHeight = (animatedHeight.value - dragAmount).coerceIn(
                                            0f,
                                            fullExpandedHeight.value
                                        )
                                        coroutineScope.launch {
                                            animatedHeight.snapTo(newHeight)
                                        }
                                    },
                                    onDragStart = {
                                        dragStartHeight = animatedHeight.value
                                    },
                                    onDragEnd = {
                                        snapToTarget()
                                    },
                                    onHeightMeasured = { heightPx ->
                                        contentHeightPx = heightPx
                                    }
                                )
                            }
                        }
                        
                        // 评论区域（只在第二阶段显示）
                        if (isPhase2) {
                            item {
                                Spacer(Modifier.height(24.dp))
                                Text(
                                    text = "COMMENTS ($localCommentCount)",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1C1B1F)
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                            
                            // 加载中状态
                            if (isLoadingComments) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            color = Color(0xFF636EF1)
                                        )
                                    }
                                }
                            } else {
                                items(comments) { comment ->
                                    CommentItem(comment = comment)
                                    Spacer(Modifier.height(12.dp))
                                }
                                
                                // 空状态
                                if (comments.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No comments yet",
                                                fontSize = 14.sp,
                                                color = Color(0xFF9B9B9B)
                                            )
                                        }
                                    }
                                }
                                
                                // 加载更多评论
                                if (hasMorePages && comments.isNotEmpty()) {
                                    item(key = "load_more_$currentPage") {
                                        // 自动触发加载下一页（使用 currentPage 作为 key 确保每页只触发一次）
                                        LaunchedEffect(currentPage) {
                                            if (!isLoadingMoreComments) {
                                                onLoadMoreComments()
                                            }
                                        }
                                        
                                        // 加载中指示器
                                        if (isLoadingMoreComments) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                androidx.compose.material3.CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    color = Color(0xFF636EF1),
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // 评论输入框
                            item {
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(24.dp))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                                        .padding(horizontal = 16.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        value = commentText,
                                        onValueChange = { commentText = it },
                                        placeholder = {
                                            Text(
                                                text = "Write a comment...",
                                                color = Color(0xFF9B9B9B),
                                                fontSize = 14.sp
                                            )
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(focusRequester),
                                        singleLine = false,
                                        maxLines = 4
                                    )
                                    
                                    IconButton(
                                        onClick = {
                                            if (commentText.isNotBlank() && postDetail != null) {
                                                // 生成唯一的 clientMsgId（使用时间戳）
                                                val clientMsgId = "c-${System.currentTimeMillis()}"
                                                
                                                // 构建 WebSocket 消息
                                                val message = mapOf(
                                                    "type" to "MSG_SEND",
                                                    "convId" to postDetail.convId,
                                                    "clientMsgId" to clientMsgId,
                                                    "msgType" to 0,  // 0 = 文本消息
                                                    "contentText" to commentText
                                                )
                                                
                                                // 通过 WebSocket 发送
                                                val gson = Gson()
                                                val json = gson.toJson(message)
                                                mainViewModel.send(json)
                                                
                                                // 清空输入框
                                                commentText = ""
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = if (commentText.isNotBlank()) Color(0xFF636EF1) else Color(0xFF9B9B9B),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            
                            // 底部留白
                            item {
                                Spacer(Modifier.height(48.dp))
                            }
                        }
                    }
                }
                
                // 右上角关闭按钮 - 浮动在内容之上
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 24.dp, end = 24.dp)
                ) {
                    // 动画状态管理
                    val closeButtonInteractionSource = remember { MutableInteractionSource() }
                    val isCloseButtonPressed by closeButtonInteractionSource.collectIsPressedAsState()
                    
                    val closeButtonScale = remember { Animatable(1f) }
                    
                    LaunchedEffect(isCloseButtonPressed) {
                        if (isCloseButtonPressed) {
                            // 按下：快速放大一点点
                            closeButtonScale.animateTo(
                                targetValue = 1.2f,
                                animationSpec = tween(
                                    durationMillis = 170, 
                                    easing = LinearOutSlowInEasing
                                )
                            )
                        } else {
                            // 松手：先缩回一点再弹回 1
                            closeButtonScale.animateTo(
                                targetValue = 0.88f,
                                animationSpec = tween(
                                    durationMillis = 155, 
                                    easing = FastOutLinearInEasing
                                )
                            )
                            // 然后自然回弹到 1
                            closeButtonScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                    
                    // 毛玻璃背景层 - Android 原生系统级模糊
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .graphicsLayer {
                                renderEffect = RenderEffect
                                    .createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
                                    .asComposeRenderEffect()
                            }
                            .background(Color.White.copy(alpha = 0.65f))
                    )
                    
                    // 主按钮
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(closeButtonScale.value)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE5E7EB).copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.2f)
                                    )
                                )
                            )
                            .clickable(
                                onClick = { closeWithAnimation() },
                                indication = null,
                                interactionSource = closeButtonInteractionSource
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(30.dp),
                            tint = if (isCloseButtonPressed) 
                                Color(0xFF636EF1) // 按下时：蓝紫色
                            else 
                                Color(0xFF6B7280) // 正常时：gray-600
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostContentSection(
    postDetail: MapPostDetailResponse,
    post: MapPostNearby?,
    localCommentCount: Int, // 本地评论数（实时更新）
    localLikeCount: Int,
    isLikedByMe: Boolean,
    onCommentClick: () -> Unit = {},
    onToggleLike: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onDrag: (Float) -> Unit = {},
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onHeightMeasured: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                // 测量内容高度
                onHeightMeasured(coordinates.size.height)
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                )
            }
    ) {
        // 帖子标题
        Text(
            text = postDetail.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F)
        )
        
        Spacer(Modifier.height(12.dp))
        
        // 作者信息
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 作者头像（优先使用 creatorAvatar，若为空则使用首字母占位，与 ChatDetailScreen 风格一致）
            val creatorAvatarUrl = postDetail.creatorAvatar
            if (!creatorAvatarUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(model = creatorAvatarUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initial = postDetail.creatorUsername.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column {
                Text(
                    text = postDetail.creatorUsername,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = "${(post?.distance ?: 0.0 / 1000).toInt()} meters away",
                    fontSize = 13.sp,
                    color = Color(0xFF9B9B9B)
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // 帖子描述
        Text(
            text = postDetail.description ?: "No description available",
            fontSize = 16.sp,
            color = Color(0xFF4A5568),
            lineHeight = 24.sp
        )
        
        Spacer(Modifier.height(16.dp))
        
        // 图片横向 Carousel（折叠 / 展开都显示）
        if (!postDetail.mediaUrls.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(postDetail.mediaUrls) { url ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(260.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = url),
                                contentDescription = "Post photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
        }
        
        // 位置信息
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = Color(0xFF9B9B9B),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = postDetail.locName ?: "Unknown Location",
                fontSize = 14.sp,
                color = Color(0xFF9B9B9B)
            )
        }
        
        Spacer(Modifier.height(20.dp))
        
        // 互动统计
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Outlined.Face,
                count = postDetail.viewCount,
                label = "Views"
            )
            StatItem(
                icon = if (isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                count = localLikeCount,
                label = "Likes",
                onClick = onToggleLike
            )
            StatItem(
                icon = Icons.Outlined.Create,
                count = localCommentCount,
                label = "Comnt",
                onClick = onCommentClick
            )
            StatItem(
                icon = Icons.Outlined.Share,
                count = null,
                label = "Share",
                onClick = onShareClick
            )
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int? = null,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF636EF1),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        if (count != null) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
            Spacer(Modifier.height(2.dp))
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF9B9B9B)
        )
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // 头像（优先使用后端返回的 avatar，若为空则使用首字母占位，风格参考 ChatDetailScreen）
        if (!comment.avatarUrl.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(model = comment.avatarUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val initial = comment.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B7280)
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 用户名和时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comment.username,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = comment.timestamp,
                    fontSize = 12.sp,
                    color = Color(0xFF9B9B9B)
                )
            }
            
            Spacer(Modifier.height(6.dp))
            
            // 评论内容
            Text(
                text = comment.content,
                fontSize = 14.sp,
                color = Color(0xFF4A5568),
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(8.dp))
            
            // 点赞数
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Likes",
                    tint = Color(0xFF9B9B9B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = comment.likeCount.toString(),
                    fontSize = 12.sp,
                    color = Color(0xFF9B9B9B)
                )
            }
        }
    }
}

@Composable
fun PostShareDialog(
    targets: List<ShareTargetUi>,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onConfirm: (ShareTargetUi) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ShareTargetFilter.ALL) }
    var selectedTargetId by remember { mutableStateOf<Long?>(null) }

    val locale = Locale.getDefault()
    val filteredTargets = remember(targets, searchQuery, selectedFilter) {
        val keyword = searchQuery.trim().lowercase(locale)
        targets.filter { target ->
            val matchesFilter = when (selectedFilter) {
                ShareTargetFilter.ALL -> true
                ShareTargetFilter.GROUPS -> target.type == ShareTargetType.GROUP
                ShareTargetFilter.CONTACTS -> target.type == ShareTargetType.CONTACT
            }
            val matchesQuery = if (keyword.isEmpty()) {
                true
            } else {
                target.name.lowercase(locale).contains(keyword) ||
                    (target.subtitle?.lowercase(locale)?.contains(keyword) ?: false)
            }
            matchesFilter && matchesQuery
        }
    }

    LaunchedEffect(targets) {
        selectedTargetId?.let { currentId ->
            if (targets.none { it.id == currentId }) {
                selectedTargetId = null
            }
        }
    }

    val selectedTarget = targets.firstOrNull { it.id == selectedTargetId }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(48.dp),
            color = Color(0xFFF8F6F4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Share to chat",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1B1F)
                    )
                    FloatingActionButton(
                        icon = Icons.Default.Close,
                        onClick = onDismiss,
                        containerSize = 42.dp,
                        iconSize = 20.dp
                    )
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    placeholder = { Text("Search groups or friends") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = Color(0xFF9B9B9B)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF636EF1),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShareFilterChip(
                        label = "All",
                        selected = selectedFilter == ShareTargetFilter.ALL,
                        onClick = { selectedFilter = ShareTargetFilter.ALL }
                    )
                    ShareFilterChip(
                        label = "Groups",
                        selected = selectedFilter == ShareTargetFilter.GROUPS,
                        onClick = { selectedFilter = ShareTargetFilter.GROUPS }
                    )
                    ShareFilterChip(
                        label = "Contacts",
                        selected = selectedFilter == ShareTargetFilter.CONTACTS,
                        onClick = { selectedFilter = ShareTargetFilter.CONTACTS }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = Color(0xFF636EF1)
                                )
                            }
                        }
                        errorMessage != null -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = errorMessage,
                                    fontSize = 16.sp,
                                    color = Color(0xFF9B9B9B)
                                )
                                if (onRetry != null) {
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = onRetry,
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF636EF1)
                                        )
                                    ) {
                                        Text("Retry", color = Color.White)
                                    }
                                }
                            }
                        }
                        filteredTargets.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No chats found",
                                    fontSize = 16.sp,
                                    color = Color(0xFF9B9B9B)
                                )
                                Text(
                                    text = "Try another keyword",
                                    fontSize = 13.sp,
                                    color = Color(0xFFCBD5F5)
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredTargets) { target ->
                                    ShareTargetRow(
                                        target = target,
                                        selected = target.id == selectedTargetId,
                                        onSelect = { selectedTargetId = target.id }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        selectedTarget?.let { onConfirm(it) }
                    },
                    enabled = selectedTarget != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF636EF1),
                        disabledContainerColor = Color(0xFF9B9B9B).copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = if (selectedTarget != null) {
                            "Share with ${selectedTarget.name}"
                        } else {
                            "Choose a chat"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) Color(0xFF636EF1) else Color.White.copy(alpha = 0.9f)
    val contentColor = if (selected) Color.White else Color(0xFF4B5563)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF636EF1) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ShareTargetRow(
    target: ShareTargetUi,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.95f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!target.avatarUrl.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(model = target.avatarUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )
        } else {
            val initial = target.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = target.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = target.subtitle ?: if (target.type == ShareTargetType.GROUP) {
                    "Group chat"
                } else {
                    "Contact"
                },
                fontSize = 13.sp,
                color = Color(0xFF9B9B9B)
            )
        }

        RadioButton(
            selected = selected,
            onClick = onSelect
        )
    }
}
