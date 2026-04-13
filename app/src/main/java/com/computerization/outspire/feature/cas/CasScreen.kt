package com.computerization.outspire.feature.cas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.computerization.outspire.designsystem.AppSpace

@Composable
fun CasScreen(viewModel: CasViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpace.md, vertical = AppSpace.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
        ) {
            Text(
                text = "CAS",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            if (state.selectedGroup != null) {
                ClubDetailScreen(
                    group = state.selectedGroup!!,
                    records = state.records,
                    reflections = state.reflections,
                    onBack = { viewModel.closeGroup() },
                    onRetry = { viewModel.retryGroupDetail() },
                    onAddRecord = { viewModel.openAddRecord() },
                    onEditRecord = { viewModel.openEditRecord(it) },
                    onDeleteRecord = { viewModel.deleteRecord(it) },
                    onAddReflection = { viewModel.openAddReflection() },
                    onEditReflection = { viewModel.openEditReflection(it) },
                    onDeleteReflection = { viewModel.deleteReflection(it) },
                )
            } else {
                val tabs = CasTab.values()
                TabRow(selectedTabIndex = tabs.indexOf(state.selectedTab)) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = state.selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            text = { Text(tab.label()) },
                        )
                    }
                }
                when (state.selectedTab) {
                    CasTab.MyClubs -> MyClubsTab(
                        state = state.myClubs,
                        onRetry = { viewModel.retryMyClubs() },
                        onOpen = { viewModel.openGroup(it) },
                    )
                    CasTab.Browse -> BrowseClubsTab(
                        state = state.browse,
                        joiningId = state.joiningId,
                        onLoadMore = { viewModel.loadNextBrowsePage() },
                        onJoin = { viewModel.join(it) },
                        onRetry = { viewModel.retryBrowse() },
                    )
                    CasTab.Evaluation -> EvaluationTab(
                        state = state.evaluation,
                        onRetry = { viewModel.retryEvaluation() },
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { Snackbar(it) }

        state.recordEditor?.let { editor ->
            RecordEditorDialog(
                state = editor,
                saving = state.savingEditor,
                onChange = { transform -> viewModel.updateRecordEditor(transform) },
                onSave = { viewModel.saveRecord() },
                onDismiss = { viewModel.closeRecordEditor() },
            )
        }
        state.reflectionEditor?.let { editor ->
            ReflectionEditorDialog(
                state = editor,
                saving = state.savingEditor,
                onChange = { transform -> viewModel.updateReflectionEditor(transform) },
                onSave = { viewModel.saveReflection() },
                onDismiss = { viewModel.closeReflectionEditor() },
            )
        }
    }
}

private fun CasTab.label(): String = when (this) {
    CasTab.MyClubs -> "My Clubs"
    CasTab.Browse -> "Browse"
    CasTab.Evaluation -> "Evaluation"
}
