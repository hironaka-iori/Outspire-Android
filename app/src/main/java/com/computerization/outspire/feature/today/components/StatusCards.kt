package com.computerization.outspire.feature.today.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.computerization.outspire.designsystem.AppSpace
import com.computerization.outspire.designsystem.coloredRichCard

@Composable
fun WeekendCard(modifier: Modifier = Modifier) {
    StatusCardShell(
        modifier = modifier,
        colors = listOf(Color(0xFFFFD60A).copy(alpha = 0.85f), Color(0xFFFF9F0A).copy(alpha = 0.75f)),
        iconContent = {
            Icon(
                imageVector = Icons.Filled.WbSunny,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 2.dp),
            )
        },
        title = "It's the Weekend!",
        subtitle = "Relax and recharge.",
    )
}

@Composable
fun NoClassCard(
    isDimmed: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = if (isDimmed) {
        listOf(Color(0xFF5E5CE6).copy(alpha = 0.8f), Color(0xFF5E5CE6).copy(alpha = 0.6f))
    } else {
        listOf(Color(0xFF30D158).copy(alpha = 0.75f), Color(0xFF66D4CF).copy(alpha = 0.65f))
    }
    StatusCardShell(
        modifier = modifier,
        colors = colors,
        iconContent = {
            Icon(
                imageVector = if (isDimmed) Icons.Filled.Bedtime else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
            )
        },
        title = if (isDimmed) "All Done for Today" else "No Classes",
        subtitle = if (isDimmed) "Great work! Time to relax." else "Enjoy your free time!",
    )
}

@Composable
fun HolidayModeCard(
    endDateLabel: String?,
    modifier: Modifier = Modifier,
) {
    StatusCardShell(
        modifier = modifier,
        colors = listOf(Color(0xFFFF9F0A).copy(alpha = 0.85f), Color(0xFFFF453A).copy(alpha = 0.7f)),
        iconContent = {
            Icon(
                imageVector = Icons.Filled.BeachAccess,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
            )
        },
        title = "Holiday Mode",
        subtitle = endDateLabel?.let { "Until $it" } ?: "Enjoy your time off!",
    )
}

@Composable
private fun StatusCardShell(
    colors: List<Color>,
    iconContent: @Composable () -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .coloredRichCard(colors = colors)
            .padding(AppSpace.cardPadding),
    ) {
        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppSpace.xxs)) {
            Box(
                modifier = Modifier
                    .height(44.dp),
                contentAlignment = Alignment.CenterStart,
            ) { iconContent() }
            Spacer(Modifier.height(AppSpace.xs))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}
