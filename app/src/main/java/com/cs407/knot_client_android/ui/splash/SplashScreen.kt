package com.cs407.knot_client_android.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cs407.knot_client_android.R
import com.cs407.knot_client_android.navigation.Screen
import kotlinx.coroutines.delay

/**
 * 启动页（Splash Screen）
 * 类似微信/TikTok 的启动体验：
 * 1. 显示品牌壁纸
 * 2. 后台进行自动登录检查
 * 3. 根据结果跳转到登录页或主页
 */
@Composable
fun SplashScreen(
    navController: NavHostController
) {
    val viewModel = viewModel<SplashViewModel>()
    val navigationTarget by viewModel.navigationTarget.collectAsState()
    
    // Logo 动画
    var animationStarted by remember { mutableStateOf(false) }
    
    val logoScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo scale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logo alpha"
    )
    
    val textAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 200, easing = LinearEasing),
        label = "text alpha"
    )
    
    // 启动动画和自动登录检查
    LaunchedEffect(Unit) {
        animationStarted = true
        // 最少显示 1 秒启动页，让动画播放完整
        delay(1000)
        viewModel.checkAutoLogin()
    }
    
    // 监听导航目标并跳转
    LaunchedEffect(navigationTarget) {
        when (navigationTarget) {
            NavigationTarget.Login -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            NavigationTarget.Main -> {
                navController.navigate(Screen.Main.createRoute("MAP")) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
            NavigationTarget.None -> {
                // 还在检查中，不跳转
            }
        }
    }
    
    // UI - 渐变背景 + Logo + 加载指示器
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F6F4), // 温暖米白
                        Color(0xFFF3F0FA), // 淡紫
                        Color(0xFFE8E6F8)  // 更深的紫
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / 品牌图标
            Box(
                modifier = Modifier
                    .size(135.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                // 这里可以放你的 App Logo
                // 暂时用一个圆形占位
                Image(
                    painter = painterResource(id = R.drawable.user_avatar), // 替换为你的 logo
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(48.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App 名称
            Text(
                text = "Knot",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D33),
                modifier = Modifier.alpha(textAlpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Slogan
            Text(
                text = "Connect with places",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6B7280),
                modifier = Modifier.alpha(textAlpha)
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // 加载指示器
            CircularProgressIndicator(
                modifier = Modifier
                    .size(32.dp)
                    .alpha(textAlpha),
                color = Color(0xFF636EF1),
                strokeWidth = 3.dp
            )
        }
        
        // 底部版本信息（可选）
        Text(
            text = "Version 1.0.0",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(textAlpha)
        )
    }
}

