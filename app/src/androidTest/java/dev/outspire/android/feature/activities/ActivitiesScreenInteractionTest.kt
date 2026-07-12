package dev.outspire.android.feature.activities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.outspire.android.data.model.CasActivity
import dev.outspire.android.data.model.User
import dev.outspire.android.designsystem.OutspireTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivitiesScreenInteractionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val activity = CasActivity(
        id = "1",
        title = "Campus technology support",
        club = "Computerization",
        date = LocalDate.of(2026, 7, 10),
        serviceHours = 2.0,
        reflection = "I learned how to coordinate the support team.",
    )
    private val user = User(1, "s20248401", "Test Student", null)

    @Test
    fun refreshAndActivityDetailsRespondToClicks() {
        var refreshClicks = 0
        composeRule.setContent {
            OutspireTheme {
                ActivitiesScreen(
                    state = ActivitiesUiState(activities = listOf(activity)),
                    user = user,
                    onSignIn = {},
                    onRefresh = { refreshClicks++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Refresh activities").performClick()
        composeRule.onNodeWithText("Campus technology support").performClick()

        assertEquals(1, refreshClicks)
        composeRule.onNodeWithText("Reflection").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Close details").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertDoesNotExist()
    }
}
