package com.cs407.knot_client_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cs407.knot_client_android.ui.map.MapFragment
import org.maplibre.android.MapLibre

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 MapLibre
        MapLibre.getInstance(this)

        // 设置布局
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MapFragment())
                .commit()
        }
    }
}