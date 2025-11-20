package com.cs407.knot_client_android.data.model.response

/**
 * Mapbox Geocoding API 响应
 */
data class GeocodingResponse(
    val features: List<GeocodingFeature>?
)

/**
 * Geocoding 地理特征
 */
data class GeocodingFeature(
    val place_name: String?,
    val id: String? = null,
    val center: List<Double>? = null,
    val place_type: List<String>?,
    val text: String?
)

