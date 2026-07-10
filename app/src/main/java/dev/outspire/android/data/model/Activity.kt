package dev.outspire.android.data.model

import java.time.LocalDate

enum class CasCategory { CREATIVITY, ACTIVITY, SERVICE }

data class CasActivity(
    val id: String,
    val title: String,
    val club: String,
    val date: LocalDate,
    val categories: Set<CasCategory>,
    val hours: Int,
    val reflectionComplete: Boolean,
)
