package com.computerization.outspire.feature.academic

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.computerization.outspire.data.model.DomainScore
import com.computerization.outspire.designsystem.AppRadius
import com.computerization.outspire.designsystem.AppSpace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicScreen(
    viewModel: AcademicViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpace.md, vertical = AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        Text(
            text = "Academic",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        var expanded by remember { mutableStateOf(false) }
        val selectedLabel = state.yearOptions
            .firstOrNull { it.id == state.selectedYearId }?.name
            ?: "Select term"

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Term") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                state.yearOptions.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt.name) },
                        onClick = {
                            viewModel.selectYear(opt.id)
                            expanded = false
                        },
                    )
                }
            }
        }

        when {
            state.loading -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = state.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = { viewModel.retry() }) { Text("Retry") }
                }
            }
            state.scores.isEmpty() -> {
                Text(
                    text = "No scores for this term",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing)) {
                    items(state.scores, key = { it.subject }) { ScoreRow(it) }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(score: DomainScore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(AppSpace.cardPadding)) {
            Text(
                text = score.subject,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                score.terms.forEach { t ->
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = {
                            val ib = if (t.ib.isNotBlank()) " / ${t.ib}" else ""
                            Text("${t.label}: ${t.raw}$ib")
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
        }
    }
}
