package dev.outspire.android.data.repository

import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.model.CasCategory
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.SchoolWeek
import dev.outspire.android.data.model.SubjectScore
import java.time.DayOfWeek
import java.time.LocalDate

internal object DemoData {
    val schedule = buildList {
        val subjects = listOf(
            Triple("Mathematics AA HL", "Ms Chen", "A401"),
            Triple("English B HL", "Mr Smith", "B302"),
            Triple("Chemistry SL", "Dr Wang", "Lab 2"),
            Triple("Physics HL", "Mr Liu", "Lab 1"),
            Triple("Economics HL", "Ms Zhao", "C205"),
            Triple("Chinese A SL", "Ms Li", "B104"),
        )
        for (day in SchoolWeek.days) {
            for (period in 1..9) {
                if (period == 5 && day == DayOfWeek.WEDNESDAY) continue
                val value = subjects[(period + day.value) % subjects.size]
                add(ScheduleEntry(day, period, value.first, value.second, value.third))
            }
        }
    }

    val scores = listOf(
        SubjectScore("Mathematics AA HL", "Term 2", "91", "7"),
        SubjectScore("English B HL", "Term 2", "88", "7"),
        SubjectScore("Chemistry SL", "Term 2", "86", "6"),
        SubjectScore("Physics HL", "Term 2", "93", "7"),
        SubjectScore("Economics HL", "Term 2", "84", "6"),
    )

    val activities = listOf(
        CasActivity(
            id = "1",
            title = "Sub-culture Week planning",
            club = "Doki-doki ACGN",
            date = LocalDate.now().minusDays(8),
            categories = setOf(CasCategory.CREATIVITY, CasCategory.SERVICE),
            hours = 5,
            reflectionComplete = true,
        ),
        CasActivity(
            id = "2",
            title = "Model evaluation workshop",
            club = "AI-Lab",
            date = LocalDate.now().minusDays(18),
            categories = setOf(CasCategory.CREATIVITY),
            hours = 3,
            reflectionComplete = false,
        ),
        CasActivity(
            id = "3",
            title = "Campus technology support",
            club = "Computerization",
            date = LocalDate.now().minusDays(27),
            categories = setOf(CasCategory.SERVICE),
            hours = 4,
            reflectionComplete = true,
        ),
    )
}
