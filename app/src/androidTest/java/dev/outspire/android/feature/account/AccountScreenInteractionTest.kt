package dev.outspire.android.feature.account

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.outspire.android.designsystem.OutspireTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountScreenInteractionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rememberCredentialsControlIsInteractive() {
        var rememberCredentials = false
        composeRule.setContent {
            OutspireTheme {
                AccountScreen(
                    state = AccountUiState(),
                    user = null,
                    onCodeChange = {},
                    onPasswordChange = {},
                    onRememberCredentialsChange = { rememberCredentials = it },
                    onLogin = {},
                    onLogout = {},
                )
            }
        }

        composeRule.onNodeWithText("Remember credentials").performClick()

        assertTrue(rememberCredentials)
        composeRule.onNodeWithText("Continue with demo data").assertDoesNotExist()
    }

    @Test
    fun loadingButtonUsesTheSpringIndicator() {
        composeRule.setContent {
            OutspireTheme {
                AccountScreen(
                    state = AccountUiState(isLoading = true),
                    user = null,
                    onCodeChange = {},
                    onPasswordChange = {},
                    onRememberCredentialsChange = {},
                    onLogin = {},
                    onLogout = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Loading").assertIsDisplayed()
    }
}
