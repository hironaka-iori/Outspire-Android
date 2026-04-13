package com.computerization.outspire.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.computerization.outspire.designsystem.AppSpace
import com.computerization.outspire.feature.today.components.CountdownCard
import com.computerization.outspire.feature.today.components.NextClassCard
import com.computerization.outspire.feature.today.components.WeatherBadge
import com.computerization.outspire.feature.today.components.WeekTimetableSection

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val week by viewModel.weekFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpace.md, vertical = AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
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
            is TodayUiState.InClass -> {
                CountdownCard(
                    title = "Current class · ends in",
                    subtitle = s.current.subject,
                    remaining = s.remaining,
                )
            }
            is TodayUiState.Break -> {
                CountdownCard(
                    title = "Next class · starts in",
                    subtitle = s.next.subject,
                    remaining = s.until,
                )
                NextClassCard(clazz = s.next)
            }
            is TodayUiState.Done -> {
                Text(
                    text = if (s.isWeekend) "Weekend — enjoy your rest ✦"
                           else "All classes wrapped for today ✦",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            is TodayUiState.Error -> {
                Text(
                    text = "Couldn't load timetable: ${s.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        WeekTimetableSection(week = week)
    }
}
