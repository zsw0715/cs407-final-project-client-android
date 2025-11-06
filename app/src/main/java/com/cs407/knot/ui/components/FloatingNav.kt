package com.cs407.knot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FloatingNav(
    onMap: () -> Unit,
    onChat: () -> Unit,
    onUser: () -> Unit,
    onAddPost: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        var expanded by remember { mutableStateOf(false) }

        Row(
            Modifier.align(Alignment.BottomStart).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmallFab(Icons.Rounded.Map)  { expanded = false; onMap() }
                    SmallFab(Icons.AutoMirrored.Rounded.Chat) { expanded = false; onChat() }
                    SmallFab(Icons.Rounded.AccountCircle) { expanded = false; onUser() }
                }
            }
            Spacer(Modifier.width(12.dp))
            FloatingActionButton(onClick = { expanded = !expanded }, shape = CircleShape) {
                Icon(if (expanded) Icons.Rounded.Close else Icons.Rounded.Menu, null)
            }
        }

        // placeholder for the add button
        FloatingActionButton(
            onClick = onAddPost,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            shape = CircleShape
        ) { Icon(Icons.Rounded.Add, null) }
    }
}

@Composable
private fun SmallFab(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick, shape = CircleShape, modifier = Modifier.size(48.dp)) {
        Icon(icon, null)
    }
}
