package dev.outspire.android.feature.academic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.outspire.android.data.model.ClassPeriod
import dev.outspire.android.data.model.PeriodPhase
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.ScheduleResolver
import dev.outspire.android.data.model.SchoolPeriods
import dev.outspire.android.data.model.SchoolWeek
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.AppRadius
import dev.outspire.android.designsystem.AppSpace
import dev.outspire.android.designsystem.ErrorCard
import dev.outspire.android.designsystem.LoadingCard
import dev.outspire.android.designsystem.RichCard
import dev.outspire.android.designsystem.ScreenTitle
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicScreen(
    state: AcademicUiState,
    user: User?,
    onSignIn: () -> Unit,
    onRefresh: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var selectedPeriod by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        Spacer(Modifier.height(AppSpace.xs))
        ClassHeader(
            signedIn = user != null,
            refreshing = state.isLoading,
            onToday = onToday,
            onRefresh = onRefresh,
            onCalendar = { showDatePicker = true },
            modifier = Modifier.padding(horizontal = AppSpace.lg),
        )

        WeekStrip(
            selectedDate = state.selectedDate,
            onSelectDate = onSelectDate,
            modifier = Modifier.padding(horizontal = AppSpace.md),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))

        if (user == null) {
            RichCard(modifier = Modifier.padding(horizontal = AppSpace.lg)) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
                    Icon(
                        Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text("Sign in to load your timetable.", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onSignIn) { Text("Open account") }
                }
            }
            Spacer(Modifier.height(88.dp))
            return@Column
        }

        when {
            state.isLoading && state.schedule.isEmpty() -> {
                Box(Modifier.padding(horizontal = AppSpace.lg)) {
                    LoadingCard("Loading classes...")
                }
            }
            state.scheduleError != null && state.schedule.isEmpty() -> {
                Box(Modifier.padding(horizontal = AppSpace.lg)) {
                    ErrorCard(state.scheduleError)
                }
            }
            else -> ClassList(
                entries = ScheduleResolver.daySchedule(state.schedule, state.selectedDay),
                selectedDate = state.selectedDate,
                now = state.now,
                onSelectPeriod = { selectedPeriod = it },
                modifier = Modifier.padding(horizontal = AppSpace.lg),
            )
        }

        state.scheduleError?.takeIf { state.schedule.isNotEmpty() }?.let {
            Box(Modifier.padding(horizontal = AppSpace.lg)) { ErrorCard(it) }
        }
        Spacer(Modifier.height(88.dp))
    }

    if (showDatePicker) {
        ClassDatePicker(
            selectedDate = state.selectedDate,
            onDismiss = { showDatePicker = false },
            onSelect = onSelectDate,
        )
    }

    selectedPeriod?.let { periodNumber ->
        val entry = ScheduleResolver
            .daySchedule(state.schedule, state.selectedDay)
            .firstOrNull { it.period == periodNumber }
        if (entry != null) {
            ClassDetailsDialog(
                entry = entry,
                period = SchoolPeriods.all.first { it.number == periodNumber },
                onDismiss = { selectedPeriod = null },
            )
        }
    }
}

@Composable
private fun ClassHeader(
    signedIn: Boolean,
    refreshing: Boolean,
    onToday: () -> Unit,
    onRefresh: () -> Unit,
    onCalendar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Class",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                TextButton(onClick = onToday) {
                    Text("Today", style = MaterialTheme.typography.titleMedium)
                }
                if (signedIn) {
                    IconButton(onClick = onRefresh, enabled = !refreshing) {
                        if (refreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh timetable")
                        }
                    }
                }
                IconButton(onClick = onCalendar) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Choose date")
                }
            }
        }
    }
}

@Composable
private fun WeekStrip(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SchoolWeek.days.forEachIndexed { index, day ->
            val date = monday.plusDays(index.toLong())
            val selected = date == selectedDate
            val interactionSource = remember(date) { MutableInteractionSource() }
            val pressed by interactionSource.collectIsPressedAsState()
            val circleColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 520f),
                label = "weekday circle color",
            )
            val circleScale by animateFloatAsState(
                targetValue = when {
                    pressed -> 0.90f
                    selected -> 1f
                    else -> 0.88f
                },
                animationSpec = spring(dampingRatio = 0.68f, stiffness = 460f),
                label = "weekday circle scale",
            )
            val labelColor by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 600f),
                label = "weekday label color",
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onSelectDate(date) },
                    )
                    .padding(vertical = AppSpace.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpace.xs),
            ) {
                Text(
                    if (selected) {
                        day.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    } else {
                        day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = labelColor,
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .graphicsLayer {
                            scaleX = circleScale
                            scaleY = circleScale
                        }
                        .background(circleColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.background
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassList(
    entries: List<ScheduleEntry>,
    selectedDate: LocalDate,
    now: LocalDateTime,
    onSelectPeriod: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpace.sm),
    ) {
        entries.forEach { entry ->
            val period = SchoolPeriods.all.first { it.number == entry.period }
            ClassPeriodCard(
                entry = entry,
                period = period,
                phase = periodPhase(selectedDate, period, now),
                now = now,
                onClick = { onSelectPeriod(entry.period) },
            )
        }
    }
}

@Composable
private fun ClassPeriodCard(
    entry: ScheduleEntry,
    period: ClassPeriod,
    phase: PeriodPhase,
    now: LocalDateTime,
    onClick: () -> Unit,
) {
    val past = phase == PeriodPhase.PAST
    val current = phase == PeriodPhase.CURRENT
    val colors = classGradient(entry).map { color ->
        if (past) color.copy(alpha = 0.38f) else color
    }
    val mainText = Color.White.copy(alpha = if (past) 0.58f else 0.96f)
    val detailText = Color.White.copy(alpha = if (past) 0.48f else 0.76f)
    val shape = RoundedCornerShape(AppRadius.xl)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (current) 154.dp else 132.dp)
            .background(Brush.linearGradient(colors), shape)
            .clickable(onClick = onClick)
            .padding(AppSpace.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                entry.subject,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = mainText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "Period ${period.number}  ·  ${periodTime(period)}",
                style = MaterialTheme.typography.bodyLarge,
                color = detailText,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
            ) {
                if (entry.representsSelfStudy) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = detailText,
                        modifier = Modifier.size(20.dp),
                    )
                    Text("Free Period", color = detailText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open class details",
                        tint = detailText,
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = detailText,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        entry.teacher?.takeIf(String::isNotBlank) ?: "Teacher unavailable",
                        color = detailText,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    entry.room?.takeIf(String::isNotBlank)?.let { room ->
                        Icon(
                            Icons.Default.MeetingRoom,
                            contentDescription = null,
                            tint = detailText,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(room, color = detailText, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open class details",
                        tint = detailText,
                    )
                }
            }

            if (current) {
                val remaining = Duration.between(now.toLocalTime(), period.end).seconds.coerceAtLeast(0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LinearProgressIndicator(
                        progress = { period.progressAt(now.toLocalTime()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.18f),
                    )
                    Text(
                        formatRemaining(remaining),
                        color = mainText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassDetailsDialog(
    entry: ScheduleEntry,
    period: ClassPeriod,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(entry.subject) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
                Text("Period ${period.number} · ${periodTime(period)}")
                if (entry.representsSelfStudy) {
                    Text("Free Period")
                } else {
                    Text("Teacher: ${entry.teacher?.takeIf(String::isNotBlank) ?: "Unavailable"}")
                    Text("Room: ${entry.room?.takeIf(String::isNotBlank) ?: "Unavailable"}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassDatePicker(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onSelect(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    onDismiss()
                },
                enabled = pickerState.selectedDateMillis != null,
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = pickerState)
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
        if (state.isLoading && state.scores.isEmpty()) LoadingCard("Loading grades...")
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

private fun periodPhase(
    selectedDate: LocalDate,
    period: ClassPeriod,
    now: LocalDateTime,
): PeriodPhase = when {
    selectedDate < now.toLocalDate() -> PeriodPhase.PAST
    selectedDate > now.toLocalDate() -> PeriodPhase.UPCOMING
    period.isActiveAt(now.toLocalTime()) -> PeriodPhase.CURRENT
    now.toLocalTime() >= period.end -> PeriodPhase.PAST
    else -> PeriodPhase.UPCOMING
}

private fun periodTime(period: ClassPeriod): String {
    val start = period.start.format(DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH))
    val end = period.end.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
    return "$start-$end"
}

private fun formatRemaining(seconds: Long): String = String.format(
    Locale.ENGLISH,
    "%d:%02d",
    seconds / 60,
    seconds % 60,
)

private fun classGradient(entry: ScheduleEntry): List<Color> {
    val subject = entry.subject.lowercase()
    return when {
        entry.representsSelfStudy -> listOf(Color(0xFFF1C9A6), Color(0xFFF5D5BA))
        "math" in subject -> listOf(Color(0xFF2297F4), Color(0xFF59B4FF))
        "physics" in subject -> listOf(Color(0xFFFF9848), Color(0xFFFFB269))
        "chem" in subject -> listOf(Color(0xFFFF4770), Color(0xFFFF7E91))
        "econom" in subject -> listOf(Color(0xFFB98262), Color(0xFFD6A17C))
        "english" in subject -> listOf(Color(0xFF7957D7), Color(0xFFA77CEB))
        "chinese" in subject -> listOf(Color(0xFFE65158), Color(0xFFF07B77))
        "tok" in subject -> listOf(Color(0xFF22B5CC), Color(0xFF5ACDDD))
        else -> listOf(Color(0xFF4E8D73), Color(0xFF75B99A))
    }
}
