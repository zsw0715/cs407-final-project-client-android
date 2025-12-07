package com.cs407.knot_client_android.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
//import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import com.cs407.knot_client_android.R
import com.cs407.knot_client_android.navigation.Screen
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter


@Composable
fun ProfileScreen(
    navController: NavHostController
) {
    // 为了发 WS、断开 & 清 JWT
    val mainVm = viewModel<com.cs407.knot_client_android.ui.main.MainViewModel>()
    val profileVm = viewModel<ProfileViewModel>()
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenStore = remember { com.cs407.knot_client_android.data.local.TokenStore(context) }
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val shortestSide = kotlin.math.min(configuration.screenHeightDp, configuration.screenWidthDp)
    val isCompactScreen = shortestSide <= 600
    
    // 收集用户设置数据
    val userSettings by profileVm.userSettings.collectAsState()
    val isLoading by profileVm.loading.collectAsState()
    val error by profileVm.error.collectAsState()
    
    // 页面首次显示时加载用户数据
    LaunchedEffect(Unit) {
        profileVm.loadUserSettings()
    }
    
    // Snackbar 状态
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示错误信息
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            profileVm.clearError()
        }
    }

    // Profile 页面内容 - 不再包含导航栏
    Box(
        modifier = Modifier
            .fillMaxSize()
            // 主体：浅色柔和多段渐变
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFBEFE8),
                        Color(0xFFF2E5F5),
                        Color(0xFFCCCBE0)
                    ),
                    start = Offset(0f, 0f),       // 左上
                    end = Offset(1000f, 1000f)    // 右下，控制斜率
                )
            )
            // 顶部柔光层
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x44FFFFFF),
                        Color(0x11FFFFFF),
                        Color.Transparent
                    ),
                    center = androidx.compose.ui.geometry.Offset(250f, 150f),
                    radius = 700f
                )
            )
            // 底部轻微暗角：增强层次
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x14000000)
                    )
                )
            ).background(Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            
            // 按钮行 - Logout 左对齐，Edit 右对齐
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logout 按钮 - 左对齐，带动画
                val logoutInteractionSource = remember { MutableInteractionSource() }
                val logoutIsPressed by logoutInteractionSource.collectIsPressedAsState()
                val logoutScale = remember { Animatable(1f) }
                
                LaunchedEffect(logoutIsPressed) {
                    if (logoutIsPressed) {
                        logoutScale.animateTo(
                            targetValue = 1.2f,
                            animationSpec = tween(
                                durationMillis = 170,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    } else {
                        logoutScale.animateTo(
                            targetValue = 0.88f,
                            animationSpec = tween(
                                durationMillis = 155,
                                easing = FastOutLinearInEasing
                            )
                        )
                        logoutScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }
                
                // Logout 按钮容器 - 仿照 FloatingActionButton 的毛玻璃实现
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // 毛玻璃背景层 - Android 原生系统级模糊（固定大小，不放大）
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(42.dp))
                            .graphicsLayer {
                                renderEffect = RenderEffect
                                    .createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
                                    .asComposeRenderEffect()
                            }
                            .background(Color.White.copy(alpha = 0.65f))
                    ) {
                        // 占位内容，确保背景层大小与按钮一致
                        Text(
                            text = "Logout",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Transparent,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 9.dp)
                        )
                    }
                    
                    // 主按钮层 - 在毛玻璃背景之上，按中心放大
                    Button(
                        onClick = {
                            scope.launch {
                                // 1) 通过 WS 通知后端注销（如果已连接）
                                mainVm.send("""{"type":"LOGOUT"}""")

                                // 2) 清理本地认证态
                                tokenStore.clear()

                                // 3) 断开 WebSocket（后端也会关闭，我们这边主动断开更干净）
                                mainVm.wsManager.disconnect()

                                // 4) 导航回登录页，并清空返回栈，避免 Back 返回到主界面
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Main.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                         },
                        modifier = Modifier
                            .scale(logoutScale.value),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(42.dp),
                        interactionSource = logoutInteractionSource
                    ) {
                        Text(
                            text = "Logout",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF444444)
                        )
                    }
                }
                
                // Edit 按钮 - 右对齐，带动画
                val editInteractionSource = remember { MutableInteractionSource() }
                val editIsPressed by editInteractionSource.collectIsPressedAsState()
                val editScale = remember { Animatable(1f) }
                
                LaunchedEffect(editIsPressed) {
                    if (editIsPressed) {
                        editScale.animateTo(
                            targetValue = 1.2f,
                            animationSpec = tween(
                                durationMillis = 170,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    } else {
                        editScale.animateTo(
                            targetValue = 0.88f,
                            animationSpec = tween(
                                durationMillis = 155,
                                easing = FastOutLinearInEasing
                            )
                        )
                        editScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }

                // Edit 按钮容器 - 仿照 FloatingActionButton 的毛玻璃实现
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // 毛玻璃背景层 - Android 原生系统级模糊（固定大小，不放大）
                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .wrapContentWidth()
                            .clip(RoundedCornerShape(42.dp))
                            .graphicsLayer {
                                renderEffect = RenderEffect
                                    .createBlurEffect(40f, 40f, Shader.TileMode.CLAMP)
                                    .asComposeRenderEffect()
                            }
                            .background(Color.White.copy(alpha = 0.65f))
                    ) {
                        // 占位内容，确保背景层大小与按钮一致
                        Text(
                            text = "Edit",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Transparent,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 9.dp)
                        )
                    }
                    
                    // 主按钮层 - 在毛玻璃背景之上，按中心放大
                    Button(
                        onClick = { 
                            navController.navigate(Screen.ProfileEdit.route) {
                                popUpTo(Screen.ProfileEdit.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .scale(editScale.value),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(42.dp),
                        interactionSource = editInteractionSource
                    ) {
                        Text(
                            text = "Edit",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF444444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))
            
            val avatarSize = if (isCompactScreen) 140.dp else 180.dp
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .border(width = 2.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val avatarUrl = userSettings?.avatarUrl
                if (!avatarUrl.isNullOrBlank()) {
                    // 有头像 URL：加载网络头像
                    Image(
                        painter = rememberAsyncImagePainter(model = avatarUrl),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 无头像：使用首字母圆形占位（样式参考 ChatDetailScreen 的 MessageAvatar）
                    val initial = (userSettings?.nickname ?: "U")
                        .firstOrNull()
                        ?.uppercaseChar()
                        ?.toString() ?: "U"
                    Box(
                        modifier = Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🧾 名称
            Text(
                text = userSettings?.nickname ?: "N/A",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D33)
            )
            Spacer(modifier = Modifier.height(4.dp))
            // 用户 message
            Text(
                text = userSettings?.statusMessage ?: "You haven't set a status message yet!",
                fontSize = 14.sp,
                color = Color(0xFF5B5B65) // 中等灰色
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 📱 信息卡片
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .clip(RoundedCornerShape(42.dp))
                    .background(Color.White.copy(alpha = 0.45f))
                    .padding(10.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nickname
                    UserInfoItem(
                        icon = Icons.Default.Person,
                        label = "Nickname",
                        value = userSettings?.nickname ?: "N/A",
                        compact = isCompactScreen
                    )
                    
                    // Email
                    UserInfoItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = userSettings?.email ?: "N/A",
                        compact = isCompactScreen
                    )
                    
                    // Gender
                    UserInfoItem(
                        icon = Icons.Default.Face,
                        label = "Gender",
                        value = userSettings?.gender ?: "N/A",
                        compact = isCompactScreen
                    )
                    
                    // Birthday
                    UserInfoItem(
                        icon = Icons.Default.DateRange,
                        label = "Birthday",
                        value = userSettings?.birthdate ?: "N/A",
                        compact = isCompactScreen
                    )
                    
                    // Privacy Level
                    UserInfoItem(
                        icon = Icons.Default.Lock,
                        label = "Privacy",
                        value = userSettings?.privacyLevel ?: "N/A",
                        compact = isCompactScreen
                    )
                    
                    // Discoverable
                    UserInfoItem(
                        icon = Icons.Default.LocationOn,
                        label = "Discoverable",
                        value = if (userSettings?.discoverable == true) "TRUE" else "FALSE",
                        compact = isCompactScreen
                    )
                }
            
            }
        }
        
        // 加载指示器
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF636EF1)
            )
        }
        
        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun UserInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    compact: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = if (compact) 8.dp else 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF8E8E93), // 可见的灰色
            modifier = Modifier.size(if (compact) 22.dp else 28.dp) // 增大图标
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 标签
        Text(
            text = label,
            fontSize = if (compact) 16.sp else 18.sp, // 增大字体
            fontWeight = FontWeight.Medium,
            color = Color(0xFF505058), // 可见的深灰色
            modifier = Modifier.weight(1f)
        )
        
        // 值
        Text(
            text = value,
            fontSize = if (compact) 14.sp else 16.sp, // 增大字体
            fontWeight = FontWeight.Normal,
            color = Color(0xFF7B7D86) // 可见的灰色
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}
