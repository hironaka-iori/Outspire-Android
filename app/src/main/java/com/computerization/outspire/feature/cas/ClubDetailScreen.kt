package com.computerization.outspire.feature.cas

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import com.computerization.outspire.data.model.DomainCasGroup
import com.computerization.outspire.data.model.DomainRecord
import com.computerization.outspire.data.model.DomainReflection
import com.computerization.outspire.designsystem.AppRadius
import com.computerization.outspire.designsystem.AppSpace

private enum class DetailTab { Records, Reflections }

@Composable
fun ClubDetailScreen(
    group: DomainCasGroup,
    records: AsyncList<DomainRecord>,
    reflections: AsyncList<DomainReflection>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAddRecord: () -> Unit,
    onEditRecord: (DomainRecord) -> Unit,
    onDeleteRecord: (DomainRecord) -> Unit,
    onAddReflection: () -> Unit,
    onEditReflection: (DomainReflection) -> Unit,
    onDeleteReflection: (DomainReflection) -> Unit,
) {
    var tab by remember { mutableStateOf(DetailTab.Records) }
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(group.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onBack) { Text("Back") }
            }

            IntroCard(group)

            TabRow(selectedTabIndex = tab.ordinal) {
                DetailTab.values().forEach { t ->
                    Tab(
                        selected = tab == t,
                        onClick = { tab = t },
                        text = { Text(if (t == DetailTab.Records) "Records" else "Reflections") },
                    )
                }
            }

            when (tab) {
                DetailTab.Records -> RecordsList(records, onRetry, onEditRecord, onDeleteRecord)
                DetailTab.Reflections -> ReflectionsList(reflections, onRetry, onEditReflection, onDeleteReflection)
            }
        }

        ExtendedFloatingActionButton(
            onClick = { if (tab == DetailTab.Records) onAddRecord() else onAddReflection() },
            text = { Text(if (tab == DetailTab.Records) "Add record" else "Add reflection") },
            icon = { Text("+", style = MaterialTheme.typography.titleLarge) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AppSpace.md),
        )
    }
}

@Composable
private fun IntroCard(group: DomainCasGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            Modifier.padding(AppSpace.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpace.xxs),
        ) {
            if (group.groupNo.isNotBlank()) {
                Text(
                    "Group No · ${group.groupNo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (group.teacher.isNotBlank()) {
                Text(
                    "Teacher · ${group.teacher}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (group.description.isNotBlank()) {
                Text(
                    group.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                Text(
                    "No description provided.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RecordsList(
    state: AsyncList<DomainRecord>,
    onRetry: () -> Unit,
    onEdit: (DomainRecord) -> Unit,
    onDelete: (DomainRecord) -> Unit,
) {
    when (state) {
        AsyncList.Loading -> CenterLoader()
        is AsyncList.Error -> ErrorBlock(state.message, onRetry)
        is AsyncList.Data -> if (state.items.isEmpty()) {
            Text("No records yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            items(state.items, key = { it.id }) { record ->
                RecordRow(record, onEdit = { onEdit(record) }, onDelete = { onDelete(record) })
            }
        }
    }
}

@Composable
private fun ReflectionsList(
    state: AsyncList<DomainReflection>,
    onRetry: () -> Unit,
    onEdit: (DomainReflection) -> Unit,
    onDelete: (DomainReflection) -> Unit,
) {
    when (state) {
        AsyncList.Loading -> CenterLoader()
        is AsyncList.Error -> ErrorBlock(state.message, onRetry)
        is AsyncList.Data -> if (state.items.isEmpty()) {
            Text("No reflections yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else LazyColumn(
            verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            items(state.items, key = { it.id }) { reflection ->
                ReflectionRow(reflection, onEdit = { onEdit(reflection) }, onDelete = { onDelete(reflection) })
            }
        }
    }
}

@Composable
private fun CenterLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecordRow(record: DomainRecord, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirming by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onEdit, onLongClick = { confirming = true }),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(AppSpace.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpace.xxs)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(record.theme.ifBlank { "(untitled)" }, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (record.confirmed) "Confirmed" else "Pending",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (record.confirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (record.date.isNotBlank()) {
                Text(record.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.xs)) {
                AssistChip(onClick = {}, enabled = false, label = { Text("C ${record.cDuration}") })
                AssistChip(onClick = {}, enabled = false, label = { Text("A ${record.aDuration}") })
                AssistChip(onClick = {}, enabled = false, label = { Text("S ${record.sDuration}") })
            }
            if (record.reflection.isNotBlank()) {
                Text(record.reflection, style = MaterialTheme.typography.bodySmall, maxLines = 4)
            }
        }
    }
    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Delete record?") },
            text = { Text(record.theme.ifBlank { "(untitled)" }) },
            confirmButton = {
                TextButton(onClick = { confirming = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReflectionRow(reflection: DomainReflection, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirming by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onEdit, onLongClick = { confirming = true }),
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(AppSpace.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpace.xxs)) {
            Text(reflection.title.ifBlank { "(untitled)" }, style = MaterialTheme.typography.titleMedium)
            if (reflection.outcome != null) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(reflection.outcome.label) },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
            if (reflection.summary.isNotBlank()) {
                Text(reflection.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (reflection.contentPreview.isNotBlank()) {
                Text(reflection.contentPreview, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Delete reflection?") },
            text = { Text(reflection.title.ifBlank { "(untitled)" }) },
            confirmButton = {
                TextButton(onClick = { confirming = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text("Cancel") } },
        )
    }
}

