package com.cs407.knot_client_android.ui.login

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun LoginScreen(
    navController: NavHostController
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) } // true = Login, false = Register
    
    // 背景色 #F8F6F4 - 更温暖的米白色
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6F4)),
        contentAlignment = BiasAlignment(0f, -0.15f) // 稍微往上偏移，视觉中心
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // 主标题 - 更大更粗
            Text(
                text = "Knot",
                fontSize = 62.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2C2C2C),
                letterSpacing = (-1.2).sp
            )
            
            // 副标题 - 更轻更优雅
            Text(
                text = "Where friendships leave their marks.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF7A7A7A),
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                letterSpacing = 0.4.sp
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Username 标签
            Text(
                text = "Username",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A4A4A),
                modifier = Modifier.padding(bottom = 10.dp),
                letterSpacing = 0.5.sp
            )
            
            // Username 输入框 - 优化圆角和颜色
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "Enter your username", 
                        color = Color(0xFFAAAAAA),
                        fontSize = 15.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Username",
                        tint = Color(0xFF9B8FD9).copy(alpha = 0.7f) // 图标调淡
                    )
                },
                shape = RoundedCornerShape(18.dp), // 更大圆角
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color(0xFFB5A8FF), // 淡紫色
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Password 标签
            Text(
                text = "Password",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A4A4A),
                modifier = Modifier.padding(bottom = 10.dp),
                letterSpacing = 0.5.sp
            )
            
            // Password 输入框 - 优化圆角和颜色
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "Enter your password", 
                        color = Color(0xFFAAAAAA),
                        fontSize = 15.sp
                    ) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = Color(0xFF9B8FD9).copy(alpha = 0.7f) // 图标调淡
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(18.dp), // 更大圆角
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color(0xFFB5A8FF), // 淡紫色
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(52.dp))
            
            // Toggle Switch (Login / Register)
            CustomToggle(
                isLogin = isLogin,
                onToggleChange = { isLogin = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action Button - 渐变效果
            Button(
                onClick = { 
                    if (isLogin) {
                        // TODO: Login logic
                    } else {
                        // TODO: Register logic
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(18.dp), // 更大圆角
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp) // 移除默认内边距，保持与 toggle 一致
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFB5A8FF),
                                    Color(0xFF9B8FD9)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLogin) "Login" else "Register",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CustomToggle(
    isLogin: Boolean,
    onToggleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFFE8E8E8))
    ) {
        val totalWidth = maxWidth
        val halfWidth = totalWidth / 2
        
        val indicatorOffset by animateDpAsState(
            targetValue = if (isLogin) 0.dp else halfWidth,
            animationSpec = tween(durationMillis = 300),
            label = "indicator offset"
        )
        
        // Sliding indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(halfWidth)
                .height(50.dp)
                .padding(4.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(Color.White)
        )
        
        // Toggle options
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Login option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        onClick = { onToggleChange(true) },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Login",
                    fontSize = 15.sp,
                    fontWeight = if (isLogin) FontWeight.Bold else FontWeight.Normal,
                    color = if (isLogin) Color(0xFF2C2C2C) else Color(0xFF888888)
                )
            }
            
            // Register option
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        onClick = { onToggleChange(false) },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Register",
                    fontSize = 15.sp,
                    fontWeight = if (!isLogin) FontWeight.Bold else FontWeight.Normal,
                    color = if (!isLogin) Color(0xFF2C2C2C) else Color(0xFF888888)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}