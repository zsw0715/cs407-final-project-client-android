package com.cs407.knot_client_android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.knot_client_android.R
import kotlinx.coroutines.launch

@Composable
fun ExpandableBottomSheet(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    isDraggable: Boolean,
    modifier: Modifier = Modifier,
    onExpandProgressChange: (Float) -> Unit = {} // 回调展开进度
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // 收起和展开的高度
    val collapsedHeight = 64.dp
    val expandedHeight = screenHeight * 0.5f
    
    // 动画状态
    val animatedHeight = remember { Animatable(collapsedHeight.value) }
    val coroutineScope = rememberCoroutineScope()
    
    // 当前高度（Dp）
    val currentHeight = animatedHeight.value.dp
    
    // 展开进度 (0f = 收起, 1f = 展开)
    val progress = ((animatedHeight.value - collapsedHeight.value) / 
                    (expandedHeight.value - collapsedHeight.value)).coerceIn(0f, 1f)
    
    // 通知外部展开进度变化
    LaunchedEffect(progress) {
        onExpandProgressChange(progress)
    }
    
    // 累计拖动距离（用于判断拖动意图）
    var accumulatedDrag by remember { mutableStateOf(0f) }
    val dragThreshold = with(LocalDensity.current) { 30.dp.toPx() } // 拖动阈值
    
    // 用于计算拖动速度（惯性滚动）
    var lastDragTime by remember { mutableStateOf(0L) }
    var lastDragAmount by remember { mutableStateOf(0f) }
    
    // 拖动结束后的处理（带惯性）
    fun snapToTarget() {
        coroutineScope.launch {
            // 计算拖动速度（用于惯性判断）
            val currentTime = System.currentTimeMillis()
            val timeDelta = (currentTime - lastDragTime).coerceAtLeast(1)
            val velocity = lastDragAmount / timeDelta * 1000 // px/s
            
            // 如果速度够快，根据速度方向判断；否则根据中点判断
            val target = if (kotlin.math.abs(velocity) > 500f) {
                // 快速滑动：根据速度方向
                if (velocity < 0) expandedHeight.value else collapsedHeight.value
            } else {
                // 慢速滑动：根据位置中点
                if (animatedHeight.value > (collapsedHeight.value + expandedHeight.value) / 2) {
                    expandedHeight.value
                } else {
                    collapsedHeight.value
                }
            }
            
            animatedHeight.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = 0.75f, // 更柔和的阻尼
                    stiffness = 120f // 降低弹性，更丝滑
                )
            )
        }
    }
    
    // 根据拖动方向自动完成动画（带惯性）
    fun autoCompleteByDirection(dragDirection: Float) {
        coroutineScope.launch {
            val target = if (dragDirection < 0) {
                // 向上拖动 → 展开
                expandedHeight.value
            } else {
                // 向下拖动 → 收起
                collapsedHeight.value
            }
            animatedHeight.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = 120f
                )
            )
        }
    }
    
    Box(modifier = modifier) {
        // 当前宽度（收起时窄，展开时宽）
        // 展开时宽度 = screenWidth - 16.dp（因为 MainScreen 左右各有 8dp padding）
        val currentWidth = 254.dp + (screenWidth - 254.dp - 16.dp) * progress
        
        // 动态圆角（收起时 32dp，展开时 48dp）
        val currentCornerRadius = 32.dp + 16.dp * progress
        
        // 毛玻璃背景层（增强柔和感）
        Box(
            modifier = Modifier
                .width(currentWidth)
                .height(currentHeight)
                .clip(RoundedCornerShape(currentCornerRadius))
                .background(Color.White.copy(alpha = 0.65f)) // 降低不透明度，更通透
                .blur(20.dp) // 增强模糊，更柔和
        )
        
        // 主容器（增强浮感）
        Box(
            modifier = Modifier
                .width(currentWidth)
                .height(currentHeight)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(currentCornerRadius))
                .clip(RoundedCornerShape(currentCornerRadius))
                .shadow(12.dp, RoundedCornerShape(currentCornerRadius)) // 增强阴影，更有浮感
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.85f)
                        )
                    )
                )
        ) {
            // 收起状态：显示导航栏（整个区域可拖动）
            Box(
                modifier = Modifier
                    .width(254.dp)
                    .height(64.dp)
                    .alpha(1f - progress)
                    .then(
                        if (isDraggable) {
                            Modifier.pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        accumulatedDrag = 0f
                                        lastDragTime = System.currentTimeMillis()
                                        lastDragAmount = 0f
                                    },
                                    onDragEnd = {
                                        accumulatedDrag = 0f
                                        snapToTarget()
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        
                                        // 记录拖动速度
                                        lastDragTime = System.currentTimeMillis()
                                        lastDragAmount = dragAmount
                                        
                                        // 累计拖动距离
                                        accumulatedDrag += dragAmount
                                        
                                        // 如果拖动超过阈值，自动完成动画
                                        if (kotlin.math.abs(accumulatedDrag) > dragThreshold) {
                                            autoCompleteByDirection(accumulatedDrag)
                                            accumulatedDrag = 0f
                                        } else {
                                            // 否则继续实时跟随
                                            val newHeight = (animatedHeight.value - dragAmount).coerceIn(
                                                collapsedHeight.value,
                                                expandedHeight.value
                                            )
                                            coroutineScope.launch {
                                                animatedHeight.snapTo(newHeight)
                                            }
                                        }
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
            }
            
            // 展开状态：显示内容
            if (progress > 0.1f) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(progress)
                        .padding(12.dp)
                        .background(Color.White.copy(alpha = 0.8f))
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    // 顶部：搜索框 + 头像
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 搜索框
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.8f))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // 头像
                        Image(
                            painter = painterResource(id = R.drawable.user_avatar),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 拖动指示器（始终可拖动）
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(24.dp)
                            .align(Alignment.CenterHorizontally)
                            .then(
                                if (isDraggable) {  // 移除 progress 限制，始终可拖动
                                    Modifier.pointerInput(Unit) {
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                accumulatedDrag = 0f
                                                lastDragTime = System.currentTimeMillis()
                                                lastDragAmount = 0f
                                            },
                                            onDragEnd = {
                                                accumulatedDrag = 0f
                                                snapToTarget()
                                            },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                
                                                // 记录拖动速度
                                                lastDragTime = System.currentTimeMillis()
                                                lastDragAmount = dragAmount
                                                
                                                // 累计拖动距离
                                                accumulatedDrag += dragAmount
                                                
                                                // 如果拖动超过阈值，自动完成动画
                                                if (kotlin.math.abs(accumulatedDrag) > dragThreshold) {
                                                    autoCompleteByDirection(accumulatedDrag)
                                                    accumulatedDrag = 0f
                                                } else {
                                                    // 否则继续实时跟随
                                                    val newHeight = (animatedHeight.value - dragAmount).coerceIn(
                                                        collapsedHeight.value,
                                                        expandedHeight.value
                                                    )
                                                    coroutineScope.launch {
                                                        animatedHeight.snapTo(newHeight)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFD1D5DB))
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // POSTS 标题
                    Text(
                        text = "POSTS",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 占位内容
                    Text(
                        text = "这里显示帖子内容\n\nPlaceholder Content",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

