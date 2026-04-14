package com.computerization.outspire.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.coloredRichCard(
    colors: List<Color>,
    cornerRadius: Dp = 20.dp,
    shadowRadius: Dp = 12.dp,
): Modifier = composed {
    val shape = RoundedCornerShape(cornerRadius)
    this
        .shadow(elevation = shadowRadius, shape = shape, clip = false)
        .clip(shape)
        .background(
            Brush.linearGradient(colors),
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
            ),
            shape = shape,
        )
}
