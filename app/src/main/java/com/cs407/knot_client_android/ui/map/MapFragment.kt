package com.cs407.knot_client_android.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cs407.knot_client_android.R
import com.google.android.gms.location.LocationServices
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
// v11 uses built-in Marker API
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.IconFactory

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var _mapView: MapView? = null
    private val mapView get() = _mapView!!
    private var maplibreMap: MapLibreMap? = null

    // TODO: Backend - Get real user data from server
    private val currentUserId: String = "user123" // Replace with backend user ID
    private val currentUsername: String = "Current User" // Replace with backend username
    private val currentUserAvatar: String? = null // Replace with backend avatar URL

    // Temporary listener for "tap map to place marker" mode
    private var pendingMapClick: MapLibreMap.OnMapClickListener? = null

    // Track created markers for cleanup
    private val createdMarkers = mutableListOf<Marker>()

    // TODO: Backend - Match data structure with server API
    private data class PostData(
        val id: String, // Backend will provide real ID
        val title: String,
        val content: String,
        val location: LatLng,
        val username: String,
        val avatarUrl: String?,
        val createdAt: String, // Backend timestamp
        val replies: MutableList<ReplyData> = mutableListOf()
    )

    // TODO: Backend - Match reply structure with server
    private data class ReplyData(
        val id: String, // Backend reply ID
        val userId: String,
        val username: String,
        val content: String,
        val createdAt: String // Backend timestamp
    )

    // Marker -> PostData mapping
    private val markerToPost = mutableMapOf<Marker, PostData>()

    private val fused by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val reqPerms =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Log.d("MapFragment", "Permission request result: $permissions")
            if (hasLocationPermission()) moveToMyLocation() else moveToDefaultLocation()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _mapView = view.findViewById(R.id.mapView)

        Log.d("MapFragment", "MapView init using tiles source: ${TileSource.CURRENT_STYLE_URL}")

        val addPostView = view.findViewById<AddPostView>(R.id.addPostView)
        val fabAdd = view.findViewById<View>(R.id.fabAdd)
        val postMarkerDetailView = view.findViewById<PostMarkerDetailView>(R.id.postMarkerDetailView)

        addPostView?.setOnPostReadyListener { title, content ->
            // TODO: Backend - Get real user info from server
            startMarkerPlacementMode(title, content, currentUsername, currentUserAvatar)
        }

        fabAdd?.setOnClickListener {
            addPostView?.show()
        }

        // TODO: Backend - Load posts from server when view created
        // loadPostsFromBackend()

        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: MapLibreMap) {
        Log.d("MapFragment", "onMapReady called")
        maplibreMap = map

        // Enable map gestures
        map.uiSettings.apply {
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
        }

        map.setStyle(Style.Builder().fromUri(TileSource.CURRENT_STYLE_URL)) {
            // Set up marker click listener
            map.setOnMarkerClickListener { marker ->
                markerToPost[marker]?.let { showPostDetail(it) }
                true
            }
            ensureLocationThenCenter()

            // TODO: Backend - Load posts for current map area
            // loadPostsForMapArea(map.projection.visibleRegion.latLngBounds)
        }
    }

    private fun startMarkerPlacementMode(
        title: String,
        content: String,
        username: String,
        avatarUrl: String?
    ) {
        Toast.makeText(requireContext(), "Tap on map to place your post", Toast.LENGTH_LONG).show()
        val listener = MapLibreMap.OnMapClickListener { point ->
            placePostMarker(point, title, content, username, avatarUrl)
            true
        }
        pendingMapClick = listener
        maplibreMap?.addOnMapClickListener(listener)
    }

    private fun placePostMarker(
        location: LatLng,
        title: String,
        content: String,
        username: String,
        avatarUrl: String?
    ) {
        // Exit placement mode
        pendingMapClick?.let { maplibreMap?.removeOnMapClickListener(it) }
        pendingMapClick = null

        val bmp = createMarkerBitmap(
            title = title,
            username = username,
            avatarBitmap = null
        )
        val icon = bmp?.let { IconFactory.getInstance(requireContext()).fromBitmap(it) }

        // Create Marker
        val options = MarkerOptions()
            .position(location)
            .title(title)
        if (icon != null) options.icon(icon)

        val marker = maplibreMap?.addMarker(options)
        if (marker != null) {
            createdMarkers += marker

            // TODO: Backend - Call API to create post on server
            val tempPostId = "temp_${System.currentTimeMillis()}" // Replace with real ID from backend

            markerToPost[marker] = PostData(
                id = tempPostId,
                title = title,
                content = content,
                location = location,
                username = username,
                avatarUrl = avatarUrl,
                createdAt = System.currentTimeMillis().toString()
            )

            // TODO: Backend - Send post data to server API
            // sendPostToBackend(title, content, location, username, avatarUrl)

            Toast.makeText(requireContext(), "Post placed!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Failed to create marker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createMarkerBitmap(
        title: String,
        username: String,
        avatarBitmap: Bitmap?
    ): Bitmap? = try {
        val markerView = layoutInflater.inflate(R.layout.map_marker_view, null)

        val ivAvatar = markerView.findViewById<ImageView>(R.id.ivAvatar)
        val tvUsername = markerView.findViewById<TextView>(R.id.tvUsername)
        val tvTitle = markerView.findViewById<TextView>(R.id.tvPostTitle)

        tvUsername.text = username
        tvTitle.text = title
        avatarBitmap?.let { ivAvatar.setImageBitmap(it) }

        markerView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        markerView.layout(0, 0, markerView.measuredWidth, markerView.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            markerView.measuredWidth,
            markerView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        markerView.draw(canvas)
        bitmap
    } catch (e: Exception) {
        Log.e("MapFragment", "Error creating marker bitmap", e)
        null
    }

    private fun showPostDetail(postData: PostData) {
        val detailView = requireView().findViewById<PostMarkerDetailView>(R.id.postMarkerDetailView)

        // Convert replies for display
        val replyPairs = postData.replies.map { it.username to it.content }
        detailView.setPostData(postData.title, postData.content, replyPairs, postData.username)
        detailView.visibility = View.VISIBLE

        detailView.setOnReplySentListener { replyContent ->
            // TODO: Backend - Send reply to server API
            val newReply = ReplyData(
                id = "reply_temp_${System.currentTimeMillis()}", // Backend will provide real ID
                userId = currentUserId,
                username = currentUsername,
                content = replyContent,
                createdAt = System.currentTimeMillis().toString()
            )

            postData.replies.add(newReply)

            // TODO: Backend - Call API to save reply on server
            // sendReplyToBackend(postData.id, replyContent)

            Toast.makeText(requireContext(), "Reply sent: $replyContent", Toast.LENGTH_SHORT).show()
        }

        // Prevent clicks inside detail view from closing it
        detailView.setOnClickListener {
            // Do nothing - consume the click
        }
    }

    // TODO: Backend - Implement loading posts from server
    private fun loadPostsFromBackend() {
        // Call backend API to get posts
        // Handle response and create markers
        // Update markerToPost map
    }

    // TODO: Backend - Implement loading posts for map area
    private fun loadPostsForMapArea(bounds: org.maplibre.android.geometry.LatLngBounds) {
        // Get posts within map bounds from backend
        // Update markers on map
    }

    // TODO: Backend - Implement sending post to server
    private fun sendPostToBackend(
        title: String,
        content: String,
        location: LatLng,
        username: String,
        avatarUrl: String?
    ) {
        // Make API call to create post
        // Handle response and update local data
    }

    // TODO: Backend - Implement sending reply to server
    private fun sendReplyToBackend(postId: String, replyContent: String) {
        // Make API call to create reply
        // Handle response
    }

    // Rest of existing code remains the same...
    private fun ensureLocationThenCenter() {
        if (hasLocationPermission()) {
            moveToMyLocation()
        } else {
            reqPerms.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun hasLocationPermission(): Boolean =
        hasPerm(Manifest.permission.ACCESS_FINE_LOCATION) ||
                hasPerm(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun hasPerm(permission: String): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED

    private fun moveToMyLocation() {
        if (!hasLocationPermission()) {
            moveToDefaultLocation()
            return
        }

        try {
            fused.lastLocation.addOnSuccessListener { location ->
                val target = if (location != null) {
                    LatLng(location.latitude, location.longitude)
                } else {
                    getDefaultLocation()
                }
                moveCameraToLocation(target)
            }.addOnFailureListener { exception ->
                Log.e("MapFragment", "Failed to get location: ${exception.message}")
                moveToDefaultLocation()
            }
        } catch (securityException: SecurityException) {
            Log.e("MapFragment", "Permission denied: ${securityException.message}")
            moveToDefaultLocation()
        }
    }

    private fun moveToDefaultLocation() = moveCameraToLocation(getDefaultLocation())

    private fun getDefaultLocation(): LatLng = LatLng(43.0731, -89.4012)

    private fun moveCameraToLocation(location: LatLng) {
        maplibreMap?.let { map ->
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 14.0)
            map.animateCamera(cameraUpdate)
        } ?: Log.e("MapFragment", "maplibreMap is null")
    }

    // ===== MapView Lifecycle Methods =====
    override fun onStart() {
        super.onStart()
        _mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        _mapView?.onResume()
    }

    override fun onPause() {
        _mapView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        _mapView?.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        _mapView?.onLowMemory()
    }

    override fun onDestroyView() {
        // Clean up created markers
        try {
            createdMarkers.forEach { marker ->
                maplibreMap?.removeMarker(marker)
            }
            createdMarkers.clear()
            markerToPost.clear()
        } catch (exception: Exception) {
            // Ignore cleanup exceptions
        }

        _mapView?.onDestroy()
        _mapView = null
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _mapView?.onSaveInstanceState(outState)
    }
}