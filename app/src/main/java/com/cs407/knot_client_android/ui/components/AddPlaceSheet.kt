package com.cs407.knot_client_android.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cs407.knot_client_android.data.model.CreateLocInfo
import com.cs407.knot_client_android.data.model.MapPostCreateMessage
import com.cs407.knot_client_android.data.repository.UserRepository
import com.mapbox.geojson.Point
import kotlinx.coroutines.launch
import java.util.UUID

data class Friend(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)

data class Location(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

enum class ShareType {
    ALL_FRIENDS,
    SELECTED_FRIENDS
}

enum class SheetMode {
    FORM,
    FRIEND_SELECTION
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AddPlaceSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onPostKnot: (MapPostCreateMessage) -> Unit,
    onLocationPickerRequest: () -> Unit = {},
    currentUserLocation: Point?,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val targetHeight = screenHeight * 0.92f
    val coroutineScope = rememberCoroutineScope()

    // 状态管理
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var shareType by remember { mutableStateOf(ShareType.ALL_FRIENDS) }
    // 用索引而不是 id 来记录选中的好友，避免后端 userId 异常导致全部高亮
    var selectedFriends by remember { mutableStateOf(emptyList<Int>()) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var photos by remember { mutableStateOf(emptyList<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentMode by remember { mutableStateOf(SheetMode.FORM) }
    var showLocationPicker by remember { mutableStateOf(false) }

    // API & Token
    val context = LocalContext.current
    val tokenStore = remember { com.cs407.knot_client_android.data.local.TokenStore(context) }
    val friendApiService = remember {
        com.cs407.knot_client_android.data.api.RetrofitProvider.createFriendService("http://10.0.101.215:8080")
    }
    val userRepository = remember {
        UserRepository(context.applicationContext, baseUrl = "http://10.0.101.215:8080")
    }

    // 朋友列表状态
    var friends by remember { mutableStateOf(emptyList<Friend>()) }

    // 加载朋友列表
    LaunchedEffect(Unit) {
        val token = tokenStore.getAccessToken()
        if (token != null) {
            try {
                val response = friendApiService.getFriends("Bearer $token")
                if (response.success && response.data != null) {
                    friends = response.data.map { summary ->
                        Friend(
                            id = summary.userId.toString(),
                            name = summary.username,
                            avatarUrl = summary.avatar
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 照片选择器：从相册中选择后，上传到 S3 并把 URL 填入 photos 列表
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val resolver = context.contentResolver
                    val type = resolver.getType(uri) ?: "image/jpeg"
                    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes == null) {
                        // 简单失败提示（可按需改成 Snackbar）
                        return@launch
                    }
                    // 复用头像上传的 S3 逻辑，这里同样返回公开 URL
                    val uploadedUrl = userRepository.uploadAvatarToS3(bytes, type)
                    photos = photos + uploadedUrl
                } catch (_: Exception) {
                    // 失败时先静默，避免打断主流程
                }
            }
        }
    }

    // 同步 shareType 和 mode
    LaunchedEffect(shareType) {
        currentMode = if (shareType == ShareType.ALL_FRIENDS) {
            SheetMode.FORM
        } else {
            SheetMode.FRIEND_SELECTION
        }
    }

    // 动画高度
    val animatedHeight = remember { Animatable(0f) }
    
    // 记录拖动起始高度
    var dragStartHeight by remember { mutableStateOf(0f) }
    
    // 监听 isVisible 变化，触发动画
    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedHeight.animateTo(
                targetValue = targetHeight.value,
                animationSpec = spring(
                    dampingRatio = 0.75f, // 更高的 dampingRatio，更快更稳
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            animatedHeight.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.75f, // 快速下滑
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    // 拖动结束后的处理
    fun snapToTarget() {
        coroutineScope.launch {
            val current = animatedHeight.value
            val threshold = targetHeight.value * 0.5f // 如果拖动超过 50%，则关闭
            
            if (current < threshold) {
                // 关闭 sheet
                animatedHeight.animateTo(
//                    targetValue = 0f,
//                    animationSpec = spring(
//                        dampingRatio = 0.75f, // 快速下滑
//                        stiffness = Spring.StiffnessLow
//                    )
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 750, // 从200~500之间调节速度
                        easing = FastOutSlowInEasing
                    )
                )
                // 动画结束后通知外部关闭
                onDismiss()
            } else {
                // 回弹到原位
                animatedHeight.animateTo(
                    // targetValue = targetHeight.value,
                    // animationSpec = spring(
                    //     dampingRatio = 0.75f, // 快速回弹
                    //     stiffness = Spring.StiffnessLow
                    // )
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 750, // 从200~500之间调节速度
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }
    
    // 监听动画高度，当接近 0 时自动同步状态
    LaunchedEffect(animatedHeight.value) {
        // 如果高度已经很小（< 5dp），认为已关闭，同步状态
        if (animatedHeight.value < 5f && isVisible) {
            onDismiss()
        }
    }

    LocationPickerDialog(
        isVisible = showLocationPicker,
        onDismiss = { showLocationPicker = false },
        onLocationSelected = { location ->
            selectedLocation = location
            showLocationPicker = false
        },
        initialLocation = selectedLocation
            ?: currentUserLocation?.let { point ->
                Location(
                    id = "device",
                    name = "Current Location",
                    address = "Current Location",
                    latitude = point.latitude(),
                    longitude = point.longitude()
                )
            }
    )

    if (animatedHeight.value > 0f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(0.dp)
                .height(animatedHeight.value.dp)
                .clip(RoundedCornerShape(51.0f.dp)) // 更大的圆角
                .background(Color(0xFFF8F6F4)) // 米黄色，不透明
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
                    .padding(top = 0.dp)
                    .padding(horizontal = 28.dp)
            ) {
                // 顶部拖动指示器 - 可以拖动关闭
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
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
                                    
                                    // 实时跟随手指，只允许向下拖（减小高度）
                                    val newHeight = (animatedHeight.value - dragAmount).coerceIn(
                                        0f, targetHeight.value
                                    )
                                    coroutineScope.launch {
                                        animatedHeight.snapTo(newHeight)
                                    }
                                }
                            )
                        },
                ) {
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DROP A KNOT",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1B1F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Mark your favorite location on the map 📍",
                                fontSize = 14.sp,
                                color = Color(0xFF9B9B9B),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 关闭按钮
                        val buttonInteractionSource = remember { MutableInteractionSource() }
                        val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
                        val buttonScale = remember { Animatable(1f) }

                        LaunchedEffect(isButtonPressed) {
                            if (isButtonPressed) {
                                buttonScale.animateTo(
                                    targetValue = 1.2f,
                                    animationSpec = tween(170, easing = LinearOutSlowInEasing)
                                )
                            } else {
                                buttonScale.animateTo(
                                    targetValue = 0.88f,
                                    animationSpec = tween(155, easing = FastOutLinearInEasing)
                                )
                                buttonScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }

                        Box(modifier = Modifier.offset(y = (-12).dp)) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .graphicsLayer {
                                        renderEffect = android.graphics.RenderEffect
                                            .createBlurEffect(40f, 40f, android.graphics.Shader.TileMode.CLAMP)
                                            .asComposeRenderEffect()
                                    }
                                    .background(Color.White.copy(alpha = 0.65f))
                            )

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .scale(buttonScale.value)
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
                                        onClick = onDismiss,
                                        indication = null,
                                        interactionSource = buttonInteractionSource
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                    tint = if (isButtonPressed) Color(0xFF636EF1) else Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.weight(1f)      // 仍然占据中间剩余空间
                ) {
                    // 内容区域 - 根据模式切换
                    AnimatedContent(
                        targetState = currentMode,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with
                                fadeOut(animationSpec = tween(300))
                        },
                        label = "content_mode"
                    ) { mode ->
                        when (mode) {
                            SheetMode.FORM -> {
                                val scrollState = rememberScrollState()

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                ) {
                                    FormContent(
                                        title = title,
                                        onTitleChange = { title = it },
                                        selectedLocation = selectedLocation,
                                        onLocationPickerRequest = {
                                            // 触发位置选择器显示
                                            showLocationPicker = true
                                        },
                                        description = description,
                                        onDescriptionChange = { description = it },
                                        photos = photos,
                                        onAddPhoto = {
                                            // 打开系统相册选择图片
                                            photoPickerLauncher.launch("image/*")
                                        },
                                        onRemoveLocation = { selectedLocation = null }
                                    )
                                }
                            }

                            SheetMode.FRIEND_SELECTION -> {
                                FriendSelectionContent(
                                    friends = friends,
                                    selectedFriends = selectedFriends,
                                    onFriendToggle = { index ->
                                        selectedFriends = if (selectedFriends.contains(index)) {
                                            selectedFriends - index
                                        } else if (selectedFriends.size < 3) {
                                            selectedFriends + index
                                        } else {
                                            selectedFriends
                                        }
                                    },
                                    maxFriends = 3
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                }

                // SHARE WITH 滑动选择器
                ShareTypeSlider(
                    selectedType = shareType,
                    onTypeSelected = { shareType = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Post knot 按钮
                PostButton(
                    isEnabled = title.isNotBlank() && selectedLocation != null && !isLoading,
                    isLoading = isLoading,
                    shareType = shareType,
                    selectedFriendsCount = selectedFriends.size,
                    onClick = {
                        if (title.isNotBlank() && selectedLocation != null) {
                            isLoading = true

                            // 解析 mediaJson → mediaUrls / memberIds
                            val mediaUrls: List<String>? =
                                if (photos.isNotEmpty()) photos.toList() else null

                            val allFriends = shareType == ShareType.ALL_FRIENDS

                            val memberIdLongs: List<Long>? =
                                if (!allFriends && selectedFriends.isNotEmpty()) {
                                    // 根据选中索引映射到对应好友的 userId
                                    selectedFriends.mapNotNull { idx ->
                                        friends.getOrNull(idx)?.id?.toLongOrNull()
                                    }
                                } else null

                            val uiMsg = MapPostCreateMessage(
                                clientReqId = java.util.UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                mediaUrls = mediaUrls,
                                loc = CreateLocInfo(
                                    lat = selectedLocation!!.latitude,
                                    lng = selectedLocation!!.longitude,
                                    name = selectedLocation!!.name
                                ),
                                allFriends = allFriends,
                                memberIds = memberIdLongs
                            )

                            onPostKnot(uiMsg)

                            // 本地状态重置：清空表单和图片，回到初始状态
                            title = ""
                            description = ""
                            selectedLocation = null
                            photos = emptyList()
                            shareType = ShareType.ALL_FRIENDS
                            selectedFriends = emptyList()
                            isLoading = false
                        }
                    }
                )


                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FormContent(
    title: String,
    onTitleChange: (String) -> Unit,
    selectedLocation: Location?,
    onLocationPickerRequest: () -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    photos: List<String>,
    onAddPhoto: () -> Unit,
    onRemoveLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // TITLE 输入
        Text(
            text = "TITLE",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9B9B9B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Enter title...") },
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF636EF1),
                unfocusedIndicatorColor = Color(0xFFE5E7EB)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 地点选择器（点击打开新窗口）
        Text(
            text = "SELECT LOCATION",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9B9B9B)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 位置选择按钮（替代原来的搜索框）
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onLocationPickerRequest() }
                ),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF636EF1),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedLocation?.name ?: "Drop the pin or search by name",
                    color = if (selectedLocation != null) Color.Black else Color(0xFF9B9B9B),
                    fontSize = 16.sp
                )
            }
        }

        // 已选位置显示
        selectedLocation?.let { location ->
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF636EF1).copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Color(0xFF636EF1).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF636EF1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = location.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = location.address,
                            fontSize = 14.sp,
                            color = Color(0xFF9B9B9B)
                        )
                    }
                    IconButton(onClick = onRemoveLocation) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove location",
                            tint = Color(0xFF9B9B9B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DESCRIPTION 输入
        Text(
            text = "DESCRIPTION",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9B9B9B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("Share your experience...") },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF636EF1),
                unfocusedIndicatorColor = Color(0xFFE5E7EB)
            ),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PHOTOS 部分
        Text(
            text = "PHOTOS",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9B9B9B)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 已添加的照片预览
            items(photos) { photoUrl ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE5E7EB))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = photoUrl),
                        contentDescription = "Selected photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 添加按钮
            item {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(2.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAddPhoto
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 32.sp,
                        color = Color(0xFF9B9B9B),
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendSelectionContent(
    friends: List<Friend>,
    selectedFriends: List<Int>,
    onFriendToggle: (Int) -> Unit,
    maxFriends: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 选择提示
        Text(
            text = "Select friends to share with",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${selectedFriends.size}/$maxFriends selected",
            fontSize = 14.sp,
            color = Color(0xFF9B9B9B)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 朋友网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(friends.size) { index ->
                val friend = friends[index]
                val isSelected = selectedFriends.contains(index)
                FriendSelectionItem(
                    friend = friend,
                    isSelected = isSelected,
                    onToggle = { onFriendToggle(index) }
                )
            }
        }
    }
}

@Composable
private fun FriendSelectionItem(
    friend: Friend,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color(0xFF636EF1).copy(alpha = 0.2f)
                    else Color(0xFFE5E7EB)
                )
                .border(
                    width = 3.dp,
                    color = if (isSelected) Color(0xFF636EF1) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = friend.name.take(1).uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF636EF1) else Color(0xFF9B9B9B)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = friend.name,
            fontSize = 14.sp,
            maxLines = 1,
            color = if (isSelected) Color.Black else Color(0xFF6B7280)
        )
    }
}

@Composable
private fun ShareTypeSlider(
    selectedType: ShareType,
    onTypeSelected: (ShareType) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val animatedSelection = remember { Animatable(0f) }

    LaunchedEffect(selectedType) {
        animatedSelection.animateTo(
            targetValue = if (selectedType == ShareType.ALL_FRIENDS) 0f else 1f,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {

            BoxWithConstraints(                       // CHANGED: 外面用 BoxWithConstraints 包一层
                modifier = Modifier.fillMaxSize()
            ) {
                val density = LocalDensity.current    // CHANGED: 拿到当前 density

                // 计算平移距离: animatedSelection * (maxWidth * 0.5)
                val offsetX = animatedSelection.value *
                        with(density) { (maxWidth * 0.5f).toPx() }   // CHANGED: 在作用域里用 maxWidth 并转 px

                // 滑动背景指示器
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .graphicsLayer(
                            translationX = offsetX              // CHANGED: 用上面算好的 offsetX
                        )
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                )

                // 两个选项
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTypeSelected(ShareType.ALL_FRIENDS) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All friends",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedType == ShareType.ALL_FRIENDS) Color(0xFF636EF1) else Color(0xFF6B7280)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTypeSelected(ShareType.SELECTED_FRIENDS) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selected friends",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedType == ShareType.SELECTED_FRIENDS) Color(0xFF636EF1) else Color(0xFF6B7280)
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun PostButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    shareType: ShareType,
    selectedFriendsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF636EF1),
            disabledContainerColor = Color(0xFF9B9B9B).copy(alpha = 0.3f)
        ),
        enabled = isEnabled && (shareType != ShareType.SELECTED_FRIENDS || selectedFriendsCount > 0),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = when {
                    shareType == ShareType.SELECTED_FRIENDS && selectedFriendsCount == 0 -> "Select at least 1 friend"
                    shareType == ShareType.SELECTED_FRIENDS -> "Share with $selectedFriendsCount friends"
                    else -> "Post knot"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}