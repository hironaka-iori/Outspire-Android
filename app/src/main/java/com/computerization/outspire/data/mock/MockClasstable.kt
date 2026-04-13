package com.computerization.outspire.data.mock

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class MockClass(
    val subject: String,
    val teacher: String,
    val room: String,
    val start: LocalTime,
    val end: LocalTime,
)

object MockClasstable {

    val fixed: List<MockClass> = listOf(
        MockClass("English Literature", "Ms. Chen", "A203", LocalTime(8, 0), LocalTime(8, 45)),
        MockClass("Higher Maths", "Mr. Liu", "B105", LocalTime(8, 55), LocalTime(9, 40)),
        MockClass("Physics", "Dr. Wang", "C301", LocalTime(9, 55), LocalTime(10, 40)),
        MockClass("Chinese", "Ms. Zhao", "A108", LocalTime(10, 50), LocalTime(11, 35)),
        MockClass("Economics", "Mr. Smith", "B204", LocalTime(13, 0), LocalTime(13, 45)),
        MockClass("PE", "Coach Li", "Gym", LocalTime(13, 55), LocalTime(14, 40)),
        MockClass("CAS Workshop", "Ms. Chen", "A203", LocalTime(14, 50), LocalTime(15, 35)),
    )

    /**
     * Demo-friendly 课表: 以当前时间为锚点生成 5 节课,
     * 保证任何时候打开 app 都能看到"课中 + 接下来几节"。
     */
    val today: List<MockClass>
        get() {
            val now = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).time
            val anchorSec = (now.hour * 3600 + now.minute * 60) - 10 * 60
            val subjects = listOf(
                Triple("Higher Maths", "Mr. Liu", "B105"),
                Triple("English Literature", "Ms. Chen", "A203"),
                Triple("Physics", "Dr. Wang", "C301"),
                Triple("Economics", "Mr. Smith", "B204"),
                Triple("CAS Workshop", "Ms. Chen", "A203"),
            )
            return subjects.mapIndexed { i, (s, t, r) ->
                val start = (anchorSec + i * 55 * 60).coerceIn(0, 23 * 3600 + 59 * 60)
                val end = (start + 45 * 60).coerceAtMost(23 * 3600 + 59 * 60)
                MockClass(
                    subject = s,
                    teacher = t,
                    room = r,
                    start = LocalTime(start / 3600, (start % 3600) / 60),
                    end = LocalTime(end / 3600, (end % 3600) / 60),
                )
            }
        }
}
