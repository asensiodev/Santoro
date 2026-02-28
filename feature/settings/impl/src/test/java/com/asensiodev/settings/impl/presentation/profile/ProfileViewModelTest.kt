package com.asensiodev.settings.impl.presentation.profile

import com.asensiodev.auth.domain.exception.AccountCollisionException
import com.asensiodev.auth.domain.usecase.LinkWithGoogleUseCase
import com.asensiodev.auth.domain.usecase.ObserveAuthStateUseCase
import com.asensiodev.auth.domain.usecase.SignInWithGoogleUseCase
import com.asensiodev.auth.helper.GoogleSignInHelper
import com.asensiodev.core.domain.model.SantoroUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val observeAuthStateUseCase: ObserveAuthStateUseCase = mockk()
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = mockk(relaxed = true)
    private val linkWithGoogleUseCase: LinkWithGoogleUseCase = mockk(relaxed = true)
    private val googleSignInHelper: GoogleSignInHelper = mockk(relaxed = true)

    private lateinit var sut: ProfileViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val anonymousUser =
        SantoroUser(
            uid = "uid-anon",
            email = null,
            displayName = null,
            photoUrl = null,
            isAnonymous = true,
        )

    private val googleUser =
        SantoroUser(
            uid = "uid-google",
            email = "user@example.com",
            displayName = "User",
            photoUrl = null,
            isAnonymous = false,
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeAuthStateUseCase() } returns flowOf(anonymousUser)
        sut =
            ProfileViewModel(
                observeAuthStateUseCase = observeAuthStateUseCase,
                signInWithGoogleUseCase = signInWithGoogleUseCase,
                linkWithGoogleUseCase = linkWithGoogleUseCase,
                googleSignInHelper = googleSignInHelper,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class ObserveAuth {
        @Test
        fun `GIVEN anonymous user WHEN ObserveAuth intent THEN isAnonymous is true`() =
            runTest {
                every { observeAuthStateUseCase() } returns flowOf(anonymousUser)

                sut.process(ProfileIntent.ObserveAuth)
                advanceUntilIdle()

                sut.uiState.value.isAnonymous
                    .shouldBeTrue()
                sut.uiState.value.user shouldBeEqualTo anonymousUser
            }

        @Test
        fun `GIVEN google user WHEN ObserveAuth intent THEN isAnonymous is false`() =
            runTest {
                every { observeAuthStateUseCase() } returns flowOf(googleUser)

                sut.process(ProfileIntent.ObserveAuth)
                advanceUntilIdle()

                sut.uiState.value.isAnonymous
                    .shouldBeFalse()
                sut.uiState.value.user shouldBeEqualTo googleUser
            }

        @Test
        fun `GIVEN null user WHEN ObserveAuth intent THEN user is null and isAnonymous is false`() =
            runTest {
                every { observeAuthStateUseCase() } returns flowOf(null)

                sut.process(ProfileIntent.ObserveAuth)
                advanceUntilIdle()

                sut.uiState.value.user
                    .shouldBeNull()
                sut.uiState.value.isAnonymous
                    .shouldBeFalse()
            }
    }

    @Nested
    inner class LinkWithGoogle {
        @Test
        fun `GIVEN anonymous user and successful link WHEN OnLinkGoogleClicked THEN isLinkAccountSuccessful is true`() =
            runTest {
                every { observeAuthStateUseCase() } returns flowOf(anonymousUser)
                coEvery { googleSignInHelper.signIn(any()) } returns Result.success("id-token")
                coEvery { linkWithGoogleUseCase(any()) } returns Result.success(googleUser)

                sut.process(ProfileIntent.ObserveAuth)
                advanceUntilIdle()

                sut.process(ProfileIntent.OnLinkGoogleClicked(mockk(relaxed = true)))
                advanceUntilIdle()

                sut.uiState.value.isLinkAccountSuccessful
                    .shouldBeTrue()
                sut.uiState.value.isLoading
                    .shouldBeFalse()
                sut.uiState.value.error
                    .shouldBeNull()
            }

        @Test
        fun `GIVEN anonymous user and collision WHEN OnLinkGoogleClicked THEN showAccountCollisionDialog is true`() =
            runTest {
                every { observeAuthStateUseCase() } returns flowOf(anonymousUser)
                coEvery { googleSignInHelper.signIn(any()) } returns Result.success("id-token")
                coEvery { linkWithGoogleUseCase(any()) } returns Result.failure(AccountCollisionException())

                sut.process(ProfileIntent.ObserveAuth)
                advanceUntilIdle()

                sut.process(ProfileIntent.OnLinkGoogleClicked(mockk(relaxed = true)))
                advanceUntilIdle()

                sut.uiState.value.showAccountCollisionDialog
                    .shouldBeTrue()
                sut.uiState.value.isLoading
                    .shouldBeFalse()
            }

        @Test
        fun `GIVEN sign-in failure WHEN OnLinkGoogleClicked intent THEN error is set and loading is false`() =
            runTest {
                coEvery { googleSignInHelper.signIn(any()) } returns Result.failure(Exception("Sign-in failed"))

                sut.process(ProfileIntent.OnLinkGoogleClicked(mockk(relaxed = true)))
                advanceUntilIdle()

                sut.uiState.value.isLoading
                    .shouldBeFalse()
                (sut.uiState.value.error != null).shouldBeTrue()
            }
    }

    @Nested
    inner class DismissDialogs {
        @Test
        fun `GIVEN isLinkAccountSuccessful true WHEN DismissLinkSuccess THEN isLinkAccountSuccessful is false`() =
            runTest {
                every { observeAuthStateUseCase() } returns flowOf(anonymousUser)
                coEvery { googleSignInHelper.signIn(any()) } returns Result.success("id-token")
                coEvery { linkWithGoogleUseCase(any()) } returns Result.success(googleUser)

                sut.process(ProfileIntent.ObserveAuth)
                advanceUntilIdle()
                sut.process(ProfileIntent.OnLinkGoogleClicked(mockk(relaxed = true)))
                advanceUntilIdle()

                sut.process(ProfileIntent.DismissLinkSuccess)

                sut.uiState.value.isLinkAccountSuccessful
                    .shouldBeFalse()
            }

        @Test
        fun `GIVEN showAccountCollisionDialog true WHEN DismissAccountCollision THEN dialog is false`() =
            runTest {
                every { observeAuthStateUseCase() } returns flowOf(anonymousUser)
                coEvery { googleSignInHelper.signIn(any()) } returns Result.success("id-token")
                coEvery { linkWithGoogleUseCase(any()) } returns Result.failure(AccountCollisionException())

                sut.process(ProfileIntent.ObserveAuth)
                advanceUntilIdle()
                sut.process(ProfileIntent.OnLinkGoogleClicked(mockk(relaxed = true)))
                advanceUntilIdle()

                sut.process(ProfileIntent.DismissAccountCollision)

                sut.uiState.value.showAccountCollisionDialog
                    .shouldBeFalse()
            }
    }

    @Nested
    inner class ConfirmAccountCollision {
        @Test
        fun `GIVEN pending token WHEN ConfirmAccountCollision intent succeeds THEN isLoading is false and no error`() =
            runTest {
                every { observeAuthStateUseCase() } returns flowOf(anonymousUser)
                coEvery { googleSignInHelper.signIn(any()) } returns Result.success("id-token")
                coEvery { linkWithGoogleUseCase(any()) } returns Result.failure(AccountCollisionException())
                coEvery { signInWithGoogleUseCase(any()) } returns Result.success(googleUser)

                sut.process(ProfileIntent.ObserveAuth)
                advanceUntilIdle()
                sut.process(ProfileIntent.OnLinkGoogleClicked(mockk(relaxed = true)))
                advanceUntilIdle()

                sut.process(ProfileIntent.ConfirmAccountCollision)
                advanceUntilIdle()

                sut.uiState.value.isLoading
                    .shouldBeFalse()
                sut.uiState.value.error
                    .shouldBeNull()
            }

        @Test
        fun `GIVEN no pending token WHEN ConfirmAccountCollision intent THEN no action taken`() =
            runTest {
                sut.process(ProfileIntent.ConfirmAccountCollision)
                advanceUntilIdle()

                sut.uiState.value shouldBeEqualTo ProfileUiState()
            }
    }
}
