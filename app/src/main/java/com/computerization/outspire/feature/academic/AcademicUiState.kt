package com.computerization.outspire.feature.academic

import com.computerization.outspire.data.model.DomainScore
import com.computerization.outspire.data.remote.dto.YearOption

data class AcademicUiState(
    val yearOptions: List<YearOption> = emptyList(),
    val selectedYearId: String? = null,
    val loading: Boolean = true,
    val scores: List<DomainScore> = emptyList(),
    val error: String? = null,
)
