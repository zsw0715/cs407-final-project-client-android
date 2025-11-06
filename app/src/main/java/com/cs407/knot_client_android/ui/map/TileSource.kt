package com.cs407.knot_client_android.ui.map

object TileSource {

    private val maptilerApiKey = com.cs407.knot_client_android.BuildConfig.MAPTILER_API_KEY

    // default
    // const val CURRENT_STYLE_URL = "https://demotiles.maplibre.org/style.json"

    // OpenStreetMap
    // const val CURRENT_STYLE_URL = "https://tiles.openstreetmap.org/styles/osm-bright/style.json"

    // Maptiler
     val CURRENT_STYLE_URL = "https://api.maptiler.com/maps/streets/style.json?key=$maptilerApiKey"
}