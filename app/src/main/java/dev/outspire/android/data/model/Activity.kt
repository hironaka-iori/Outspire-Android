package dev.outspire.android.data.model

import java.time.LocalDate

enum class CasCategory { CREATIVITY, ACTIVITY, SERVICE }

data class CasActivity(
    val id: String,
    val title: String,
    val club: String,
    val date: LocalDate?,
    val creativityHours: Double = 0.0,
    val activityHours: Double = 0.0,
    val serviceHours: Double = 0.0,
    val reflection: String = "",
    val confirmed: Boolean? = null,
) {
    val categories: Set<CasCategory>
        get() = buildSet {
            if (creativityHours > 0) add(CasCategory.CREATIVITY)
            if (activityHours > 0) add(CasCategory.ACTIVITY)
            if (serviceHours > 0) add(CasCategory.SERVICE)
        }

    val hours: Double
        get() = creativityHours + activityHours + serviceHours

    val reflectionComplete: Boolean
        get() = reflection.isNotBlank()
}
