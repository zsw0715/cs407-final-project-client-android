package com.cs407.knot_client_android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FloatingActionButton(
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 如果没有图标，不显示按钮
    if (icon == null) return
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Apple-style 双阶段弹性动画
    val scale = remember { Animatable(1f) }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            // 按下：快速放大一点点
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(
                    durationMillis = 140, 
                    easing = LinearOutSlowInEasing
                )
            )
        } else {
            // 松手：先缩回一点再弹回 1
            scale.animateTo(
                targetValue = 0.88f,
                animationSpec = tween(
                    durationMillis = 135, 
                    easing = FastOutLinearInEasing
                )
            )
            // 然后自然回弹到 1
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    Box(
        modifier = modifier
    ) {
        // 毛玻璃背景层
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.7f))
                .blur(16.dp)
        )
        
        // 主按钮
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale.value)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE5E7EB), // gray-200
                    shape = CircleShape
                )
                .clip(CircleShape)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x33636EF1),
                    spotColor = Color(0x1A000000)
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.85f)
                        )
                    )
                )
                .clickable(
                    onClick = onClick,
                    indication = null,
                    interactionSource = interactionSource
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color(0xFF6B7280) // gray-600，与 BottomNavigationBar 未选中颜色一致
            )
        }
    }
}

