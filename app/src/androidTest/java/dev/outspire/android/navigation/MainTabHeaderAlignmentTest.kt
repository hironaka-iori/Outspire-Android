package dev.outspire.android.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.outspire.android.designsystem.OutspireTheme
import dev.outspire.android.feature.academic.AcademicScreen
import dev.outspire.android.feature.academic.AcademicUiState
import dev.outspire.android.feature.activities.ActivitiesScreen
import dev.outspire.android.feature.activities.ActivitiesUiState
import dev.outspire.android.feature.explore.ExploreScreen
import dev.outspire.android.feature.today.TodayScreen
import dev.outspire.android.feature.today.TodayUiState
import java.time.LocalDateTime
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainTabHeaderAlignmentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun allMainTabTitlesUseTheSameTopOffset() {
        var tab by mutableIntStateOf(0)
        composeRule.setContent {
            OutspireTheme {
                when (tab) {
                    0 -> TodayScreen(
                        state = TodayUiState(now = LocalDateTime.of(2026, 7, 6, 9, 0)),
                        user = null,
                        onSignIn = {},
                        onRefresh = {},
                        onSelectScheduleDay = {},
                        onHolidayEnabled = {},
                        onHolidayEndDateEnabled = {},
                        onHolidayEndDate = {},
                        onShowFutureCountdown = {},
                        onClasses = {},
                        onActivities = {},
                        onGrades = {},
                    )
                    1 -> AcademicScreen(AcademicUiState(), null, {}, {}, {}, {}, {})
                    2 -> ActivitiesScreen(ActivitiesUiState(), null, {}, {})
                    else -> ExploreScreen(null, {}, {}, {}, {}, {}, {})
                }
            }
        }

        val todayTop = titleTop("Good Morning")
        composeRule.runOnIdle { tab = 1 }
        val classTop = titleTop("Class")
        composeRule.runOnIdle { tab = 2 }
        val activitiesTop = titleTop("Activities")
        composeRule.runOnIdle { tab = 3 }
        val exploreTop = titleTop("Explore")

        val tops = listOf(todayTop, classTop, activitiesTop, exploreTop)
        assertTrue(
            "title tops: today=$todayTop class=$classTop activities=$activitiesTop explore=$exploreTop",
            tops.max() - tops.min() <= 1f,
        )
    }

    private fun titleTop(text: String): Float = composeRule
        .onNodeWithText(text)
        .fetchSemanticsNode()
        .boundsInRoot
        .top
}
