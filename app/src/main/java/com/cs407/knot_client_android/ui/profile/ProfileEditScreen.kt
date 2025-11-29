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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
//import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import com.cs407.knot_client_android.R
import com.cs407.knot_client_android.navigation.Screen
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.material.icons.filled.Create
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter

@Composable
fun ProfileEditScreen(
    navController: NavHostController
) {
    val profileVm = viewModel<ProfileViewModel>()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // 收集用户设置数据
    val userSettings by profileVm.userSettings.collectAsState()
    val isLoading by profileVm.loading.collectAsState()
    val error by profileVm.error.collectAsState()
    
    // 🎨 编辑状态
    var nickname by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var privacyLevel by remember { mutableStateOf("PUBLIC") }
    var discoverable by remember { mutableStateOf(true) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    
    // Dropdown 状态
    var genderExpanded by remember { mutableStateOf(false) }
    var privacyExpanded by remember { mutableStateOf(false) }
    
    // 页面首次显示时加载用户数据
    LaunchedEffect(Unit) {
        profileVm.loadUserSettings()
    }
    
    // 当用户数据加载完成时，初始化编辑字段
    LaunchedEffect(userSettings) {
        userSettings?.let { settings ->
            nickname = settings.nickname ?: ""
            statusMessage = settings.statusMessage ?: ""
            email = settings.email ?: ""
            gender = settings.gender ?: ""
            birthdate = settings.birthdate ?: ""
            privacyLevel = settings.privacyLevel ?: "PUBLIC"
            discoverable = settings.discoverable ?: true
            avatarUrl = settings.avatarUrl
        }
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

    // 头像选择器：从相册选择图片后，上传到 S3 并更新 avatarUrl
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val resolver = context.contentResolver
                    val type = resolver.getType(uri) ?: "image/jpeg"
                    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes == null) {
                        snackbarHostState.showSnackbar("Failed to read selected image")
                        return@launch
                    }
                    val uploadedUrl = profileVm.uploadAvatarToS3(bytes, type)
                    avatarUrl = uploadedUrl
                    snackbarHostState.showSnackbar("Avatar uploaded, don't forget to SAVE")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Failed to upload avatar: ${e.message}")
                }
            }
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
                            scope.launch {
                                val success = profileVm.updateUserSettings(
                                    nickname = nickname,
                                    statusMessage = statusMessage,
                                    email = email,
                                    gender = gender,
                                    birthdate = birthdate,
                                    privacyLevel = privacyLevel,
                                    discoverable = discoverable,
                                    avatarUrl = avatarUrl
                                )
                                
                                if (success) {
                                    snackbarHostState.showSnackbar("✅ Profile updated successfully")
                                    navController.navigate(Screen.Main.createRoute("PROFILE")) {
                                        popUpTo(Screen.Main.createRoute("PROFILE")) { inclusive = true }
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("❌ Failed to update profile")
                                }
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

            Spacer(modifier = Modifier.height(20.dp))
            
            // 头像 + 相机图标
            Box(
                modifier = Modifier
                    .size(140.dp),
                contentAlignment = Alignment.Center
                ) {
                    // 头像主体
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(width = 3.dp, color = Color.White.copy(alpha = 0.3f), shape = CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        val painter = if (avatarUrl.isNullOrBlank()) {
                            painterResource(id = R.drawable.user_avatar)
                        } else {
                            rememberAsyncImagePainter(model = avatarUrl)
                        }
                        Image(
                            painter = painter,
                            contentDescription = "Profile Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                
                // 📷 相机图标（右上角）
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF636EF1))
                        .border(2.dp, Color.White, CircleShape)
                        .clickable {
                            // 打开系统相册选择图片
                            avatarPickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = "Change Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 提示文字
            Text(
                text = "Edit Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D33)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 📱 可滚动的编辑表单
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nickname 输入框
                EditField(
                    icon = Icons.Default.Person,
                    label = "Nickname",
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "Enter your nickname"
                )
                
                // Status Message 输入框
                EditField(
                    icon = Icons.Default.Email,
                    label = "Status Message",
                    value = statusMessage,
                    onValueChange = { statusMessage = it },
                    placeholder = "What's on your mind?",
                    maxLines = 2
                )
                
                // Email 输入框
                EditField(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "your.email@example.com"
                )
                
                // Gender 下拉菜单
                Box {
                    EditFieldDropdown(
                        icon = Icons.Default.Face,
                        label = "Gender",
                        value = gender.ifEmpty { "Not set" },
                        onClick = { genderExpanded = true }
                    )
                    DropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false },
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        listOf("MALE", "FEMALE").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    genderExpanded = false
                                },
                            )
                        }
                    }
                }
                
                // Birthday 输入框
                EditField(
                    icon = Icons.Default.DateRange,
                    label = "Birthday",
                    value = birthdate,
                    onValueChange = { birthdate = it },
                    placeholder = "YYYY-MM-DD"
                )
                
                // Privacy Level 下拉菜单
                Box {
                    EditFieldDropdown(
                        icon = Icons.Default.Lock,
                        label = "Privacy Level",
                        value = privacyLevel,
                        onClick = { privacyExpanded = true }
                    )
                    DropdownMenu(
                        expanded = privacyExpanded,
                        onDismissRequest = { privacyExpanded = false },
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        listOf("PUBLIC", "PRIVATE", "FRIENDS").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    privacyLevel = option
                                    privacyExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Discoverable 开关
                EditFieldSwitch(
                    icon = Icons.Default.LocationOn,
                    label = "Discoverable",
                    checked = discoverable,
                    onCheckedChange = { discoverable = it }
                )
                
                Spacer(modifier = Modifier.height(40.dp))
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

// ===============================
// 🎨 可复用的编辑字段组件
// ===============================

/**
 * 普通文本输入框
 */
@Composable
private fun EditField(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    maxLines: Int = 1
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 标签
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF636EF1),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF505058)
            )
        }
        
        // 输入框
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFAAAAAA)) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.7f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                disabledContainerColor = Color.White.copy(alpha = 0.3f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color(0xFF2D2D33),
                unfocusedTextColor = Color(0xFF2D2D33)
            ),
            maxLines = maxLines,
            singleLine = maxLines == 1
        )
    }
}

/**
 * 下拉菜单字段（只读，点击显示下拉菜单）
 */
@Composable
private fun EditFieldDropdown(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 标签
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF636EF1),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF505058)
            )
        }
        
        // 下拉框
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.5f))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = Color(0xFF2D2D33)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = Color(0xFF8E8E93)
                )
            }
        }
    }
}

/**
 * 开关字段
 */
@Composable
private fun EditFieldSwitch(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF636EF1),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF505058)
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF636EF1),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCCCCCC)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    ProfileEditScreen(navController = rememberNavController())
}


