package com.cs407.knot_client_android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.knot_client_android.data.model.CreateLocInfo
import com.cs407.knot_client_android.data.model.MapPostCreateMessage
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
    var selectedFriends by remember { mutableStateOf(emptyList<String>()) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var photos by remember { mutableStateOf(emptyList<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentMode by remember { mutableStateOf(SheetMode.FORM) }
    var showLocationPicker by remember { mutableStateOf(false) }

    // Mock 朋友列表（实际应从API获取）
    val friends = remember {
        listOf(
            Friend("1", "Jian"),
            Friend("2", "Luis"),
            Friend("3", "AG"),
            Friend("4", "JC"),
            Friend("5", "Len chen"),
            Friend("6", "sanltun soHo"),
            Friend("7", "aoyang"),
            Friend("8", "Nali atio"),
            Friend("9", "ka Kobaynshi")
        )
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
    var dragStartHeight by remember { mutableStateOf(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            animatedHeight.animateTo(
                targetValue = targetHeight.value,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            animatedHeight.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    fun snapToTarget() {
        coroutineScope.launch {
            val current = animatedHeight.value
            val threshold = targetHeight.value * 0.5f

            if (current < threshold) {
                animatedHeight.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 750,
                        easing = FastOutSlowInEasing
                    )
                )
                onDismiss()
            } else {
                animatedHeight.animateTo(
                    targetValue = targetHeight.value,
                    animationSpec = tween(
                        durationMillis = 750,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    LaunchedEffect(animatedHeight.value) {
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
                .height(animatedHeight.value.dp)
                .clip(RoundedCornerShape(topStart = 51.dp, topEnd = 51.dp))
                .background(Color(0xFFF8F6F4))
                .clickable(
                    enabled = true,
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
            ) {
                // 顶部标题栏（保持原有逻辑）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = { dragStartHeight = animatedHeight.value },
                                onDragEnd = { snapToTarget() },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    val newHeight = (animatedHeight.value - dragAmount).coerceIn(
                                        0f, targetHeight.value
                                    )
                                    coroutineScope.launch { animatedHeight.snapTo(newHeight) }
                                }
                            )
                        },
                ) {
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                        Box {
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

                // 内容区域 - 根据模式切换
                AnimatedContent(
                    targetState = currentMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "content_mode"
                ) { mode ->
                    when (mode) {
                        SheetMode.FORM -> {
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
                                onAddPhoto = { photos = photos + "new_photo_${photos.size + 1}" },
                                onRemoveLocation = { selectedLocation = null }
                            )
                        }
                        SheetMode.FRIEND_SELECTION -> {
                            FriendSelectionContent(
                                friends = friends,
                                selectedFriends = selectedFriends,
                                onFriendToggle = { friendId ->
                                    selectedFriends = if (selectedFriends.contains(friendId)) {
                                        selectedFriends - friendId
                                    } else if (selectedFriends.size < 3) {
                                        selectedFriends + friendId
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
                                    // 这里先简单用 Long.parseLong，如果你的 id 是 String，需要自己转换
                                    selectedFriends.mapNotNull { it.toLongOrNull() }
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
            placeholder = { Text("Enter title...") },
            shape = RoundedCornerShape(12.dp),
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
                .clickable { onLocationPickerRequest() },
            shape = RoundedCornerShape(12.dp),
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
            // 已添加的照片
            items(photos) { photoUrl ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE5E7EB))
                ) {
                    Text(
                        text = "Photo",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF9B9B9B)
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
                        .clickable(onClick = onAddPhoto),
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
    selectedFriends: List<String>,
    onFriendToggle: (String) -> Unit,
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
            items(friends) { friend ->
                val isSelected = selectedFriends.contains(friend.id)
                FriendSelectionItem(
                    friend = friend,
                    isSelected = isSelected,
                    onToggle = { onFriendToggle(friend.id) }
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
            .clickable(onClick = onToggle),
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
                            .clickable { onTypeSelected(ShareType.ALL_FRIENDS) },
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
                            .clickable { onTypeSelected(ShareType.SELECTED_FRIENDS) },
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
        shape = RoundedCornerShape(16.dp),
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