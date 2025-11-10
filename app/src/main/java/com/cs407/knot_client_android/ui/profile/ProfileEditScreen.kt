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
import kotlinx.coroutines.launch

@Composable
fun ProfileEditScreen(
    navController: NavHostController
) {
    val profileVm = viewModel<ProfileViewModel>()
    val scope = rememberCoroutineScope()
    
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
            
            // 按钮行 - BACK 左对齐，SAVE 右对齐
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // BACK 按钮 - 左对齐，带动画
                val backInteractionSource = remember { MutableInteractionSource() }
                val backIsPressed by backInteractionSource.collectIsPressedAsState()
                val backScale = remember { Animatable(1f) }
                
                LaunchedEffect(backIsPressed) {
                    if (backIsPressed) {
                        backScale.animateTo(
                            targetValue = 1.2f,
                            animationSpec = tween(
                                durationMillis = 170,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    } else {
                        backScale.animateTo(
                            targetValue = 0.88f,
                            animationSpec = tween(
                                durationMillis = 155,
                                easing = FastOutLinearInEasing
                            )
                        )
                        backScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }
                
                // BACK 返回 Profile 页面按钮容器 - 仿照 FloatingActionButton 的毛玻璃实现
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
                            text = "BACK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Transparent,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 9.dp)
                        )
                    }
                    
                    // 主按钮层 - 在毛玻璃背景之上，按中心放大
                    Button(
                        onClick = { 
                            navController.navigate(Screen.Main.createRoute("PROFILE")) {
                                popUpTo(Screen.Main.createRoute("PROFILE")) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .scale(backScale.value),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(42.dp),
                        interactionSource = backInteractionSource
                    ) {
                        Text(
                            text = "BACK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF444444)
                        )
                    }
                }
                
                // SAVE 按钮 - 右对齐，带动画
                val saveInteractionSource = remember { MutableInteractionSource() }
                val saveIsPressed by saveInteractionSource.collectIsPressedAsState()
                val saveScale = remember { Animatable(1f) }
                
                LaunchedEffect(saveIsPressed) {
                    if (saveIsPressed) {
                        saveScale.animateTo(
                            targetValue = 1.2f,
                            animationSpec = tween(
                                durationMillis = 170,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    } else {
                        saveScale.animateTo(
                            targetValue = 0.88f,
                            animationSpec = tween(
                                durationMillis = 155,
                                easing = FastOutLinearInEasing
                            )
                        )
                        saveScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                }

                // SAVE 按钮容器 - 仿照 FloatingActionButton 的毛玻璃实现
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
                            text = "SAVE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Transparent,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 9.dp)
                        )
                    }
                    
                    // 主按钮层 - 在毛玻璃背景之上，按中心放大
                    Button(
                        onClick = { 
                            // TODO: 处理保存事件

                            navController.navigate(Screen.Main.createRoute("PROFILE")) {
                                popUpTo(Screen.Main.createRoute("PROFILE")) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .scale(saveScale.value),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(42.dp),
                        interactionSource = saveInteractionSource
                    ) {
                        Text(
                            text = "SAVE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF444444)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))
            
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .border(width = 2.dp, color = Color.White.copy(alpha = 0.15f), shape = CircleShape)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                // user avatar
                Image(
                    painter = painterResource(id = R.drawable.user_avatar),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🧾 名称
            Text(
                text = userSettings?.nickname ?: "Loading...",
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
                    Text(text = "This is a Profile Edit page", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF444444))
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

@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    ProfileEditScreen(navController = rememberNavController())
}

