package com.cs407.knot.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.knot.ui.components.FloatingNav
import com.cs407.knot.ui.features.chatlist.ChatListScreen
import com.cs407.knot.ui.features.usersettings.UserSettingsScreen


@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    Box(Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = NavRoute.ChatList.route) {
            composable(NavRoute.ChatList.route) {
                ChatListScreen(onOpenMe = { nav.navigate(NavRoute.UserSettings.route) })
            }
            composable(NavRoute.UserSettings.route) {
                UserSettingsScreen(onBack = { nav.popBackStack() })
            }
        }

        FloatingNav(
            onMap = { /* TODO: nav.navigate(NavRoute.Map.route) */ },
            onChat = { nav.navigate(NavRoute.ChatList.route) },
            onUser = { nav.navigate(NavRoute.UserSettings.route) },
            onAddPost = { /* placeholder */ }
        )
    }
}