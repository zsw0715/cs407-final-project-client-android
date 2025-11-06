package com.cs407.knot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


val SoftCard = RoundedCornerShape(20.dp)


@Composable
fun CardContainer(
    modifier: Modifier = Modifier,
    tonal: Boolean = false,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val color = if (tonal) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    Column(
        modifier
            .shadow(8.dp, SoftCard)
            .clip(SoftCard)
            .background(color)
            .padding(padding)
    ) { content() }
}


@Composable fun EmptyHint(text: String) = Text(text, color = MaterialTheme.colorScheme.outline)