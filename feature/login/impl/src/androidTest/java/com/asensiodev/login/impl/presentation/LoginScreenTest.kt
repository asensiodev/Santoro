package com.asensiodev.login.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.core.designsystem.theme.SantoroTheme
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.asensiodev.santoro.core.stringresources.R as SR

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun guestSignInRequiresConfirmationAndKeepsNoticeOffLanding() {
        var signInCalls = 0
        composeRule.setContent {
            SantoroTheme {
                LoginScreen(
                    uiState = LoginUiState(),
                    onAnonymousLoginClicked = { signInCalls += 1 },
                    onGoogleLoginClicked = {},
                )
            }
        }
        val guest = composeRule.activity.getString(SR.string.login_anonymous_login_button)
        val notice = composeRule.activity.getString(SR.string.login_guest_data_notice)

        composeRule.onNodeWithText(notice).assertDoesNotExist()
        composeRule.onNodeWithText(guest).performClick()
        composeRule.onNodeWithText(notice).assertIsDisplayed()
        composeRule.runOnIdle { signInCalls shouldBeEqualTo 0 }

        composeRule.onNode(hasText(guest) and hasAnyAncestor(isDialog())).performClick()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.runOnIdle { signInCalls shouldBeEqualTo 1 }
    }

    @Test
    fun cancellingGuestConfirmationDoesNotSignIn() {
        var signInCalls = 0
        composeRule.setContent {
            SantoroTheme {
                LoginScreen(
                    uiState = LoginUiState(),
                    onAnonymousLoginClicked = { signInCalls += 1 },
                    onGoogleLoginClicked = {},
                )
            }
        }
        val guest = composeRule.activity.getString(SR.string.login_anonymous_login_button)
        val cancel = composeRule.activity.getString(SR.string.login_guest_confirmation_cancel)

        composeRule.onNodeWithText(guest).performClick()
        composeRule.onNodeWithText(cancel).performClick()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNodeWithText(guest).assertIsDisplayed()
        composeRule.runOnIdle { signInCalls shouldBeEqualTo 0 }
    }
}
