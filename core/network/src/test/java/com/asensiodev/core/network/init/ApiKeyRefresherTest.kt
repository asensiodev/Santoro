package com.asensiodev.core.network.init

import com.asensiodev.core.network.data.repository.ApiKeyRepository
import com.asensiodev.core.testing.coVerifyNever
import com.asensiodev.core.testing.coVerifyOnce
import com.asensiodev.library.remoteconfig.api.RemoteConfigName
import com.asensiodev.library.remoteconfig.api.RemoteConfigProvider
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApiKeyRefresherTest {
    private var repository: ApiKeyRepository = mockk()
    private var remoteConfig: RemoteConfigProvider = mockk()

    private lateinit var refresher: ApiKeyRefresher

    @BeforeEach
    fun setup() {
        refresher = ApiKeyRefresher(repository, remoteConfig)
    }

    @Test
    fun `GIVEN remote key is blank WHEN ensureKeyUpToDate is called THEN do nothing`() =
        runTest {
            coEvery { repository.getSyncOrNull() } returns null
            coEvery {
                remoteConfig.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            } returns ""

            refresher.ensureKeyUpToDate()

            coVerifyNever { repository.refreshFromRemote(any()) }
        }

    @Test
    fun `GIVEN local key is null AND remote key is valid WHEN ensureKeyUpToDate THEN update repository`() =
        runTest {
            coEvery { repository.getSyncOrNull() } returns null
            coEvery {
                remoteConfig.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            } returns "remote-key"
            coEvery { repository.refreshFromRemote(any()) } just runs

            refresher.ensureKeyUpToDate()

            coVerifyOnce { repository.refreshFromRemote(any()) }
        }

    @Test
    fun `GIVEN local key is same as remote WHEN ensureKeyUpToDate THEN do nothing`() =
        runTest {
            coEvery { repository.getSyncOrNull() } returns "same-key"
            coEvery {
                remoteConfig.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            } returns "same-key"

            refresher.ensureKeyUpToDate()

            coVerifyNever { repository.refreshFromRemote(any()) }
        }

    @Test
    fun `GIVEN local key differs from remote WHEN ensureKeyUpToDate THEN update repository`() =
        runTest {
            coEvery { repository.getSyncOrNull() } returns "old-key"
            coEvery {
                remoteConfig.getStringParameter(RemoteConfigName.TMDB_SANTORO_API_KEY)
            } returns "new-key"
            coEvery { repository.refreshFromRemote(any()) } just runs

            refresher.ensureKeyUpToDate()

            coVerifyOnce { repository.refreshFromRemote(any()) }
        }
}
