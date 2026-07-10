package dev.outspire.android.feature.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.model.CasCategory
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.AppSpace
import dev.outspire.android.designsystem.ErrorCard
import dev.outspire.android.designsystem.GradientCard
import dev.outspire.android.designsystem.LoadingCard
import dev.outspire.android.designsystem.RichCard
import dev.outspire.android.designsystem.ScreenTitle
import dev.outspire.android.designsystem.StatusPill
import java.time.format.DateTimeFormatter

@Composable
fun ActivitiesScreen(
    state: ActivitiesUiState,
    user: User?,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        Spacer(Modifier.padding(top = AppSpace.xs))
        ScreenTitle("Activities", "Creativity, activity, and service")

        if (user == null) {
            RichCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpace.md)) {
                    Icon(Icons.Default.Login, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Sign in to see your CAS records.", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = onSignIn) { Text("Open account") }
                }
            }
            return@Column
        }

        if (state.activities.isNotEmpty()) {
            GradientCard(
                colors = listOf(Color(0xFF2E7D5A), Color(0xFF365F9D)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text("Recorded CAS time", color = Color.White.copy(alpha = 0.8f))
                        Text(
                            "${state.activities.sumOf(CasActivity::hours)} hours",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                        )
                    }
                    Text(
                        "${state.activities.count(CasActivity::reflectionComplete)}/${state.activities.size} reflected",
                        color = Color.White,
                    )
                }
            }
        }

        when {
            state.isLoading && state.activities.isEmpty() -> LoadingCard("Loading CAS records…")
            state.error != null && state.activities.isEmpty() -> ErrorCard(state.error)
            else -> state.activities.forEach { ActivityCard(it) }
        }
        state.error?.takeIf { state.activities.isNotEmpty() }?.let { ErrorCard(it) }
        Spacer(Modifier.padding(bottom = 88.dp))
    }
}

@Composable
private fun ActivityCard(activity: CasActivity) {
    RichCard {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpace.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(activity.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${activity.club} · ${activity.date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("${activity.hours} h", style = MaterialTheme.typography.titleLarge)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.xs)) {
                activity.categories.forEach { category ->
                    val (label, color) = when (category) {
                        CasCategory.CREATIVITY -> "C" to Color(0xFF9C3A65)
                        CasCategory.ACTIVITY -> "A" to Color(0xFF3B7C4B)
                        CasCategory.SERVICE -> "S" to Color(0xFF356CA5)
                    }
                    StatusPill(label, color)
                }
                StatusPill(
                    if (activity.reflectionComplete) "Reflected" else "Reflection needed",
                    if (activity.reflectionComplete) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
