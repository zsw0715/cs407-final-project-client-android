package com.cs407.knot_client_android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs407.knot_client_android.ui.splash.SplashScreen
import com.cs407.knot_client_android.ui.login.LoginScreen
import com.cs407.knot_client_android.ui.main.MainScreen
import com.cs407.knot_client_android.ui.friend.FriendScreen
import com.cs407.knot_client_android.ui.debug.DebugScreen
import com.cs407.knot_client_android.ui.profile.ProfileEditScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Main : Screen("main/{selectedTab}") {
        fun createRoute(selectedTab: String = "MAP") = "main/$selectedTab"
    }
    object Friend : Screen("friend")
    object Debug : Screen("debug")
    object ProfileEdit : Screen("profile_edit")
}

// 主要的 Navigation 设置函数
@Composable
fun SetupNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route  // 从启动页开始
    ) {
        // 启动页
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        // 登录页
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(
            route = Screen.Main.route,
            arguments = listOf(
                navArgument("selectedTab") {
                    type = NavType.StringType
                    defaultValue = "MAP"
                }
            )
        ) { backStackEntry ->
            val selectedTab = backStackEntry.arguments?.getString("selectedTab") ?: "MAP"
            MainScreen(
                navController = navController,
                initialTab = selectedTab
            )
        }
        composable(route = Screen.Friend.route) {
            FriendScreen(navController = navController)
        }
        composable(route = Screen.Debug.route) {
            DebugScreen(navController = navController)
        }
        composable(route = Screen.ProfileEdit.route) {
            ProfileEditScreen(navController = navController)
        }
    }
}