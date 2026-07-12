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
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import dev.outspire.android.data.model.ScheduleSettings
import dev.outspire.android.data.model.SchoolPeriods
import dev.outspire.android.data.model.SchoolWeek
import dev.outspire.android.data.model.TimelinePeriod
import dev.outspire.android.data.model.TodayTimeline
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.AppRadius
import dev.outspire.android.designsystem.AppSpace
import dev.outspire.android.designsystem.ErrorCard
import dev.outspire.android.designsystem.GradientCard
import dev.outspire.android.designsystem.LoadingCard
import dev.outspire.android.designsystem.RichCard
import dev.outspire.android.designsystem.StatusPill
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    state: TodayUiState,
    user: User?,
    onSignIn: () -> Unit,
    onRefresh: () -> Unit,
    onSelectScheduleDay: (DayOfWeek?) -> Unit,
    onHolidayEnabled: (Boolean) -> Unit,
    onHolidayEndDateEnabled: (Boolean) -> Unit,
    onHolidayEndDate: (LocalDate) -> Unit,
    onShowFutureCountdown: (Boolean) -> Unit,
    onClasses: () -> Unit,
    onActivities: () -> Unit,
    onGrades: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val holidayActive = user != null && state.settings.isHolidayActive(state.now.toLocalDate())
    val weekend = state.now.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        Spacer(Modifier.height(AppSpace.xs))
        TodayHeader(
            state = state,
            holidayActive = holidayActive,
            showSettings = user != null,
            onSettings = { showSettings = true },
        )

        if (user == null) {
            SignInPrompt(onSignIn)
            QuickLinks(onClasses, onActivities, onGrades)
            Spacer(Modifier.height(88.dp))
            return@Column
        }

        when {
            holidayActive -> HolidayCard(state.settings)
            weekend && state.settings.dayOverride == null -> WeekendCard()
            state.isLoading && state.schedule.isEmpty() -> LoadingCard("Loading your timetable...")
            state.error != null && state.schedule.isEmpty() -> ErrorCard(state.error)
            else -> ScheduleCard(state)
        }

        QuickLinks(onClasses, onActivities, onGrades)
        state.error?.takeIf { state.schedule.isNotEmpty() }?.let { ErrorCard(it) }
        Spacer(Modifier.height(88.dp))
    }

    if (showSettings) {
        ScheduleSettingsSheet(
            settings = state.settings,
            onDismiss = { showSettings = false },
            onRefresh = onRefresh,
            onSelectDay = onSelectScheduleDay,
            onHolidayEnabled = onHolidayEnabled,
            onHolidayEndDateEnabled = onHolidayEndDateEnabled,
            onHolidayEndDate = onHolidayEndDate,
            onShowFutureCountdown = onShowFutureCountdown,
        )
    }
}

@Composable
private fun TodayHeader(
    state: TodayUiState,
    holidayActive: Boolean,
    showSettings: Boolean,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(greeting(state.now.hour), style = MaterialTheme.typography.headlineMedium)
            Text(
                state.now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (holidayActive && state.settings.holidayEndDateEnabled) {
                Text(
                    "Holiday until ${state.settings.holidayEndDate.format(HOLIDAY_DATE_FORMAT)}",
                    color = Color(0xFFFF8A2B),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        if (showSettings) {
            IconButton(
                onClick = onSettings,
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f), CircleShape),
            ) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "Schedule settings")
            }
        }
    }
}

@Composable
private fun SignInPrompt(onSignIn: () -> Unit) {
    GradientCard(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
            Icon(Icons.Default.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            Text("Your school day, in one place", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(
                "Sign in to see live classes, scores, and CAS records.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.86f),
            )
            Button(onClick = onSignIn) { Text("Open account") }
        }
    }
}

@Composable
private fun WeekendCard() {
    GradientCard(colors = listOf(Color(0xFF28AE63), Color(0xFF17A89F))) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
            Icon(
                Icons.Default.BeachAccess,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.84f),
                modifier = Modifier.size(48.dp),
            )
            Text(
                "It's the weekend!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                "Relax and recharge.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun HolidayCard(settings: ScheduleSettings) {
    GradientCard(colors = listOf(Color(0xFFF18A19), Color(0xFFE33932))) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    Icons.Default.BeachAccess,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.82f),
                    modifier = Modifier.size(52.dp),
                )
                Icon(
                    Icons.Default.Flight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(66.dp),
                )
            }
            Spacer(Modifier.height(AppSpace.md))
            Text(
                "Holiday Mode",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                if (settings.holidayEndDateEnabled) {
                    "Until ${settings.holidayEndDate.format(HOLIDAY_DATE_FORMAT)}"
                } else {
                    "Enjoy your break."
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.86f),
            )
        }
    }
}

@Composable
private fun ScheduleCard(state: TodayUiState) {
    val selectedDay = state.settings.dayOverride ?: state.now.dayOfWeek
    val timeline = displayedTimeline(state, selectedDay)
    if (!timeline.hasRemainingClasses) {
        NoClassesCard()
        return
    }
    val dayName = selectedDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val firstUpcoming = timeline.periods.firstOrNull { it.phase == PeriodPhase.UPCOMING }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF45CC70), Color(0xFF13AFA5))),
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
                timeline.periods.forEach { period ->
                    val futureCountdown = if (
                        state.settings.showFutureCountdown &&
                        state.settings.dayOverride == null &&
                        period == firstUpcoming
                    ) {
                        Duration.between(state.now.toLocalTime(), period.period.start).seconds.takeIf { it > 0 }
                    } else {
                        null
                    }
                    PeriodRow(period, futureCountdown)
                }
            }
        }
    }
}

private fun displayedTimeline(state: TodayUiState, selectedDay: DayOfWeek): TodayTimeline {
    if (state.settings.dayOverride == null || selectedDay == state.now.dayOfWeek) {
        return ScheduleResolver.todayTimeline(state.schedule, state.now)
    }
    val entries = ScheduleResolver.daySchedule(state.schedule, selectedDay)
    val periods = entries.map { entry ->
        TimelinePeriod(
            period = SchoolPeriods.all.first { it.number == entry.period },
            entry = entry,
            phase = PeriodPhase.UPCOMING,
            progress = 0f,
            remainingSeconds = 0,
        )
    }
    val classCount = entries.count { !it.representsSelfStudy }
    return TodayTimeline(periods, classCount, hasRemainingClasses = classCount > 0)
}

@Composable
private fun NoClassesCard() {
    GradientCard(colors = listOf(Color(0xFF22B44E), Color(0xFF00A99D))) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.size(48.dp),
            )
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
private fun PeriodRow(item: TimelinePeriod, futureCountdownSeconds: Long?) {
    val entry = item.entry
    val accent = subjectColor(entry.subject)
    val past = item.phase == PeriodPhase.PAST
    val current = item.phase == PeriodPhase.CURRENT
    val primaryText = if (past) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.34f)
    else MaterialTheme.colorScheme.onSurface
    val secondaryText = if (past) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
    else MaterialTheme.colorScheme.onSurfaceVariant
    val detail = buildList {
        val formatter = DateTimeFormatter.ofPattern("H:mm")
        add("${item.period.start.format(formatter)} - ${item.period.end.format(formatter)}")
        entry.teacher?.takeIf(String::isNotBlank)?.let(::add)
        entry.room?.takeIf(String::isNotBlank)?.let(::add)
    }.joinToString(" | ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (current) accent.copy(alpha = 0.11f) else Color.Transparent,
                RoundedCornerShape(AppRadius.lg),
            )
            .padding(horizontal = AppSpace.md, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(9.dp)
                .background(if (past) accent.copy(alpha = 0.32f) else accent, CircleShape),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
                ProgressCountdown(
                    progress = item.progress,
                    label = "ends in ${formatRemaining(item.remainingSeconds)}",
                    accent = accent,
                )
            } else if (futureCountdownSeconds != null) {
                Text(
                    "starts in ${formatCountdown(futureCountdownSeconds)}",
                    modifier = Modifier.padding(top = 4.dp),
                    color = accent,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ProgressCountdown(progress: Float, label: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
            color = accent,
            trackColor = accent.copy(alpha = 0.16f),
        )
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleSettingsSheet(
    settings: ScheduleSettings,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onSelectDay: (DayOfWeek?) -> Unit,
    onHolidayEnabled: (Boolean) -> Unit,
    onHolidayEndDateEnabled: (Boolean) -> Unit,
    onHolidayEndDate: (LocalDate) -> Unit,
    onShowFutureCountdown: (Boolean) -> Unit,
) {
    var showEndDatePicker by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpace.lg, vertical = AppSpace.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpace.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Schedule Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onDismiss) { Text("Done") }
            }

            SettingsSection("Day Selection") {
                DayOption("Today", settings.dayOverride == null) { onSelectDay(null) }
                SchoolWeek.days.forEach { day ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    DayOption(
                        day.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        settings.dayOverride == day,
                    ) { onSelectDay(day) }
                }
            }

            Text("Holiday Mode", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            RichCard {
                Column {
                    SettingsSwitchRow(
                        title = "Enable Holiday Mode",
                        icon = Icons.Default.WbSunny,
                        checked = settings.holidayEnabled,
                        accent = Color(0xFFFF8A2B),
                        onCheckedChange = onHolidayEnabled,
                    )
                    HorizontalDivider()
                    SettingsSwitchRow(
                        title = "Set End Date",
                        checked = settings.holidayEndDateEnabled,
                        onCheckedChange = onHolidayEndDateEnabled,
                    )
                    if (settings.holidayEndDateEnabled) {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEndDatePicker = true }
                                .padding(vertical = AppSpace.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Holiday Ends", style = MaterialTheme.typography.titleMedium)
                            Text(
                                settings.holidayEndDate.format(HOLIDAY_DATE_FORMAT),
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                                    .padding(horizontal = AppSpace.md, vertical = AppSpace.xs),
                            )
                        }
                    }
                }
            }

            Text("Display Options", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            RichCard {
                SettingsSwitchRow(
                    title = "Show Countdown for Future Classes",
                    checked = settings.showFutureCountdown,
                    onCheckedChange = onShowFutureCountdown,
                )
            }
            OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.size(AppSpace.xs))
                Text("Refresh timetable")
            }
            Spacer(Modifier.height(AppSpace.lg))
        }
    }

    if (showEndDatePicker) {
        HolidayEndDatePicker(
            date = settings.holidayEndDate,
            onDismiss = { showEndDatePicker = false },
            onSelect = {
                onHolidayEndDate(it)
                showEndDatePicker = false
            },
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        RichCard { Column { content() } }
    }
}

@Composable
private fun DayOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppSpace.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        if (selected) Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpace.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpace.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = accent) }
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = accent)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HolidayEndDatePicker(
    date: LocalDate,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onSelect(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
            ) { Text("Select") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = pickerState)
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

private fun formatRemaining(seconds: Long): String = String.format(Locale.ENGLISH, "%d:%02d", seconds / 60, seconds % 60)

private fun formatCountdown(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return if (hours > 0) String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, remainingSeconds)
    else String.format(Locale.ENGLISH, "%d:%02d", minutes, remainingSeconds)
}

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

private val HOLIDAY_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
