package dev.outspire.android.feature.today

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.outspire.android.data.model.ScheduleSettings
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.OutspireTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodayScreenInteractionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val user = User(1, "s20248401", "Test Student", null)

    @Test
    fun weekendUsesTheWeekendMessage() {
        setTodayContent(TodayUiState(now = LocalDateTime.of(2026, 7, 11, 10, 0)))

        composeRule.onNodeWithText("It's the weekend!").assertIsDisplayed()
        composeRule.onNodeWithText("Relax and recharge.").assertIsDisplayed()
    }

    @Test
    fun settingsSheetChangesTheScheduleDay() {
        var selectedDay: DayOfWeek? = null
        setTodayContent(
            state = TodayUiState(now = LocalDateTime.of(2026, 7, 11, 10, 0)),
            onSelectDay = { selectedDay = it },
        )

        composeRule.onNodeWithContentDescription("Schedule settings").performClick()
        composeRule.onNodeWithText("Schedule Settings").assertIsDisplayed()
        composeRule.onNodeWithText("Monday").performClick()

        assertEquals(DayOfWeek.MONDAY, selectedDay)
    }

    @Test
    fun holidayModeShowsItsEndDate() {
        setTodayContent(
            TodayUiState(
                now = LocalDateTime.of(2026, 7, 11, 10, 0),
                settings = ScheduleSettings(
                    holidayEnabled = true,
                    holidayEndDateEnabled = true,
                    holidayEndDate = LocalDate.of(2026, 8, 31),
                ),
            ),
        )

        composeRule.onNodeWithText("Holiday Mode").assertIsDisplayed()
        composeRule.onNodeWithText("Until Aug 31, 2026").assertIsDisplayed()
    }

    @Test
    fun signedOutStateNeverShowsAnAccountsHoliday() {
        setTodayContent(
            state = TodayUiState(
                now = LocalDateTime.of(2026, 7, 11, 10, 0),
                settings = ScheduleSettings(
                    holidayEnabled = true,
                    holidayEndDateEnabled = true,
                    holidayEndDate = LocalDate.of(2026, 8, 31),
                ),
            ),
            user = null,
        )

        composeRule.onNodeWithText("Holiday until Aug 31, 2026").assertDoesNotExist()
        composeRule.onNodeWithText("Your school day, in one place").assertIsDisplayed()
    }

    private fun setTodayContent(
        state: TodayUiState,
        onSelectDay: (DayOfWeek?) -> Unit = {},
        user: User? = this.user,
    ) {
        composeRule.setContent {
            OutspireTheme {
                TodayScreen(
                    state = state,
                    user = user,
                    onSignIn = {},
                    onRefresh = {},
                    onSelectScheduleDay = onSelectDay,
                    onHolidayEnabled = {},
                    onHolidayEndDateEnabled = {},
                    onHolidayEndDate = {},
                    onShowFutureCountdown = {},
                    onClasses = {},
                    onActivities = {},
                    onGrades = {},
                )
            }
        }
    }
}
