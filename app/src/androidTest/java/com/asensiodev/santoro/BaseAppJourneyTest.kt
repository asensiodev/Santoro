package com.asensiodev.santoro

import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.asensiodev.santoro.fake.FakeAuthRepository
import com.asensiodev.santoro.fake.FakeDatabaseRepository
import com.asensiodev.santoro.fake.FakeMovieDetailRepository
import com.asensiodev.santoro.fake.FakeRecentSearchesRepository
import com.asensiodev.santoro.fake.FakeSyncRepository
import com.asensiodev.santoro.fake.FakeUserPreferencesRepository
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

abstract class BaseAppJourneyTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: FakeAuthRepository

    @Inject
    lateinit var preferencesRepository: FakeUserPreferencesRepository

    @Inject
    lateinit var databaseRepository: FakeDatabaseRepository

    @Inject
    lateinit var detailRepository: FakeMovieDetailRepository

    @Inject
    lateinit var recentSearchesRepository: FakeRecentSearchesRepository

    @Inject
    lateinit var syncRepository: FakeSyncRepository

    private val scenarios = mutableListOf<ActivityScenario<MainActivity>>()

    @Before
    fun injectAndReset() {
        hiltRule.inject()
        authRepository.reset()
        preferencesRepository.reset()
        databaseRepository.reset()
        detailRepository.reset()
        recentSearchesRepository.reset()
        syncRepository.reset()
    }

    @After
    fun closeScenarios() {
        scenarios.forEach(ActivityScenario<MainActivity>::close)
        scenarios.clear()
    }

    protected fun launch(intent: Intent = mainActivityIntent()): ActivityScenario<MainActivity> =
        ActivityScenario.launch<MainActivity>(intent).also(scenarios::add)

    protected fun mainActivityIntent(): Intent =
        Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    protected fun string(resourceId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)

    protected fun formattedString(
        resourceId: Int,
        vararg arguments: Any?,
    ): String =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(resourceId, *arguments)
}
