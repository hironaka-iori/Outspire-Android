package com.computerization.outspire.data.repository

import com.computerization.outspire.data.local.SecureCredentialStore
import com.computerization.outspire.data.model.DomainClass
import com.computerization.outspire.data.remote.TimetableService
import com.computerization.outspire.data.remote.dto.LessonSlot
import com.computerization.outspire.data.remote.dto.TimetableDto
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimetableRepository @Inject constructor(
    private val service: TimetableService,
    private val store: SecureCredentialStore,
    private val yearRepository: YearRepository,
) {

    private var cached: TimetableDto? = null
    private var cachedYearId: String? = null

    suspend fun todayClasses(): Result<List<DomainClass>> = runCatching {
        val dow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfWeek
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return@runCatching emptyList()
        val dayIndex = dow.ordinal
        mapDay(loadTimetable(), dayIndex)
    }

    suspend fun weekClasses(): Result<Map<DayOfWeek, List<DomainClass>>> = runCatching {
        val dto = loadTimetable()
        val days = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
        )
        days.associateWith { day -> mapDay(dto, day.ordinal) }
    }

    private suspend fun loadTimetable(): TimetableDto {
        val yearId = yearRepository.currentYearId.value
            ?: yearRepository.ensureOptions().getOrThrow().let {
                yearRepository.currentYearId.value
                    ?: throw IllegalStateException("No yearId resolved")
            }
        val studentId = store.studentId
            ?: throw IllegalStateException("No student id in credential store")

        cached?.takeIf { cachedYearId == yearId }?.let { return it }
        val fresh = service.getTimetableByStudent(yearId.toInt(), studentId)
        cached = fresh
        cachedYearId = yearId
        return fresh
    }

    companion object {
        internal fun mapDay(dto: TimetableDto, dayIndex: Int): List<DomainClass> {
            return dto.periods.mapNotNull { row ->
                val lessonNumber = row.lessonNumber.toIntOrNull() ?: return@mapNotNull null
                val slot: LessonSlot = row.slots.getOrNull(dayIndex) ?: return@mapNotNull null
                val period = BellSchedule[lessonNumber] ?: return@mapNotNull null
                DomainClass(
                    subject = slot.subject,
                    teacher = slot.teacher,
                    room = slot.room,
                    start = period.start,
                    end = period.end,
                )
            }.sortedBy { it.start }
        }
    }
}
