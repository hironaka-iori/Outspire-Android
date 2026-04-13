package com.computerization.outspire.feature.cas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.computerization.outspire.data.model.DomainCasGroup
import com.computerization.outspire.designsystem.AppSpace
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun BrowseClubsTab(
    state: BrowseState,
    joiningId: String?,
    onLoadMore: () -> Unit,
    onJoin: (DomainCasGroup) -> Unit,
    onRetry: () -> Unit,
) {
    if (state.error != null && state.items.isEmpty()) {
        ErrorBlock(state.error, onRetry)
        return
    }
    if (state.items.isEmpty() && state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        LaunchedEffect(Unit) { onLoadMore() }
        return
    }
    if (state.items.isEmpty()) {
        LaunchedEffect(Unit) { onLoadMore() }
        return
    }

    val listState = rememberLazyListState()
    val needMore = remember(state) {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.items.size - 3 && state.hasMore && !state.loading
        }
    }
    LaunchedEffect(listState, state.items.size, state.pageIndex) {
        snapshotFlow { needMore.value }.distinctUntilChanged().collect { trigger ->
            if (trigger) onLoadMore()
        }
    }

    LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing)) {
        items(state.items, key = { it.id }) { group ->
            GroupCard(
                group = group,
                trailing = {
                    Button(
                        onClick = { onJoin(group) },
                        enabled = joiningId == null,
                    ) {
                        Text(if (joiningId == group.id) "Joining…" else "Join")
                    }
                },
            )
        }
        if (state.loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(AppSpace.md), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        if (state.error != null) {
            item {
                Text(
                    state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(AppSpace.md),
                )
            }
        }
    }
}
