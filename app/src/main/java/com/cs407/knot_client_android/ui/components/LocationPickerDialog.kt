// ui/components/LocationPickerDialog.kt
package com.cs407.knot_client_android.ui.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cs407.knot_client_android.R
import com.cs407.knot_client_android.data.api.RetrofitProvider
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.style.GenericStyle
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

// 推荐地点数据类：用于列表展示
data class RecommendedLocation(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distance: String? = null
)

@OptIn(MapboxExperimental::class)
@Composable
fun LocationPickerDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onLocationSelected: (Location) -> Unit,
    initialLocation: Location? = null,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val context = LocalContext.current
    val mapboxToken = remember {
        context.getString(R.string.mapbox_access_token)
    }
    val geocodingApi = remember { RetrofitProvider.createGeocodingService() }

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    var selectedLocation by remember { mutableStateOf<Location?>(initialLocation) }

    var selectedPoint by remember {
        mutableStateOf<Point?>(
            initialLocation?.let {
                Point.fromLngLat(it.longitude, it.latitude)
            }
        )
    }

    var searchResults by remember { mutableStateOf<List<RecommendedLocation>>(emptyList()) }

    var nearbyRecommendations by remember { mutableStateOf<List<RecommendedLocation>>(emptyList()) }

    var isSearching by remember { mutableStateOf(false) }
    var isLoadingNearby by remember { mutableStateOf(false) }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            val initPoint = selectedPoint ?: Point.fromLngLat(-122.4194, 37.7749)
            center(initPoint)
            zoom(14.0)
        }
    }

    /**
     * 1️⃣ 正向 geocoding：搜索地点
     */
    LaunchedEffect(searchQuery.text) {
        val query = searchQuery.text.trim()
        if (query.isEmpty()) {
            searchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }

        isSearching = true
        delay(400)

        try {
            Log.d(
                "LocationPicker",
                "searchPlaces request query=\"$query\""
            )

            val response = geocodingApi.searchPlaces(
                query = query,
                accessToken = mapboxToken,
                limit = 8
            )

            val features = response.features ?: emptyList()
            Log.d("LocationPicker", "searchPlaces features count = ${features.size}")

            searchResults = features.mapIndexed { index, f ->
                val lng = f.center?.getOrNull(0) ?: -122.4194
                val lat = f.center?.getOrNull(1) ?: 37.7749

                RecommendedLocation(
                    id = f.id ?: "search_$index",
                    name = f.text ?: "Unnamed place",
                    address = f.place_name ?: (f.text ?: "Unknown address"),
                    latitude = lat,
                    longitude = lng
                )
            }
        } catch (e: CancellationException) {
            // 被新的 query 或关闭 Dialog 取消，属正常情况
            throw e
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(
                "LocationPicker",
                "searchPlaces HttpException: code=${e.code()}, body=$errorBody",
                e
            )
            searchResults = emptyList()
        } catch (e: Exception) {
            Log.e("LocationPicker", "searchPlaces error", e)
            searchResults = emptyList()
        } finally {
            isSearching = false
        }
    }

    /**
     * 2️⃣ 监听 pin 位置变化，做 reverse geocoding，刷新“附近推荐”
     */
    LaunchedEffect(selectedPoint) {
        val point = selectedPoint ?: return@LaunchedEffect

        isLoadingNearby = true
        delay(500)

        try {
            Log.d(
                "LocationPicker",
                "reverseGeocode request lon=${point.longitude()}, lat=${point.latitude()}"
            )

            val response = geocodingApi.reverseGeocode(
                longitude = point.longitude(),
                latitude = point.latitude(),
                accessToken = mapboxToken
            )

            val features = response.features ?: emptyList()
            Log.d("LocationPicker", "reverseGeocode features count = ${features.size}")

            nearbyRecommendations = features.mapIndexed { index, f ->
                val lng = f.center?.getOrNull(0) ?: point.longitude()
                val lat = f.center?.getOrNull(1) ?: point.latitude()

                RecommendedLocation(
                    id = f.id ?: "nearby_$index",
                    name = f.text ?: "Nearby place",
                    address = f.place_name ?: (f.text ?: "Unknown address"),
                    latitude = lat,
                    longitude = lng
                )
            }

            if (selectedLocation == null && nearbyRecommendations.isNotEmpty()) {
                val first = nearbyRecommendations.first()
                selectedLocation = Location(
                    id = first.id,
                    name = first.name,
                    address = first.address,
                    latitude = first.latitude,
                    longitude = first.longitude
                )
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(
                "LocationPicker",
                "reverseGeocode HttpException: code=${e.code()}, body=$errorBody",
                e
            )
            nearbyRecommendations = emptyList()
        } catch (e: Exception) {
            Log.e("LocationPicker", "reverseGeocode error", e)
            nearbyRecommendations = emptyList()
        } finally {
            isLoadingNearby = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF8F6F4)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose Location",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1C1B1F)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search for a place or address...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Search")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color(0xFF636EF1),
                        unfocusedIndicatorColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8EAF6))
                ) {
                    MapboxMap(
                        modifier = Modifier.fillMaxSize(),
                        mapViewportState = mapViewportState,
                        style = {
                            GenericStyle(style = Style.MAPBOX_STREETS)
                        },
                        onMapClickListener = OnMapClickListener { point ->
                            selectedPoint = point
                            selectedLocation = Location(
                                id = "manual_${point.latitude()}_${point.longitude()}",
                                name = "Dropped pin",
                                address = "Custom location",
                                latitude = point.latitude(),
                                longitude = point.longitude()
                            )
                            true
                        }
                    ) {
                        selectedPoint?.let { point ->
                            PointAnnotation(point = point)
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Pin",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                selectedLocation?.let { location ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF636EF1).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFF636EF1).copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                "Selected",
                                tint = Color(0xFF636EF1)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = location.name,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = location.address,
                                    fontSize = 14.sp,
                                    color = Color(0xFF9B9B9B)
                                )
                                Text(
                                    text = "${String.format("%.6f", location.latitude)}, " +
                                            "${String.format("%.6f", location.longitude)}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9B9B9B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val locationsToShow = if (searchQuery.text.isNotBlank()) {
                    searchResults
                } else {
                    nearbyRecommendations
                }

                val listTitle = if (searchQuery.text.isNotBlank()) {
                    "Search Results"
                } else {
                    "Places near pin"
                }

                Text(
                    text = listTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (isSearching || (isLoadingNearby && searchQuery.text.isBlank())) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF636EF1)
                            )
                        }
                    } else {
                        if (locationsToShow.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val msg = if (searchQuery.text.isNotBlank()) {
                                    "No results for \"${searchQuery.text}\""
                                } else {
                                    "No nearby places found."
                                }

                                Text(
                                    text = msg,
                                    fontSize = 14.sp,
                                    color = Color(0xFF9B9B9B)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(locationsToShow) { loc ->
                                    RecommendedLocationItem(
                                        location = loc,
                                        onClick = {
                                            val selectedLoc = Location(
                                                id = loc.id,
                                                name = loc.name,
                                                address = loc.address,
                                                latitude = loc.latitude,
                                                longitude = loc.longitude
                                            )
                                            selectedLocation = selectedLoc
                                            selectedPoint = Point.fromLngLat(
                                                loc.longitude,
                                                loc.latitude
                                            )

                                            mapViewportState.flyTo(
                                                CameraOptions.Builder()
                                                    .center(selectedPoint)
                                                    .zoom(16.0)
                                                    .build(),
                                                MapAnimationOptions.mapAnimationOptions {
                                                    duration(1000)
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        selectedLocation?.let { onLocationSelected(it) }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF636EF1)
                    ),
                    enabled = selectedLocation != null
                ) {
                    Text(
                        text = "Use This Location",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedLocationItem(
    location: RecommendedLocation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Place,
                "Place",
                tint = Color(0xFF636EF1)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.name,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = location.address,
                    fontSize = 14.sp,
                    color = Color(0xFF9B9B9B)
                )
            }
            location.distance?.let { distance ->
                Text(
                    text = distance,
                    fontSize = 14.sp,
                    color = Color(0xFF9B9B9B)
                )
            }
        }
    }
}
