package com.computerization.outspire.feature.cas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.computerization.outspire.data.model.DomainEvaluation
import com.computerization.outspire.designsystem.AppRadius
import com.computerization.outspire.designsystem.AppSpace

@Composable
fun EvaluationTab(state: AsyncValue<DomainEvaluation>, onRetry: () -> Unit) {
    when (state) {
        AsyncValue.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is AsyncValue.Error -> ErrorBlock(state.message, onRetry)
        is AsyncValue.Data -> EvaluationContent(state.value)
    }
}

@Composable
private fun EvaluationContent(eval: DomainEvaluation) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpace.cardSpacing)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.card),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                Modifier.padding(AppSpace.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpace.xs),
            ) {
                Text("Semester scores", style = MaterialTheme.typography.titleMedium)
                ScoreLine("Records (30%)", eval.recLevel)
                ScoreLine("Reflections (40%)", eval.refLevel)
                ScoreLine("Talk (30%)", eval.talk)
                ScoreLine("Final", eval.finalScore)
            }
        }

        if (eval.groups.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppRadius.card),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    Modifier.padding(AppSpace.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpace.xs),
                ) {
                    Text("Hours by club", style = MaterialTheme.typography.titleMedium)
                    eval.groups.forEach { row ->
                        Column {
                            Text(row.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "C ${row.cDuration} · A ${row.aDuration} · S ${row.sDuration}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium)
    }
}
