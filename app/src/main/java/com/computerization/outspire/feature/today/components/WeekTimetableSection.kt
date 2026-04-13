package com.computerization.outspire.feature.today.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.computerization.outspire.data.model.DomainClass
import com.computerization.outspire.designsystem.AppRadius
import com.computerization.outspire.designsystem.AppSpace
import kotlinx.datetime.DayOfWeek

@Composable
fun WeekTimetableSection(
    week: Map<DayOfWeek, List<DomainClass>>,
    modifier: Modifier = Modifier,
) {
    val days = listOf(
        DayOfWeek.MONDAY to "Mon",
        DayOfWeek.TUESDAY to "Tue",
        DayOfWeek.WEDNESDAY to "Wed",
        DayOfWeek.THURSDAY to "Thu",
        DayOfWeek.FRIDAY to "Fri",
    )
    val maxRows = (week.values.maxOfOrNull { it.size } ?: 0).coerceAtLeast(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(AppSpace.cardPadding)) {
            Text(
                text = "This week",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (week.values.all { it.isEmpty() }) {
                Text(
                    text = "No classes this week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                return@Column
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                days.forEach { (dow, label) ->
                    DayColumn(label = label, classes = week[dow].orEmpty(), rows = maxRows)
                }
            }
        }
    }
}

@Composable
private fun DayColumn(label: String, classes: List<DomainClass>, rows: Int) {
    Column(
        modifier = Modifier.width(96.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        repeat(rows) { i ->
            val c = classes.getOrNull(i)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (c == null) {
                    Text(
                        "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = c.subject,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = c.room,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
