package com.computerization.outspire.feature.cas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.computerization.outspire.data.model.LearningOutcome
import com.computerization.outspire.designsystem.AppSpace

@Composable
fun ReflectionEditorDialog(
    state: ReflectionEditorState,
    saving: Boolean,
    onChange: ((ReflectionEditorState) -> ReflectionEditorState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.isEdit) "Edit Reflection" else "Add Reflection") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpace.xs),
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { v -> onChange { it.copy(title = v) } },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.summary,
                    onValueChange = { v -> onChange { it.copy(summary = v) } },
                    label = { Text("Summary") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.content,
                    onValueChange = { v -> onChange { it.copy(content = v) } },
                    label = { Text("Content") },
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Learning outcome", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpace.xxs),
                ) {
                    LearningOutcome.values().forEach { lo ->
                        FilterChip(
                            selected = state.outcome == lo.code,
                            onClick = {
                                onChange {
                                    it.copy(outcome = if (it.outcome == lo.code) null else lo.code)
                                }
                            },
                            label = { Text(lo.label) },
                        )
                    }
                }
                if (state.error != null) {
                    Text(
                        state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !saving) {
                Text(if (saving) "Saving…" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") }
        },
    )
}
