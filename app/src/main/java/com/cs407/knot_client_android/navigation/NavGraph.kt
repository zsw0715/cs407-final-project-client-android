package com.cs407.knot_client_android.navigation

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.cs407.knot_client_android.data.local.TokenStore
import com.cs407.knot_client_android.ui.chat.ChatDetailRoute
import com.cs407.knot_client_android.ui.debug.DebugScreen
import com.cs407.knot_client_android.ui.friend.FriendScreen
import com.cs407.knot_client_android.ui.login.LoginScreen
import com.cs407.knot_client_android.ui.main.MainScreen
import com.cs407.knot_client_android.ui.main.MainViewModel
import com.cs407.knot_client_android.ui.profile.ProfileEditScreen
import com.cs407.knot_client_android.ui.splash.SplashScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

/**
 * 定义应用的导航图
 * 注意！这里的是单独导航，而mapscreen，chatscreen，profilescreen等页面内的tab导航
 * 是在它们各自的文件里定义的
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Main : Screen("main/{selectedTab}") {
        fun createRoute(selectedTab: String = "MAP") = "main/$selectedTab"
    }
    object Friend : Screen("friend")
    object Debug : Screen("debug")
    object ProfileEdit : Screen("profile_edit")

    object ChatDetail : Screen("chat/{convId}/{title}") {
        fun createRoute(convId: Long, title: String): String {
            // title 里可能有空格/中文，要编码一下
            val encoded = Uri.encode(title)
            return "chat/$convId/$encoded"
        }
    }
}

// 主要的 Navigation 设置函数
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SetupNavGraph(
    navController: NavHostController = rememberAnimatedNavController()
) {
    AnimatedNavHost(
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
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("convId") { type = NavType.LongType },
                navArgument("title") { type = NavType.StringType }
            ),
            enterTransition = {
                // 从会话列表进入详情：新页面从右滑入
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(320)
                ) + fadeIn(animationSpec = tween(320))
            },
            exitTransition = {
                // 进入详情时，列表页明显左移并淡出，模拟微信 push 效果
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth / 3 },
                    animationSpec = tween(320)
                ) + fadeOut(animationSpec = tween(240))
            },
            popEnterTransition = {
                // 从详情返回：列表页从右侧淡入滑回（与下面 popExit 形成反向配合）
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth / 3 },
                    animationSpec = tween(320)
                ) + fadeIn(animationSpec = tween(320))
            },
            popExitTransition = {
                // 返回时，详情页向左滑出并淡出，让用户感觉“回到左边的列表页”
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(320)
                ) + fadeOut(animationSpec = tween(240))
            }
        ) { backStackEntry ->
            val convId = backStackEntry.arguments?.getLong("convId")!!
            val title = backStackEntry.arguments?.getString("title") ?: "Chat"

            // 从 NavGraph 作用域拿到 MainViewModel（里面有 WebSocket）
            val mainBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Main.route)
            }
            val mainVm: MainViewModel = viewModel(mainBackStackEntry)

            // 当前用户 id，优先从 TokenStore 获取，没有就用 0 兜底保证页面能正常展示
            val context = LocalContext.current
            val tokenStore = remember(context.applicationContext) {
                TokenStore(context.applicationContext)
            }
            val myUid: Long = tokenStore.getUserId() ?: 0L

            ChatDetailRoute(
                navController = navController,
                convId = convId,
                title = title,
                myUid = myUid,
                mainVm = mainVm
            )
        }
    }
}