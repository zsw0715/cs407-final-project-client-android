package com.cs407.knot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun TopBar(title: String, left: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (left != null) left() else Spacer(Modifier.width(48.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}


data class BottomItem(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val selected: Boolean = false)


@Composable
fun BottomBar(items: List<BottomItem>, modifier: Modifier = Modifier) {
    Surface(tonalElevation = 4.dp, shadowElevation = 16.dp, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            items.forEach { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val bg = if (item.selected) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else Color.Transparent
                    Box(Modifier.clip(CircleShape).background(bg).padding(8.dp)) { Icon(item.icon, null) }
                    Text(item.label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}


@Composable fun SectionHeader(text: String) = Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 10.dp))