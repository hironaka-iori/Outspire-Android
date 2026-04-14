package com.computerization.outspire.feature.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.computerization.outspire.designsystem.AppSpace
import com.computerization.outspire.designsystem.coloredRichCard

@Composable
fun QuickLinksCard(
    onClubs: () -> Unit,
    onDining: () -> Unit,
    onActivities: () -> Unit,
    onReflect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpace.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
            QuickLinkTile(
                modifier = Modifier.weight(1f),
                title = "Clubs",
                icon = Icons.Filled.People,
                colors = listOf(Color(0xFF0A84FF).copy(alpha = 0.8f), Color(0xFF0A84FF).copy(alpha = 0.6f)),
                onClick = onClubs,
            )
            QuickLinkTile(
                modifier = Modifier.weight(1f),
                title = "Dining",
                icon = Icons.Filled.Restaurant,
                colors = listOf(Color(0xFFFF9F0A).copy(alpha = 0.8f), Color(0xFFFF9F0A).copy(alpha = 0.6f)),
                onClick = onDining,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
            QuickLinkTile(
                modifier = Modifier.weight(1f),
                title = "Activities",
                icon = Icons.Filled.Checklist,
                colors = listOf(Color(0xFF30D158).copy(alpha = 0.8f), Color(0xFF30D158).copy(alpha = 0.6f)),
                onClick = onActivities,
            )
            QuickLinkTile(
                modifier = Modifier.weight(1f),
                title = "Reflect",
                icon = Icons.Filled.EditNote,
                colors = listOf(Color(0xFFBF5AF2).copy(alpha = 0.8f), Color(0xFFBF5AF2).copy(alpha = 0.6f)),
                onClick = onReflect,
            )
        }
    }
}

@Composable
private fun QuickLinkTile(
    title: String,
    icon: ImageVector,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .coloredRichCard(colors = colors, shadowRadius = 8.dp)
            .clickable(onClick = onClick)
            .padding(AppSpace.md),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
        )
        Spacer(Modifier.height(AppSpace.xs))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}
