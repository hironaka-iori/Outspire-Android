package com.computerization.outspire.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.computerization.outspire.designsystem.AppRadius
import com.computerization.outspire.designsystem.AppSpace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpace.md, vertical = AppSpace.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.card),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(
                modifier = Modifier.padding(AppSpace.cardPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Account", style = MaterialTheme.typography.titleMedium)
                Text("Student ID · ${state.user?.studentId ?: "—"}")
                Text("Username · ${state.user?.username ?: "—"}")
            }
        }

        var expanded by remember { mutableStateOf(false) }
        val selectedLabel = state.yearOptions
            .firstOrNull { it.id == state.currentYearId }?.name
            ?: "—"

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Current term") },
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

        Button(
            onClick = { viewModel.logout() },
            enabled = !state.loggingOut,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.loggingOut) "Signing out…" else "Logout")
        }
    }
}
