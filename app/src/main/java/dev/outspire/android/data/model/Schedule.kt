package dev.outspire.android.data.model

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class ClassPeriod(
    val number: Int,
    val start: LocalTime,
    val end: LocalTime,
) {
    fun progressAt(time: LocalTime): Float = when {
        time < start -> 0f
        time >= end -> 1f
        else -> {
            val total = Duration.between(start, end).seconds.toFloat()
            val elapsed = Duration.between(start, time).seconds.toFloat()
            (elapsed / total).coerceIn(0f, 1f)
        }
    }

    fun isActiveAt(time: LocalTime): Boolean = time >= start && time < end
}

data class ScheduleEntry(
    val day: DayOfWeek,
    val period: Int,
    val subject: String,
    val teacher: String? = null,
    val room: String? = null,
    val isSelfStudy: Boolean = false,
) {
    val representsSelfStudy: Boolean
        get() = isSelfStudy ||
            subject.contains("self-study", ignoreCase = true) ||
            subject.contains("self study", ignoreCase = true)
}

data class ClassInfo(
    val teacher: String?,
    val subject: String,
    val room: String?,
    val isSelfStudy: Boolean,
)

object ClassInfoParser {
    fun parse(raw: String): ClassInfo {
        val parts = raw
            .replace("<br>", "\n", ignoreCase = true)
            .lines()
            .map(String::trim)
            .filter(String::isNotEmpty)

        if (parts.isEmpty()) {
            return ClassInfo(null, "Self-Study", null, true)
        }

        val teacher = parts.getOrNull(0)
        val subject = parts.getOrNull(1) ?: parts.first()
        val room = parts.getOrNull(2)
        val selfStudy = subject.contains("self-study", ignoreCase = true) ||
            subject.contains("self study", ignoreCase = true)
        return ClassInfo(teacher, if (selfStudy) "Self-Study" else subject, room, selfStudy)
    }
}

object SchoolPeriods {
    val all = listOf(
        ClassPeriod(1, LocalTime.of(8, 15), LocalTime.of(8, 55)),
        ClassPeriod(2, LocalTime.of(9, 5), LocalTime.of(9, 45)),
        ClassPeriod(3, LocalTime.of(9, 55), LocalTime.of(10, 35)),
        ClassPeriod(4, LocalTime.of(10, 45), LocalTime.of(11, 25)),
        ClassPeriod(5, LocalTime.of(12, 30), LocalTime.of(13, 10)),
        ClassPeriod(6, LocalTime.of(13, 20), LocalTime.of(14, 0)),
        ClassPeriod(7, LocalTime.of(14, 10), LocalTime.of(14, 50)),
        ClassPeriod(8, LocalTime.of(15, 0), LocalTime.of(15, 40)),
        ClassPeriod(9, LocalTime.of(15, 50), LocalTime.of(16, 30)),
    )
}

object SchoolWeek {
    val days = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
    )
}

object SchoolTime {
    val zone: ZoneId = ZoneId.of("Asia/Shanghai")

    fun now(): LocalDateTime = LocalDateTime.now(zone)
}

enum class PeriodPhase {
    PAST,
    CURRENT,
    UPCOMING,
}

data class TimelinePeriod(
    val period: ClassPeriod,
    val entry: ScheduleEntry,
    val phase: PeriodPhase,
    val progress: Float,
    val remainingSeconds: Long,
)

data class TodayTimeline(
    val periods: List<TimelinePeriod>,
    val classCount: Int,
    val hasRemainingClasses: Boolean,
)

data class PeriodStatus(
    val period: ClassPeriod,
    val entry: ScheduleEntry,
    val active: Boolean,
    val progress: Float,
)

object ScheduleResolver {
    fun todayTimeline(
        schedule: List<ScheduleEntry>,
        now: LocalDateTime,
    ): TodayTimeline {
        val day = now.dayOfWeek
        val entries = if (day in SchoolWeek.days) daySchedule(schedule, day) else emptyList()
        val classCount = entries.count { !it.representsSelfStudy }
        val currentTime = now.toLocalTime()
        val hasRemainingClasses = entries.isNotEmpty() &&
            classCount > 0 &&
            currentTime < SchoolPeriods.all.last().end

        val periods = entries.map { entry ->
            val period = SchoolPeriods.all.first { it.number == entry.period }
            val phase = when {
                period.isActiveAt(currentTime) -> PeriodPhase.CURRENT
                currentTime >= period.end -> PeriodPhase.PAST
                else -> PeriodPhase.UPCOMING
            }
            TimelinePeriod(
                period = period,
                entry = entry,
                phase = phase,
                progress = if (phase == PeriodPhase.CURRENT) period.progressAt(currentTime) else 0f,
                remainingSeconds = if (phase == PeriodPhase.CURRENT) {
                    Duration.between(currentTime, period.end).seconds.coerceAtLeast(0)
                } else {
                    0
                },
            )
        }

        return TodayTimeline(
            periods = periods,
            classCount = classCount,
            hasRemainingClasses = hasRemainingClasses,
        )
    }

    fun currentOrNext(
        schedule: List<ScheduleEntry>,
        now: LocalDateTime,
        dayOverride: DayOfWeek? = null,
        treatOverrideAsToday: Boolean = false,
    ): PeriodStatus? {
        val selectedDay = dayOverride ?: now.dayOfWeek
        if (selectedDay !in SchoolWeek.days) return null

        val entries = schedule.filter { it.day == selectedDay }.associateBy { it.period }
        val effectiveTime = if (dayOverride == null || treatOverrideAsToday) now.toLocalTime() else LocalTime.MIN
        val current = SchoolPeriods.all.firstOrNull { it.isActiveAt(effectiveTime) }
        if (current != null) {
            val entry = entries[current.number] ?: selfStudy(selectedDay, current.number)
            return PeriodStatus(current, entry, true, current.progressAt(effectiveTime))
        }

        return SchoolPeriods.all
            .asSequence()
            .filter { it.start > effectiveTime }
            .mapNotNull { period -> entries[period.number]?.let { period to it } }
            .firstOrNull()
            ?.let { (period, entry) -> PeriodStatus(period, entry, false, 0f) }
    }

    fun daySchedule(schedule: List<ScheduleEntry>, day: DayOfWeek): List<ScheduleEntry> {
        val byPeriod = schedule.filter { it.day == day }.associateBy { it.period }
        return SchoolPeriods.all.map { byPeriod[it.number] ?: selfStudy(day, it.number) }
    }

    private fun selfStudy(day: DayOfWeek, period: Int) = ScheduleEntry(
        day = day,
        period = period,
        subject = "Self-Study",
        isSelfStudy = true,
    )
}
