package com.cs407.knot_client_android.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * MapPreferences 存储类
 * 负责保存和获取地图的最后位置和缩放级别
 */
class MapPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "map_preferences",
        Context.MODE_PRIVATE
    )
    
    /** 默认位置和缩放级别 */
    companion object {
        private const val KEY_LAST_LATITUDE = "last_latitude"
        private const val KEY_LAST_LONGITUDE = "last_longitude"
        private const val KEY_LAST_ZOOM = "last_zoom"
        
        // 默认位置：Mountain View (假数据中心区域)
        private const val DEFAULT_LATITUDE = 37.424
        private const val DEFAULT_LONGITUDE = -122.087
        private const val DEFAULT_ZOOM = 7.0
    }
    
    /** 保存地图的最后位置和缩放级别 */
    fun saveMapPosition(latitude: Double, longitude: Double, zoom: Double) {
        prefs.edit().apply {
            putFloat(KEY_LAST_LATITUDE, latitude.toFloat())
            putFloat(KEY_LAST_LONGITUDE, longitude.toFloat())
            putFloat(KEY_LAST_ZOOM, zoom.toFloat())
            apply()
        }
    }
    
    /** 获取地图的最后位置和缩放级别，如果不存在则返回默认值 */
    fun getLastLatitude(): Double {
        return prefs.getFloat(KEY_LAST_LATITUDE, DEFAULT_LATITUDE.toFloat()).toDouble()
    }
    
    /** 获取地图的最后经度，如果不存在则返回默认值 */
    fun getLastLongitude(): Double {
        return prefs.getFloat(KEY_LAST_LONGITUDE, DEFAULT_LONGITUDE.toFloat()).toDouble()
    }
    
    /** 获取地图的最后缩放级别，如果不存在则返回默认值 */
    fun getLastZoom(): Double {
        return prefs.getFloat(KEY_LAST_ZOOM, DEFAULT_ZOOM.toFloat()).toDouble()
    }
    
    /** 检查是否有保存的最后位置 */
    fun hasLastPosition(): Boolean {
        return prefs.contains(KEY_LAST_LATITUDE)
    }
}

