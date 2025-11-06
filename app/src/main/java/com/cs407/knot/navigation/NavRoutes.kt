package com.cs407.knot.navigation

sealed class NavRoute(val route: String) {
    data object ChatList : NavRoute("chat_list")
    data object UserSettings : NavRoute("user_settings")
}