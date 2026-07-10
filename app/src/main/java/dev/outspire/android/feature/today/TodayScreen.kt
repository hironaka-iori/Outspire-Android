package dev.outspire.android.feature.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.outspire.android.data.model.PeriodPhase
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.ScheduleResolver
import dev.outspire.android.data.model.TimelinePeriod
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.AppRadius
import dev.outspire.android.designsystem.AppSpace
import dev.outspire.android.designsystem.ErrorCard
import dev.outspire.android.designsystem.GradientCard
import dev.outspire.android.designsystem.LoadingCard
import dev.outspire.android.designsystem.ScreenTitle
import dev.outspire.android.designsystem.StatusPill
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodayScreen(
    state: TodayUiState,
    user: User?,
    onSignIn: () -> Unit,
    onRefresh: () -> Unit,
    onClasses: () -> Unit,
    onActivities: () -> Unit,
    onGrades: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        Spacer(Modifier.height(AppSpace.xs))
        ScreenTitle(
            title = greeting(state.now.hour),
            subtitle = state.now.format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH),
            ),
        ) {
            if (user != null) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh timetable")
                }
            }
        }

        if (user == null) {
            SignInPrompt(onSignIn)
            QuickLinks(onClasses, onActivities, onGrades)
            Spacer(Modifier.height(88.dp))
            return@Column
        }

        if (user.isDemo) {
            StatusPill("Demo data", MaterialTheme.colorScheme.tertiary)
        }

        when {
            state.isLoading && state.schedule.isEmpty() -> LoadingCard("Loading your timetable...")
            state.error != null && state.schedule.isEmpty() -> ErrorCard(state.error)
            else -> ScheduleCard(state)
        }

        QuickLinks(onClasses, onActivities, onGrades)
        state.error?.takeIf { state.schedule.isNotEmpty() }?.let { ErrorCard(it) }
        Spacer(Modifier.height(88.dp))
    }
}

@Composable
private fun SignInPrompt(onSignIn: () -> Unit) {
    GradientCard(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
        ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
            Icon(Icons.Default.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            Text("Your school day, in one place", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(
                "Sign in to see live classes, scores, and CAS records, or use the included demo profile.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.86f),
            )
            Button(onClick = onSignIn) { Text("Open account") }
        }
    }
}

@Composable
private fun ScheduleCard(state: TodayUiState) {
    val timeline = ScheduleResolver.todayTimeline(state.schedule, state.now)
    if (!timeline.hasRemainingClasses) {
        NoClassesCard()
        return
    }

    val dayName = state.now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF45CC70), Color(0xFF13AFA5)),
                        ),
                    )
                    .padding(AppSpace.lg),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "$dayName's Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    classCountLabel(timeline.classCount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.86f),
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = AppSpace.sm, vertical = AppSpace.md),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                timeline.periods.forEach { period -> PeriodRow(period) }
            }
        }
    }
}

@Composable
private fun NoClassesCard() {
    GradientCard(
        colors = listOf(Color(0xFF22B44E), Color(0xFF00A99D)),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(AppSpace.xs))
            Text(
                "No Classes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                "Enjoy your free time!",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun PeriodRow(item: TimelinePeriod) {
    val entry = item.entry
    val accent = subjectColor(entry.subject)
    val past = item.phase == PeriodPhase.PAST
    val current = item.phase == PeriodPhase.CURRENT
    val primaryText = if (past) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val secondaryText = if (past) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val rowBackground = if (current) accent.copy(alpha = 0.11f) else Color.Transparent
    val timeFormatter = DateTimeFormatter.ofPattern("H:mm")
    val detail = buildList {
        add("${item.period.start.format(timeFormatter)} - ${item.period.end.format(timeFormatter)}")
        entry.teacher?.takeIf(String::isNotBlank)?.let(::add)
        entry.room?.takeIf(String::isNotBlank)?.let(::add)
    }.joinToString(" | ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground, RoundedCornerShape(AppRadius.lg))
            .padding(horizontal = AppSpace.md, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(9.dp)
                .background(
                    if (past) accent.copy(alpha = 0.32f) else accent,
                    CircleShape,
                ),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                entry.subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (current) FontWeight.Bold else FontWeight.Medium,
                color = primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                detail,
                style = MaterialTheme.typography.bodyMedium,
                color = secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (current) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LinearProgressIndicator(
                        progress = { item.progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        color = accent,
                        trackColor = accent.copy(alpha = 0.16f),
                    )
                    Text(
                        "ends in ${formatRemaining(item.remainingSeconds)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickLinks(
    onClasses: () -> Unit,
    onActivities: () -> Unit,
    onGrades: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
        Text("Quick links", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
            QuickLink("Classes", Icons.Default.CalendarMonth, onClasses, Modifier.weight(1f))
            QuickLink("Activities", Icons.Default.Checklist, onActivities, Modifier.weight(1f))
            QuickLink("Grades", Icons.Default.School, onGrades, Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickLink(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(AppRadius.lg))
            .clickable(onClick = onClick)
            .padding(AppSpace.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpace.xs),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

private fun greeting(hour: Int): String = when (hour) {
    in 5..11 -> "Good Morning"
    in 12..17 -> "Good Afternoon"
    else -> "Good Evening"
}

private fun classCountLabel(count: Int): String = "$count ${if (count == 1) "class" else "classes"}"

private fun formatRemaining(seconds: Long): String = String.format(
    Locale.ENGLISH,
    "%d:%02d",
    seconds / 60,
    seconds % 60,
)

private fun subjectColor(subject: String): Color {
    val value = subject.lowercase()
    return when {
        "math" in value -> Color(0xFF3698F5)
        "english" in value -> Color(0xFF7D57C2)
        "chem" in value -> Color(0xFFFF4F78)
        "physics" in value -> Color(0xFFFF9A45)
        "econom" in value -> Color(0xFFBD9172)
        "chinese" in value -> Color(0xFFC74B50)
        "tok" in value -> Color(0xFF31BFD6)
        "self" in value -> Color(0xFF7A7A86)
        else -> Color(0xFF5E6F64)
    }
}
