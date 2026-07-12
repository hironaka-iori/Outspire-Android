package dev.outspire.android.feature.academic

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.SemesterOption
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.OutspireTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class AcademicScreenInteractionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val monday = LocalDate.of(2026, 7, 6)
    private val user = User(1, "s20248401", "Test Student", null)
    private val state = AcademicUiState(
        schedule = listOf(
            ScheduleEntry(
                day = DayOfWeek.MONDAY,
                period = 1,
                subject = "Mathematics AA HL",
                teacher = "Ms Chen",
                room = "A401",
            ),
        ),
        semesters = listOf(
            SemesterOption("t2", "2025-2026 (T2)"),
            SemesterOption("t1", "2025-2026 (T1)"),
        ),
        selectedSemesterId = "t2",
        selectedDate = monday,
        now = LocalDateTime.of(2026, 7, 6, 8, 30),
    )

    @Test
    fun toolbarAndWeekButtonsInvokeTheirActions() {
        var todayClicks = 0
        var refreshClicks = 0
        var selectedDate: LocalDate? = null
        composeRule.setContent {
            OutspireTheme {
                AcademicScreen(
                    state = state,
                    user = user,
                    onSignIn = {},
                    onRefresh = { refreshClicks++ },
                    onSelectDate = { selectedDate = it },
                    onToday = { todayClicks++ },
                    onSelectSemester = {},
                )
            }
        }

        composeRule.onNodeWithText("Today").performClick()
        composeRule.onNodeWithContentDescription("Refresh timetable").performClick()
        composeRule.onNodeWithText("Tue").performClick()

        assertEquals(1, todayClicks)
        assertEquals(1, refreshClicks)
        assertEquals(LocalDate.of(2026, 7, 7), selectedDate)
    }

    @Test
    fun calendarButtonOpensTheSemesterMenu() {
        var selectedSemester: String? = null
        composeRule.setContent {
            OutspireTheme {
                AcademicScreen(state, user, {}, {}, {}, {}, { selectedSemester = it })
            }
        }

        composeRule.onNodeWithContentDescription("Choose semester").performClick()
        composeRule.onNodeWithText("2025-2026 (T1)").assertIsDisplayed().performClick()

        assertEquals("t1", selectedSemester)
    }

    @Test
    fun classCardOpensItsDetails() {
        composeRule.setContent {
            OutspireTheme {
                AcademicScreen(state, user, {}, {}, {}, {}, {})
            }
        }

        composeRule.onNodeWithText("Mathematics AA HL").performClick()

        composeRule.onNodeWithText("40 minutes").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close details").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertDoesNotExist()
    }
}
