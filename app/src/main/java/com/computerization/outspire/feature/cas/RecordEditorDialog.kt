package com.computerization.outspire.feature.cas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.computerization.outspire.designsystem.AppSpace

@Composable
fun RecordEditorDialog(
    state: RecordEditorState,
    saving: Boolean,
    onChange: ((RecordEditorState) -> RecordEditorState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val wordCount = state.reflection.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.isEdit) "Edit Record" else "Add Record") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpace.xs),
            ) {
                OutlinedTextField(
                    value = state.date,
                    onValueChange = { v -> onChange { it.copy(date = v) } },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.theme,
                    onValueChange = { v -> onChange { it.copy(theme = v) } },
                    label = { Text("Theme") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpace.xs)) {
                    OutlinedTextField(
                        value = state.cDuration,
                        onValueChange = { v -> onChange { it.copy(cDuration = v) } },
                        label = { Text("C") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = state.aDuration,
                        onValueChange = { v -> onChange { it.copy(aDuration = v) } },
                        label = { Text("A") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = state.sDuration,
                        onValueChange = { v -> onChange { it.copy(sDuration = v) } },
                        label = { Text("S") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = state.reflection,
                    onValueChange = { v -> onChange { it.copy(reflection = v) } },
                    label = { Text("Reflection (≥80 words)") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "$wordCount / 80 words",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (wordCount >= 80) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
