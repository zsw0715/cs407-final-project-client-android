package com.cs407.knot_client_android.utils

import android.util.Base64
import org.json.JSONObject

/**
 * JWT Token 工具类
 */
object JwtUtils {
    
    /**
     * 检查 JWT token 是否过期
     * @param token JWT token 字符串
     * @return true 如果已过期，false 如果未过期
     */
    fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrBlank()) return true
        
        try {
            val parts = token.split(".")
            if (parts.size != 3) return true
            
            // 解码 payload (第二部分)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes)
            val json = JSONObject(decodedString)
            
            // 获取过期时间 (exp 字段，单位：秒)
            if (!json.has("exp")) return true
            val exp = json.getLong("exp")
            
            // 当前时间 (秒)
            val now = System.currentTimeMillis() / 1000
            
            // 如果当前时间 >= 过期时间，则已过期
            return now >= exp
        } catch (e: Exception) {
            e.printStackTrace()
            return true // 解析失败视为过期
        }
    }
    
    /**
     * 从 JWT token 中提取 userId
     * @param token JWT token 字符串
     * @return userId 或 null
     */
    fun extractUserId(token: String?): Int? {
        if (token.isNullOrBlank()) return null
        
        try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes)
            val json = JSONObject(decodedString)
            
            // 尝试获取 userId (可能是 "userId", "uid", "sub" 等)
            return when {
                json.has("userId") -> json.getInt("userId")
                json.has("uid") -> json.getInt("uid")
                json.has("sub") -> json.getString("sub").toIntOrNull()
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * 获取 token 剩余有效时间（秒）
     * @param token JWT token 字符串
     * @return 剩余秒数，如果已过期或解析失败返回 0
     */
    fun getTokenRemainingTime(token: String?): Long {
        if (token.isNullOrBlank()) return 0
        
        try {
            val parts = token.split(".")
            if (parts.size != 3) return 0
            
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes)
            val json = JSONObject(decodedString)
            
            if (!json.has("exp")) return 0
            val exp = json.getLong("exp")
            val now = System.currentTimeMillis() / 1000
            
            val remaining = exp - now
            return if (remaining > 0) remaining else 0
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }
}

