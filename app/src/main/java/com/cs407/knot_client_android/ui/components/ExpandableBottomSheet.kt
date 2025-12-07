package com.cs407.knot_client_android.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.knot_client_android.R
import com.cs407.knot_client_android.data.model.response.MapPostNearby
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

@Composable
fun ExpandableBottomSheet(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    isDraggable: Boolean,
    modifier: Modifier = Modifier,
    onExpandProgressChange: (Float) -> Unit = {}, // 回调展开进度
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    searchResults: List<MapPostNearby> = emptyList(),
    isSearching: Boolean = false,
    searchErrorMessage: String? = null,
    hasSearched: Boolean = false,
    lastSearchQuery: String = "",
    onSearchResultClick: (MapPostNearby) -> Unit = {},
    defaultPosts: List<MapPostNearby> = emptyList()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val shortestSide = min(configuration.screenHeightDp, configuration.screenWidthDp)
    val isCompactScreen = shortestSide <= 600
    val componentScale = if (isCompactScreen) 0.9f else 1f
    val spacingScale = if (isCompactScreen) 0.85f else 1f
    val fontScale = if (isCompactScreen) 0.95f else 1f
    fun scaledSpacing(value: Dp) = value * spacingScale
    fun scaledComponent(value: Dp) = value * componentScale
    
    // 三个高度状态：收起、半展开、全展开
    val collapsedHeight = 70.dp
    val expandedHeightRatio = if (isCompactScreen) 0.6f else 0.5f
    val expandedHeight = screenHeight * expandedHeightRatio  // 半展开高度
    val maxExpandedHeight = screenHeight * 0.94f  // 全展开：94%
    
    // 动画状态
    val animatedHeight = remember { Animatable(collapsedHeight.value) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // 当前高度（Dp）
    val currentHeight = animatedHeight.value.dp
    
    // 展开进度 (0f = 收起, 1f = 半展开) - 用于内部动画
    val progress = ((animatedHeight.value - collapsedHeight.value) / 
                    (expandedHeight.value - collapsedHeight.value)).coerceIn(0f, 1f)
    
    // 完整展开进度 (0f = 收起, 1f = 半展开, 2f = 全展开) - 用于通知外部
    val fullProgress = when {
        animatedHeight.value <= expandedHeight.value -> {
            ((animatedHeight.value - collapsedHeight.value) / 
             (expandedHeight.value - collapsedHeight.value)).coerceIn(0f, 1f)
        }
        else -> {
            1f + ((animatedHeight.value - expandedHeight.value) / 
                  (maxExpandedHeight.value - expandedHeight.value)).coerceIn(0f, 1f)
        }
    }
    
    // 通知外部展开进度变化
    LaunchedEffect(fullProgress) {
        onExpandProgressChange(fullProgress)
    }
    
    // 记录拖动起始高度
    var dragStartHeight by remember { mutableStateOf(0f) }

    fun animateToHalfExpanded() {
        coroutineScope.launch {
            animatedHeight.animateTo(
                targetValue = expandedHeight.value,
                animationSpec = spring(
                    dampingRatio = 0.70f,
                    stiffness = 120f
                )
            )
        }
    }

    fun collapseToMinimum() {
        coroutineScope.launch {
            animatedHeight.animateTo(
                targetValue = collapsedHeight.value,
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 200f
                )
            )
        }
    }

    // 拖动结束后的处理 - 支持三个状态：收起(70dp)、半展开(50%)、全展开(93%)
    fun snapToTarget() {
        coroutineScope.launch {
            val current = animatedHeight.value
            
            // 定义三个吸附点
            val snapPoints = listOf(
                collapsedHeight.value,      // 70dp
                expandedHeight.value,        // 50%
                maxExpandedHeight.value      // 93%
            )
            
            // 找到最接近的吸附点
            val target = snapPoints.minByOrNull { kotlin.math.abs(it - current) } ?: expandedHeight.value
            
            animatedHeight.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = 0.70f,
                    stiffness = 120f
                )
            )
        }
    }
    
    Box(modifier = modifier) {
        // 判断是否处于第二阶段（半展开到全展开）
        val isPhase2 = animatedHeight.value > expandedHeight.value
        val shouldBlockMapTouches = progress > 0.95f
        
        // 当前宽度：三段式变化
        // 阶段1: 272.dp -> (screenWidth - 16.dp)
        // 阶段2: (screenWidth - 16.dp) -> screenWidth
        val currentWidth = if (isPhase2) {
            val phase2Progress = ((animatedHeight.value - expandedHeight.value) / 
                                  (maxExpandedHeight.value - expandedHeight.value)).coerceIn(0f, 1f)
            (screenWidth - 16.dp) + 16.dp * phase2Progress
        } else {
            272.dp + (screenWidth - 272.dp - 16.dp) * progress
        }
        
        // 动态圆角：三段式变化
        // 阶段1: 44.dp -> 51.dp (半展开状态)
        // 阶段2: 51.dp -> 42.dp (完全展开时)
        val currentCornerRadius = if (isPhase2) {
            val phase2Progress = ((animatedHeight.value - expandedHeight.value) / 
                                  (maxExpandedHeight.value - expandedHeight.value)).coerceIn(0f, 1f)
            if (phase2Progress < 0.5f) {
                51.dp - 18.dp * phase2Progress
            } else {
                42.dp + (51.dp - 42.dp) * (phase2Progress - 0.5f)
            }
        } else {
            44.dp + 7.dp * progress
        }
        
        // 毛玻璃背景层 - Android 原生系统级模糊
        // 动态透明度：收起时 0.5，展开时 0.9
        val blurAlpha = 0.5f + 0.4f * progress
        
        Box(
            modifier = Modifier
                .width(currentWidth)
                .height(currentHeight)
                .clip(RoundedCornerShape(currentCornerRadius))
                .graphicsLayer {
                    renderEffect = RenderEffect
                        .createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
                        .asComposeRenderEffect()
                }
                // .background(Color.White.copy(alpha = blurAlpha))
                .background(if (progress < 0.8f) Color.White.copy(alpha = blurAlpha) else Color(0xFFF8F6F4).copy(alpha = blurAlpha))
        )
        
        // 主容器
        Box(
            modifier = Modifier
                .width(currentWidth)
                .height(currentHeight)
                .border(1.dp, Color(0xFFE5E7EB).copy(alpha = 0.6f), RoundedCornerShape(currentCornerRadius))
                .clip(RoundedCornerShape(currentCornerRadius))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    )
                )
        ) {
            // 收起状态：显示导航栏（整个区域可拖动）
            Box(
                modifier = Modifier
                    .width(272.dp)
                    .height(70.dp)
                    .alpha(1f - progress)
                    .clickable(
                        enabled = isDraggable && progress <= 0.01f,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        animateToHalfExpanded()
                    }
                    .then(
                        if (isDraggable) {
                            Modifier.pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        // 记录起始高度
                                        dragStartHeight = animatedHeight.value
                                    },
                                    onDragEnd = {
                                        snapToTarget()
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        
                                        // 实时跟随手指，不触发任何自动动画，最高可以拖到80%
                                        val newHeight = (animatedHeight.value - dragAmount).coerceIn(
                                            collapsedHeight.value,
                                            maxExpandedHeight.value
                                        )
                                        coroutineScope.launch {
                                            animatedHeight.snapTo(newHeight)
                                        }
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onMapSearchShortcut = {
                animateToHalfExpanded()
            }
        )
            }
            
            // 展开状态：显示内容
            if (progress > 0.0f) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        // .background(Color(0xFFF8F6F4).copy(alpha = blurAlpha))
                        .clip(RoundedCornerShape(currentCornerRadius))
                        .alpha(progress)
                        .clickable (
                            enabled = shouldBlockMapTouches,
                            onClick = {},  // 消费点击事件，不让它穿透
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .verticalScroll(scrollState)
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                        .background(Color(0xFFF8F6F4).copy(alpha = blurAlpha))
                ) {
                    // 拖动指示器（始终可拖动） 
                    Box(
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth()
                            .then(
                                if (isDraggable) {  // 移除 progress 限制，始终可拖动
                                    Modifier.pointerInput(Unit) {
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                // 记录起始高度
                                                dragStartHeight = animatedHeight.value
                                            },
                                            onDragEnd = {
                                                snapToTarget()
                                            },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                
                                                // 实时跟随手指，不触发任何自动动画，最高可以拖到80%
                                                val newHeight = (animatedHeight.value - dragAmount).coerceIn(
                                                    collapsedHeight.value,
                                                    maxExpandedHeight.value
                                                )
                                                coroutineScope.launch {
                                                    animatedHeight.snapTo(newHeight)
                                                }
                                            }
                                        )
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        // 顶部：搜索框 + 头像
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .padding(horizontal = 8.dp), // 给点左右边距
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 搜索框（样式同步登录页输入框）
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { onSearchQueryChange(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                placeholder = {
                                    Text(
                                        "Search posts",
                                        color = Color(0xFFAAAAAA),
                                        fontSize = 15.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF9B8FD9).copy(alpha = 0.7f)
                                    )
                                },
                                shape = RoundedCornerShape(32.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                        focusedBorderColor = Color(0xFFB5A8FF), // 淡紫色
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                                    focusedContainerColor = Color.White
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        onSearch()
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // 头像
                            Image(
                                painter = painterResource(id = R.drawable.user_avatar),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = Color(0xFFDADADA),
                                        shape = CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // POSTS 标题
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "POSTS",
                            fontSize = 24.sp, // 稍微小一点，更精致
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1B1F)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (progress > 0.76f) {
                            // 副标题（Slogan）
                            Text(
                                text = "Share your little footprints with close friends",
                                fontSize = 14.sp,
                                color = Color(0xFF9B9B9B),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        val postsForDisplay = if (hasSearched) searchResults else defaultPosts
                        var cardsReady by remember { mutableStateOf(true) }
                        LaunchedEffect(postsForDisplay, fullProgress) {
                            if (postsForDisplay.isEmpty()) {
                                cardsReady = true
                            } else {
                                cardsReady = false
                                delay(380)
                                cardsReady = true
                            }
                        }
                        when {
                            isSearching -> {
                                SearchStatusText(
                                    text = "Searching posts...",
                                    showProgress = true,
                                    color = Color(0xFF6A6A6A),
                                    fontScale = fontScale,
                                    componentScale = componentScale
                                )
                            }
                            searchErrorMessage != null -> {
                                SearchStatusText(
                                    text = searchErrorMessage,
                                    showProgress = false,
                                    color = Color(0xFFD14343),
                                    fontScale = fontScale,
                                    componentScale = componentScale
                                )
                            }
                            hasSearched && searchResults.isEmpty() -> {
                                val hint = if (lastSearchQuery.isNotBlank()) {
                                    "No posts found for \"$lastSearchQuery\""
                                } else {
                                    "No posts found for this user"
                                }
                                SearchStatusText(
                                    text = hint,
                                    showProgress = false,
                                    color = Color(0xFF9B9B9B),
                                    fontScale = fontScale,
                                    componentScale = componentScale
                                )
                            }
                            !hasSearched && postsForDisplay.isEmpty() -> {
                                SearchStatusText(
                                    text = "Enter a username to explore their posts",
                                    showProgress = false,
                                    color = Color(0xFF9B9B9B),
                                    fontScale = fontScale,
                                    componentScale = componentScale
                                )
                            }
                            !hasSearched && postsForDisplay.isNotEmpty() -> {
                                SearchStatusText(
                                    text = "Latest posts from your friends",
                                    showProgress = false,
                                    color = Color(0xFF6A6A6A),
                                    fontScale = fontScale,
                                    componentScale = componentScale
                                )
                            }
                        }

                        if (postsForDisplay.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(scaledSpacing(8.dp)))
                            val placeholderHeight = if (fullProgress <= 1f) {
                                scaledComponent(230.dp)
                            } else {
                                scaledComponent(420.dp)
                            }
                            if (!cardsReady) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(placeholderHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp * componentScale),
                                        strokeWidth = 3.dp * componentScale,
                                        color = Color(0xFF9B8FD9)
                                    )
                                }
                            } else if (fullProgress <= 1f) {
                                val previewResults = postsForDisplay.take(5)
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(scaledSpacing(12.dp))
                                ) {
                                    items(previewResults) { post ->
                                        SearchResultCard(
                                            post = post,
                                            onClick = {
                                                collapseToMinimum()
                                                onSearchResultClick(post)
                                            },
                                            modifier = Modifier.width(scaledComponent(260.dp)),
                                            isCompact = true,
                                            componentScale = componentScale,
                                            fontScale = fontScale
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(scaledSpacing(8.dp)))
                                Text(
                                    text = if (hasSearched) "Swipe up to view all search results" else "Swipe up to view more nearby posts",
                                    fontSize = 12.sp * fontScale,
                                    color = Color(0xFF8A8A8A)
                                )
                            } else {
                                postsForDisplay.forEach { post ->
                                    SearchResultCard(
                                        post = post,
                                        onClick = {
                                            collapseToMinimum()
                                            onSearchResultClick(post)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        isCompact = false,
                                        componentScale = componentScale,
                                        fontScale = fontScale
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchStatusText(
    text: String,
    showProgress: Boolean,
    color: Color,
    fontScale: Float = 1f,
    componentScale: Float = 1f
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp * componentScale),
                strokeWidth = 2.dp * componentScale,
                color = Color(0xFF9B8FD9)
            )
        }
        Text(
            text = text,
            color = color,
            fontSize = 14.sp * fontScale,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SearchResultCard(
    post: MapPostNearby,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    componentScale: Float = 1f,
    fontScale: Float = 1f
) {
    val sizedModifier = if (isCompact) {
        modifier.height(220.dp * componentScale)
    } else {
        modifier
    }
    Card(
        modifier = sizedModifier
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        onClick = onClick
    ) {
        SearchResultContent(
            post = post,
            isCompact = isCompact,
            componentScale = componentScale,
            fontScale = fontScale
        )
    }
}

@Composable
private fun SearchResultContent(
    post: MapPostNearby,
    isCompact: Boolean,
    componentScale: Float,
    fontScale: Float
) {
    val previewUrl = post.mediaUrls?.firstOrNull()?.takeIf { it.isNotBlank() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 18.dp * componentScale,
                vertical = (if (isCompact) 12.dp else 16.dp) * componentScale
            ),
        verticalArrangement = Arrangement.spacedBy(
            (if (isCompact) 6.dp else 8.dp) * componentScale
        )
    ) {
        if (isCompact) {
            if (previewUrl != null) {
                PostImage(previewUrl, isCompact = true, componentScale = componentScale)
            }
            Text(
                text = post.title,
                fontSize = 17.sp * fontScale,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F)
            )
            if (previewUrl == null && post.description.isNotBlank()) {
                Text(
                    text = post.description,
                    fontSize = 13.sp * fontScale,
                    color = Color(0xFF4D4D4D),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "@${post.creatorUsername}",
                fontSize = 12.sp * fontScale,
                color = Color(0xFF9B9B9B)
            )
            Text(
                text = post.locName,
                fontSize = 12.sp * fontScale,
                color = Color(0xFF6A6A6A)
            )
        } else {
            if (previewUrl != null) {
                PostImage(previewUrl, isCompact = false, componentScale = componentScale)
            }
            Text(
                text = post.title,
                fontSize = 18.sp * fontScale,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1B1F)
            )
            if (post.description.isNotBlank()) {
                Text(
                    text = post.description,
                    fontSize = 14.sp * fontScale,
                    color = Color(0xFF4D4D4D),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "@${post.creatorUsername}",
                fontSize = 13.sp * fontScale,
                color = Color(0xFF9B9B9B)
            )
            Text(
                text = post.locName,
                fontSize = 13.sp * fontScale,
                color = Color(0xFF6A6A6A)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp * componentScale)
            ) {
                StatChip(label = "Likes", value = post.likeCount)
                StatChip(label = "Comments", value = post.commentCount)
                StatChip(label = "Views", value = post.viewCount)
            }
        }
    }
}

@Composable
private fun PostImage(
    url: String,
    isCompact: Boolean,
    componentScale: Float
) {
    val context = LocalContext.current
    val imageHeight = (if (isCompact) 120.dp else 160.dp) * componentScale
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(url)
            .size(Size.ORIGINAL)
            .crossfade(false)
            .build()
    )
    var showImage by remember(url) { mutableStateOf(false) }
    LaunchedEffect(url) {
        showImage = false
        delay(350)
        showImage = true
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(imageHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE8E8E8)),
        contentAlignment = Alignment.Center
    ) {
        if (showImage && painter.state !is AsyncImagePainter.State.Error) {
            Image(
                painter = painter,
                contentDescription = "Post image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp * componentScale),
                strokeWidth = 2.dp * componentScale,
                color = Color(0xFF9B8FD9)
            )
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: Int
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFF1EEFF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$value $label",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF5C4B99)
        )
    }
}
