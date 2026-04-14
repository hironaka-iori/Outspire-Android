package com.computerization.outspire.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.computerization.outspire.designsystem.AppSpace
import com.computerization.outspire.designsystem.staggeredEntry
import com.computerization.outspire.feature.today.components.NoClassCard
import com.computerization.outspire.feature.today.components.QuickLinksCard
import com.computerization.outspire.feature.today.components.UnifiedScheduleCard
import com.computerization.outspire.feature.today.components.WeatherBadge
import com.computerization.outspire.feature.today.components.WeekendCard
import kotlinx.coroutines.launch

@Composable
fun TodayScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var animateCards by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateCards = true }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpace.md, vertical = AppSpace.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpace.lg),
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            WeatherBadge(modifier = Modifier.align(Alignment.Start))

            when (val s = state) {
                TodayUiState.Loading -> {
                    Text("Loading…", color = MaterialTheme.colorScheme.onBackground)
                }
                is TodayUiState.Weekday -> {
                    UnifiedScheduleCard(
                        dayName = s.dayName,
                        classes = s.classes,
                        activeIndex = s.activeIndex,
                        nowLocal = s.now,
                        modifier = Modifier.staggeredEntry(0, animateCards),
                    )
                }
                is TodayUiState.DayDone -> {
                    if (s.isWeekend) {
                        WeekendCard(modifier = Modifier.staggeredEntry(0, animateCards))
                    } else {
                        NoClassCard(
                            isDimmed = s.isAfterSchool,
                            modifier = Modifier.staggeredEntry(0, animateCards),
                        )
                    }
                }
                is TodayUiState.Error -> {
                    Text(
                        text = "Couldn't load timetable: ${s.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            QuickLinksCard(
                modifier = Modifier.staggeredEntry(1, animateCards),
                onClubs = { onNavigate("cas") },
                onDining = {
                    scope.launch { snackbarHostState.showSnackbar("Dining · coming soon") }
                },
                onActivities = { onNavigate("cas") },
                onReflect = { onNavigate("cas") },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { Snackbar(it) }
    }
}
