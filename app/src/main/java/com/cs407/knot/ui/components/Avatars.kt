package com.cs407.knot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@Composable
fun GroupAvatar(urls: List<String>, size: Dp = 54.dp) {
    when (urls.size) {
        0 -> Box(Modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
        1 -> AsyncImage(model = urls[0], contentDescription = null, modifier = Modifier.size(size).clip(CircleShape))
        else -> Row(Modifier.size(size)) {
            AsyncImage(urls[0], null, modifier = Modifier.weight(1f).fillMaxHeight().clip(CircleShape))
            Spacer(Modifier.width(4.dp))
            AsyncImage(urls[1], null, modifier = Modifier.weight(1f).fillMaxHeight().clip(CircleShape))
        }
    }
}