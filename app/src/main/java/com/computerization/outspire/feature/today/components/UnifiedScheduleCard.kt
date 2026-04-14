package com.computerization.outspire.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.computerization.outspire.data.model.DomainClass
import kotlinx.datetime.LocalTime

@Composable
fun UnifiedScheduleCard(
    dayName: String,
    classes: List<DomainClass>,
    activeIndex: Int?,
    nowLocal: LocalTime,
    modifier: Modifier = Modifier,
) {
    if (classes.isEmpty()) {
        NoClassCard(modifier = modifier)
        return
    }
    val accent = subjectColor(classes.first().subject)
    val shape = RoundedCornerShape(22.dp)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surface = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = accent.copy(alpha = 0.25f),
                spotColor = accent.copy(alpha = 0.25f),
            )
            .shadow(elevation = 2.dp, shape = shape)
            .clip(shape)
            .background(surface),
    ) {
        Header(dayName = dayName, classes = classes, accent = accent)
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            classes.forEachIndexed { idx, cls ->
                val isActive = idx == activeIndex
                val isPast = activeIndex != null && idx < activeIndex
                val alsoPastByTime = nowLocal >= cls.end
                ScheduleRow(
                    cls = cls,
                    isActive = isActive,
                    isPast = isPast || (alsoPastByTime && !isActive),
                    nowLocal = nowLocal,
                    isDark = isDark,
                )
            }
        }
    }
}

@Composable
private fun Header(dayName: String, classes: List<DomainClass>, accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(accent, accent.copy(alpha = 0.8f)),
                ),
            )
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        // Top highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                    ),
                )
                .align(Alignment.TopCenter),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "$dayName's Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${classes.size} ${if (classes.size == 1) "class" else "classes"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = "${classes.size}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun ScheduleRow(
    cls: DomainClass,
    isActive: Boolean,
    isPast: Boolean,
    nowLocal: LocalTime,
    isDark: Boolean,
) {
    val color = subjectColor(cls.subject)
    val rowAlpha = if (isPast) 0.45f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isActive) 6.dp else 0.dp)
            .let {
                if (isActive) {
                    it
                        .clip(RoundedCornerShape(14.dp))
                        .background(color.copy(alpha = if (isDark) 0.12f else 0.06f))
                } else {
                    it
                }
            }
            .padding(
                horizontal = if (isActive) 12.dp else 18.dp,
                vertical = if (isActive) 12.dp else 9.dp,
            )
            .alpha(rowAlpha),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (isActive) 6.dp else 3.dp),
            ) {
                Text(
                    text = cls.subject.ifBlank { "Class" },
                    style = if (isActive) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = metaLine(cls),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = if (isPast) 0.4f else 1f),
                    maxLines = 1,
                )
                if (isActive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progressOf(cls, nowLocal) },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = color.copy(alpha = 0.18f),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "ends in ${countdownOf(cls, nowLocal)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}

private fun metaLine(cls: DomainClass): String {
    val range = "${formatTime(cls.start)} – ${formatTime(cls.end)}"
    val parts = mutableListOf(range)
    if (cls.teacher.isNotBlank()) parts.add(cls.teacher)
    if (cls.room.isNotBlank()) parts.add(cls.room)
    return parts.joinToString(" · ")
}

private fun formatTime(t: LocalTime): String {
    val h = if (t.hour % 12 == 0) 12 else t.hour % 12
    return "%d:%02d".format(h, t.minute)
}

private fun progressOf(cls: DomainClass, now: LocalTime): Float {
    val total = secs(cls.end) - secs(cls.start)
    if (total <= 0) return 0f
    val elapsed = secs(now) - secs(cls.start)
    return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}

private fun countdownOf(cls: DomainClass, now: LocalTime): String {
    val remaining = (secs(cls.end) - secs(now)).coerceAtLeast(0)
    val m = remaining / 60
    val s = remaining % 60
    return "%d:%02d".format(m, s)
}

private fun secs(t: LocalTime): Int = t.hour * 3600 + t.minute * 60 + t.second

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}
