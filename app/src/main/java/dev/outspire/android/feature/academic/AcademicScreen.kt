package dev.outspire.android.feature.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.outspire.android.data.model.ScheduleResolver
import dev.outspire.android.data.model.SchoolPeriods
import dev.outspire.android.data.model.SchoolWeek
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.AppSpace
import dev.outspire.android.designsystem.ErrorCard
import dev.outspire.android.designsystem.LoadingCard
import dev.outspire.android.designsystem.RichCard
import dev.outspire.android.designsystem.ScreenTitle
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AcademicScreen(
    state: AcademicUiState,
    user: User?,
    onSignIn: () -> Unit,
    onRefresh: () -> Unit,
    onSelectDay: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        Spacer(Modifier.size(AppSpace.xs))
        ScreenTitle("Classes", "Your weekly timetable") {
            if (user != null) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh timetable")
                }
            }
        }

        if (user == null) {
            RichCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
                    Icon(Icons.Default.Login, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Sign in to load your timetable.", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onSignIn) { Text("Open account") }
                }
            }
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (day in SchoolWeek.days) {
                FilterChip(
                    selected = day == state.selectedDay,
                    onClick = { onSelectDay(day) },
                    label = { Text(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(2)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        when {
            state.isLoading && state.schedule.isEmpty() -> LoadingCard("Loading classes…")
            state.scheduleError != null && state.schedule.isEmpty() -> ErrorCard(state.scheduleError)
            else -> {
                val entries = ScheduleResolver.daySchedule(state.schedule, state.selectedDay)
                RichCard {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
                        entries.forEach { entry ->
                            val period = SchoolPeriods.all.first { it.number == entry.period }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(34.dp)
                                        .background(
                                            if (entry.isSelfStudy) MaterialTheme.colorScheme.surfaceVariant
                                            else MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(entry.period.toString(), style = MaterialTheme.typography.labelLarge)
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        entry.subject,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        listOfNotNull(entry.teacher, entry.room).joinToString(" · ").ifBlank { "Independent study" },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    period.start.format(DateTimeFormatter.ofPattern("H:mm")),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
        state.scheduleError?.takeIf { state.schedule.isNotEmpty() }?.let { ErrorCard(it) }
        Spacer(Modifier.size(88.dp))
    }
}

@Composable
fun ScoresScreen(
    state: AcademicUiState,
    user: User?,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        ScreenTitle("Grades", "Academic results")
        if (user == null) {
            ErrorCard("Sign in to view academic results.")
            Button(onClick = onSignIn) { Text("Open account") }
            return@Column
        }
        if (state.isLoading && state.scores.isEmpty()) LoadingCard("Loading grades…")
        state.scoreError?.let { ErrorCard(it) }
        state.scores.forEach { result ->
            RichCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(result.subject, style = MaterialTheme.typography.titleMedium)
                        Text(result.term, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(result.score, style = MaterialTheme.typography.headlineMedium)
                        result.grade?.let { Text("IB grade $it", color = Color(0xFF2E7D32)) }
                    }
                }
            }
        }
    }
}
