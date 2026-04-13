package com.computerization.outspire.feature.cas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.computerization.outspire.data.model.DomainCasGroup
import com.computerization.outspire.designsystem.AppRadius
import com.computerization.outspire.designsystem.AppSpace

@Composable
fun MyClubsTab(
    state: AsyncList<DomainCasGroup>,
    onRetry: () -> Unit,
    onOpen: (DomainCasGroup) -> Unit,
) {
    when (state) {
        AsyncList.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is AsyncList.Error -> ErrorBlock(state.message, onRetry)
        is AsyncList.Data -> {
            if (state.items.isEmpty()) {
                Text("You haven't joined any clubs yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing)) {
                    items(state.items, key = { it.id }) { group ->
                        GroupCard(group = group, onClick = { onOpen(group) })
                    }
                }
            }
        }
    }
}

@Composable
internal fun GroupCard(
    group: DomainCasGroup,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(AppRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(AppSpace.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpace.xxs)) {
            Text(group.name, style = MaterialTheme.typography.titleMedium)
            if (group.teacher.isNotBlank()) {
                Text("Teacher · ${group.teacher}", style = MaterialTheme.typography.bodySmall)
            }
            if (group.description.isNotBlank()) {
                Text(
                    group.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                )
            }
            if (trailing != null) {
                Box(Modifier.fillMaxWidth().padding(top = AppSpace.xs), contentAlignment = Alignment.CenterEnd) {
                    trailing()
                }
            }
        }
    }
}

@Composable
internal fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpace.xs)) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry) { Text("Retry") }
    }
}
