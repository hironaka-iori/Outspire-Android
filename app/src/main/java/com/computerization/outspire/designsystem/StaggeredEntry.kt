package com.computerization.outspire.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

fun Modifier.staggeredEntry(index: Int, animate: Boolean): Modifier = composed {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(animate) {
        if (animate) {
            delay(index * 120L)
            started = true
        }
    }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "stagger$index",
    )
    graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 30.dp.toPx()
    }
}
