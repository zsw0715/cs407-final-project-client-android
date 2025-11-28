package com.cs407.knot_client_android.data.api

import com.cs407.knot_client_android.data.model.response.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Mapbox Geocoding API 服务接口
 */
interface GeocodingApiService {
    /**
     * 反向地理编码：根据经纬度获取地名
     */
    @GET("geocoding/v5/mapbox.places/{longitude},{latitude}.json")
    suspend fun reverseGeocode(
        @Path("longitude") longitude: Double,
        @Path("latitude") latitude: Double,
        @Query("access_token") accessToken: String,
//        @Query("limit") limit: Int = 5,
//        @Query("types") types: String = "poi,address,place"
    ): GeocodingResponse

    /**
     * 正向地理编码：根据关键字搜索地点名称
     * 例如 “Starbucks”、“library” 之类
     */
    @GET("geocoding/v5/mapbox.places/{query}.json")
    suspend fun searchPlaces(
        @Path("query") query: String,
        @Query("access_token") accessToken: String,
        // 可选：用当前地图中心作为搜索偏好点，会优先返回附近的结果
        @Query("proximity") proximity: String? = null,   // "lng,lat"
        @Query("limit") limit: Int = 8,
//        @Query("types") types: String = "poi"
    ): GeocodingResponse
}

