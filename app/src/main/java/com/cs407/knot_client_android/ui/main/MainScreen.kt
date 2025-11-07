package com.cs407.knot_client_android.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cs407.knot_client_android.ui.chat.ChatScreen
import com.cs407.knot_client_android.ui.components.BottomNavigationBar
import com.cs407.knot_client_android.ui.components.NavTab
import com.cs407.knot_client_android.ui.map.MapScreen
import com.cs407.knot_client_android.ui.profile.ProfileScreen

@Composable
fun MainScreen(
    navController: NavHostController
) {
    var selectedTab by remember { mutableStateOf(NavTab.MAP) }
    
    // 禁用侧滑返回
    BackHandler(enabled = true) {
        // 什么都不做，阻止返回到登录页
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 所有页面都保持存在，只是控制可见性
        // 这样 MapScreen 不会被销毁，避免每次重新加载地图
        
        // Map 页面 - 永远存在
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (selectedTab == NavTab.MAP) Modifier
                    else Modifier.alpha(0f)
                )
        ) {
            MapScreen(navController)
        }
        
        // Chat 页面 - 永远存在，但可能不可见
        if (selectedTab == NavTab.CHAT) {
            ChatScreen(navController)
        }
        
        // Profile 页面 - 永远存在，但可能不可见
        if (selectedTab == NavTab.PROFILE) {
            ProfileScreen(navController)
        }
        
        // 底部导航栏 - 统一管理，不会随页面切换而销毁
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 30.dp, bottom = 30.dp)
        )
    }
}

