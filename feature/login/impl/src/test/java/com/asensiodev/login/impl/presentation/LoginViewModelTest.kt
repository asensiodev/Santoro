package com.asensiodev.login.impl.presentation

import android.content.Context
import com.asensiodev.auth.domain.usecase.SignInAnonymouslyUseCase
import com.asensiodev.auth.domain.usecase.SignInWithGoogleUseCase
import com.asensiodev.auth.helper.GoogleSignInHelper
import com.asensiodev.core.domain.model.SantoroUser
import com.asensiodev.ui.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.asensiodev.santoro.core.stringresources.R as SR

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase = mockk()
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase = mockk()
    private val googleSignInHelper: GoogleSignInHelper = mockk()
    private val context: Context = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel =
            LoginViewModel(
                signInWithGoogleUseCase = signInWithGoogleUseCase,
                signInAnonymouslyUseCase = signInAnonymouslyUseCase,
                googleSignInHelper = googleSignInHelper,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN idle state WHEN anonymous sign-in is processed THEN loading is set synchronously`() =
        runTest {
            val result = CompletableDeferred<Result<SantoroUser>>()
            coEvery { signInAnonymouslyUseCase() } coAnswers { result.await() }

            viewModel.process(LoginIntent.SignInAnonymously)

            viewModel.uiState.value.isLoading
                .shouldBeTrue()
        }

    @Test
    fun `GIVEN anonymous sign-in succeeds WHEN completed THEN success is set and loading is cleared`() =
        runTest {
            coEvery { signInAnonymouslyUseCase() } returns Result.success(user(isAnonymous = true))

            viewModel.process(LoginIntent.SignInAnonymously)
            advanceUntilIdle()

            viewModel.uiState.value.isSignInSuccessful
                .shouldBeTrue()
            viewModel.uiState.value.isLoading
                .shouldBeFalse()
            viewModel.uiState.value.errorMessage
                .shouldBeNull()
        }

    @Test
    fun `GIVEN anonymous sign-in fails WHEN completed THEN anonymous error is set and loading is cleared`() =
        runTest {
            coEvery { signInAnonymouslyUseCase() } returns Result.failure(Exception("failed"))

            viewModel.process(LoginIntent.SignInAnonymously)
            advanceUntilIdle()

            viewModel.uiState.value.isSignInSuccessful
                .shouldBeFalse()
            viewModel.uiState.value.isLoading
                .shouldBeFalse()
            viewModel.uiState.value.errorResource() shouldBeEqualTo SR.string.login_error_anonymous
        }

    @Test
    fun `GIVEN Google credential sign-in fails WHEN completed THEN Google error is set and backend is not called`() =
        runTest {
            coEvery { googleSignInHelper.signIn(context) } returns Result.failure(Exception("failed"))

            viewModel.process(LoginIntent.SignInWithGoogle(context))
            advanceUntilIdle()

            viewModel.uiState.value.isLoading
                .shouldBeFalse()
            viewModel.uiState.value.errorResource() shouldBeEqualTo SR.string.login_error_google_sign_in
            coVerify(exactly = 0) { signInWithGoogleUseCase(any()) }
        }

    @Test
    fun `GIVEN Google credential producer throws cancellation WHEN completed THEN loading clears without error`() =
        runTest {
            coEvery { googleSignInHelper.signIn(context) } throws CancellationException("cancelled")

            viewModel.process(LoginIntent.SignInWithGoogle(context))
            advanceUntilIdle()

            viewModel.uiState.value.isLoading
                .shouldBeFalse()
            viewModel.uiState.value.errorMessage
                .shouldBeNull()
            coVerify(exactly = 0) { signInWithGoogleUseCase(any()) }
        }

    @Test
    fun `GIVEN Google backend sign-in succeeds WHEN completed THEN success is set and loading is cleared`() =
        runTest {
            coEvery { googleSignInHelper.signIn(context) } returns Result.success("token")
            coEvery { signInWithGoogleUseCase("token") } returns Result.success(user(isAnonymous = false))

            viewModel.process(LoginIntent.SignInWithGoogle(context))
            advanceUntilIdle()

            viewModel.uiState.value.isSignInSuccessful
                .shouldBeTrue()
            viewModel.uiState.value.isLoading
                .shouldBeFalse()
            viewModel.uiState.value.errorMessage
                .shouldBeNull()
        }

    @Test
    fun `GIVEN Google backend sign-in fails WHEN completed THEN Google error is set and loading is cleared`() =
        runTest {
            coEvery { googleSignInHelper.signIn(context) } returns Result.success("token")
            coEvery { signInWithGoogleUseCase("token") } returns Result.failure(Exception("failed"))

            viewModel.process(LoginIntent.SignInWithGoogle(context))
            advanceUntilIdle()

            viewModel.uiState.value.isSignInSuccessful
                .shouldBeFalse()
            viewModel.uiState.value.isLoading
                .shouldBeFalse()
            viewModel.uiState.value.errorResource() shouldBeEqualTo SR.string.login_error_google_sign_in
        }

    @Test
    fun `GIVEN Google backend producer throws cancellation WHEN completed THEN loading clears without error`() =
        runTest {
            coEvery { googleSignInHelper.signIn(context) } returns Result.success("token")
            coEvery { signInWithGoogleUseCase("token") } throws CancellationException("cancelled")

            viewModel.process(LoginIntent.SignInWithGoogle(context))
            advanceUntilIdle()

            viewModel.uiState.value.isLoading
                .shouldBeFalse()
            viewModel.uiState.value.errorMessage
                .shouldBeNull()
        }

    @Test
    fun `GIVEN anonymous sign-in is pending WHEN another auth intent is processed THEN it is ignored`() =
        runTest {
            val result = CompletableDeferred<Result<SantoroUser>>()
            coEvery { signInAnonymouslyUseCase() } coAnswers { result.await() }

            viewModel.process(LoginIntent.SignInAnonymously)
            viewModel.process(LoginIntent.SignInAnonymously)
            viewModel.process(LoginIntent.SignInWithGoogle(context))
            runCurrent()

            coVerify(exactly = 1) { signInAnonymouslyUseCase() }
            coVerify(exactly = 0) { googleSignInHelper.signIn(any()) }

            result.complete(Result.success(user(isAnonymous = true)))
            advanceUntilIdle()
        }

    @Test
    fun `GIVEN previous error WHEN a new sign-in starts THEN error is cleared`() =
        runTest {
            coEvery { signInAnonymouslyUseCase() } returns Result.failure(Exception("failed"))
            viewModel.process(LoginIntent.SignInAnonymously)
            advanceUntilIdle()

            val result = CompletableDeferred<Result<SantoroUser>>()
            coEvery { signInAnonymouslyUseCase() } coAnswers { result.await() }
            viewModel.process(LoginIntent.SignInAnonymously)

            viewModel.uiState.value.errorMessage
                .shouldBeNull()
            result.complete(Result.success(user(isAnonymous = true)))
            advanceUntilIdle()
        }

    @Test
    fun `GIVEN anonymous sign-in producer throws cancellation WHEN completed THEN loading clears without error`() =
        runTest {
            coEvery { signInAnonymouslyUseCase() } throws CancellationException("cancelled")

            viewModel.process(LoginIntent.SignInAnonymously)
            advanceUntilIdle()

            viewModel.uiState.value.isLoading
                .shouldBeFalse()
            viewModel.uiState.value.errorMessage
                .shouldBeNull()
        }

    @Test
    fun `GIVEN sign-in succeeded WHEN another auth intent is processed THEN it is ignored`() =
        runTest {
            coEvery { signInAnonymouslyUseCase() } returns Result.success(user(isAnonymous = true))

            viewModel.process(LoginIntent.SignInAnonymously)
            advanceUntilIdle()
            viewModel.process(LoginIntent.SignInAnonymously)
            viewModel.process(LoginIntent.SignInWithGoogle(context))
            advanceUntilIdle()

            coVerify(exactly = 1) { signInAnonymouslyUseCase() }
            coVerify(exactly = 0) { googleSignInHelper.signIn(any()) }
        }

    private fun LoginUiState.errorResource(): Int = (errorMessage as UiText.StringResource).resId

    private fun user(isAnonymous: Boolean) =
        SantoroUser(
            uid = "uid",
            email = null,
            displayName = null,
            photoUrl = null,
            isAnonymous = isAnonymous,
        )
}
