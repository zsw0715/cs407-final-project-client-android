package com.cs407.knot_client_android.ui.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.knot_client_android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 导航目标
 */
enum class NavigationTarget {
    None,   // 还在检查中
    Login,  // 跳转到登录页
    Main    // 跳转到主页
}

/**
 * Splash Screen ViewModel
 * 负责自动登录检查和导航
 */
class SplashViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = AuthRepository(app, baseUrl = "http://10.0.2.2:8080")
    
    private val _navigationTarget = MutableStateFlow(NavigationTarget.None)
    val navigationTarget: StateFlow<NavigationTarget> = _navigationTarget
    
    /**
     * 检查自动登录
     * 根据结果设置导航目标
     */
    fun checkAutoLogin() {
        viewModelScope.launch {
            try {
                val success = repository.tryAutoLogin()
                _navigationTarget.value = if (success) {
                    NavigationTarget.Main
                } else {
                    NavigationTarget.Login
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 出错时跳转到登录页
                _navigationTarget.value = NavigationTarget.Login
            }
        }
    }
}

