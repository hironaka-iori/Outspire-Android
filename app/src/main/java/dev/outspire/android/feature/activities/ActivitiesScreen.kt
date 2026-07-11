package dev.outspire.android.feature.activities

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.AppRadius
import dev.outspire.android.designsystem.AppSpace
import dev.outspire.android.designsystem.ErrorCard
import dev.outspire.android.designsystem.GradientCard
import dev.outspire.android.designsystem.LoadingCard
import dev.outspire.android.designsystem.RichCard
import dev.outspire.android.designsystem.StatusPill
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ActivitiesScreen(
    state: ActivitiesUiState,
    user: User?,
    onSignIn: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedClub by rememberSaveable { mutableStateOf(ALL_CLUBS) }
    var selectedActivity by remember { mutableStateOf<CasActivity?>(null) }
    val clubs = state.activities.map(CasActivity::club).distinct().sorted()
    val filteredActivities = state.activities.filter { activity ->
        (selectedClub == ALL_CLUBS || activity.club == selectedClub) &&
            (query.isBlank() || activity.title.contains(query, ignoreCase = true) ||
                activity.reflection.contains(query, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        Spacer(Modifier.padding(top = AppSpace.xs))
        ActivitiesHeader(
            signedIn = user != null,
            refreshing = state.isLoading,
            onRefresh = onRefresh,
        )

        if (user == null) {
            RichCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
                    Icon(
                        Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text("Sign in to see your CAS records.", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onSignIn) { Text("Open account") }
                }
            }
            return@Column
        }

        if (state.activities.isNotEmpty()) {
            ActivitySummary(state.activities)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search activities or reflections") },
                shape = RoundedCornerShape(AppRadius.lg),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpace.xs),
            ) {
                listOf(ALL_CLUBS).plus(clubs).forEach { club ->
                    ActivityFilter(
                        label = club,
                        selected = selectedClub == club,
                        onClick = { selectedClub = club },
                    )
                }
            }
        }

        when {
            state.isLoading && state.activities.isEmpty() -> LoadingCard("Loading CAS records...")
            state.error != null && state.activities.isEmpty() -> ErrorCard(state.error)
            filteredActivities.isEmpty() -> RichCard {
                Text(
                    if (state.activities.isEmpty()) "No activity records yet." else "No matching activities.",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            else -> filteredActivities.forEach { activity ->
                ActivityCard(activity, onClick = { selectedActivity = activity })
            }
        }
        state.error?.takeIf { state.activities.isNotEmpty() }?.let { ErrorCard(it) }
        Spacer(Modifier.padding(bottom = 88.dp))
    }

    selectedActivity?.let { activity ->
        ActivityDetailsDialog(activity, onDismiss = { selectedActivity = null })
    }
}

@Composable
private fun ActivitiesHeader(
    signedIn: Boolean,
    refreshing: Boolean,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                "Activities",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Creativity, activity, and service",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (signedIn) {
            IconButton(onClick = onRefresh, enabled = !refreshing) {
                if (refreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh activities")
                }
            }
        }
    }
}

@Composable
private fun ActivitySummary(activities: List<CasActivity>) {
    GradientCard(colors = listOf(Color(0xFF2E7D5A), Color(0xFF365F9D))) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text("Recorded CAS time", color = Color.White.copy(alpha = 0.8f))
                Text(
                    "${formatHours(activities.sumOf(CasActivity::hours))} hours",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
            }
            Text(
                "${activities.count(CasActivity::reflectionComplete)}/${activities.size} reflected",
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ActivityFilter(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember(label) { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 520f),
        label = "activity filter scale",
    )
    val background by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
        },
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 600f),
        label = "activity filter color",
    )
    Text(
        text = label,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(background, RoundedCornerShape(50))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = AppSpace.md, vertical = 10.dp),
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1,
    )
}

@Composable
private fun ActivityCard(activity: CasActivity, onClick: () -> Unit) {
    val interactionSource = remember(activity.id) { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
        label = "activity card scale",
    )
    RichCard(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(activity.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${activity.club} · ${formatDate(activity)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("${formatHours(activity.hours)} h", style = MaterialTheme.typography.titleLarge)
            }
            if (activity.reflection.isNotBlank()) {
                Text(
                    activity.reflection,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.xs)) {
                durationBadges(activity).forEach { (label, color) -> StatusPill(label, color) }
                StatusPill(
                    if (activity.reflectionComplete) "Reflected" else "Reflection needed",
                    if (activity.reflectionComplete) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ActivityDetailsDialog(activity: CasActivity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(activity.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
                Text("${activity.club} · ${formatDate(activity)}")
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.xs)) {
                    durationBadges(activity).forEach { (label, color) -> StatusPill(label, color) }
                }
                Text(
                    activity.reflection.ifBlank { "No reflection has been added to this record." },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

private fun durationBadges(activity: CasActivity): List<Pair<String, Color>> = buildList {
    if (activity.creativityHours > 0) add("C ${formatHours(activity.creativityHours)}" to Color(0xFF9C3A65))
    if (activity.activityHours > 0) add("A ${formatHours(activity.activityHours)}" to Color(0xFF3B7C4B))
    if (activity.serviceHours > 0) add("S ${formatHours(activity.serviceHours)}" to Color(0xFF356CA5))
}

private fun formatDate(activity: CasActivity): String = activity.date
    ?.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH))
    ?: "Date unavailable"

private fun formatHours(hours: Double): String = if (hours % 1.0 == 0.0) {
    hours.toInt().toString()
} else {
    String.format(Locale.ENGLISH, "%.1f", hours)
}

private const val ALL_CLUBS = "All clubs"
