package com.asensiodev.santoro.core.database.data.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.santoro.core.database.data.SantoroRoomDatabase
import com.asensiodev.santoro.core.database.data.model.BrowseCacheEntity
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrowseCacheDaoTest {
    private lateinit var database: SantoroRoomDatabase
    private lateinit var sut: BrowseCacheDao

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    SantoroRoomDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        sut = database.browseCacheDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenPagesAcrossSections_whenQueried_thenCompositeKeysSelectExactPages() {
        runBlocking {
            val popularPageOne = entry("popular", 1, "popular-one", 100L)
            val popularPageTwo = entry("popular", 2, "popular-two", 200L)
            val trendingPageOne = entry("trending", 1, "trending-one", 300L)
            listOf(popularPageOne, popularPageTwo, trendingPageOne).forEach { page ->
                sut.upsertPage(page)
            }

            sut.getPage("popular", 1) shouldBeEqualTo popularPageOne
            sut.getPage("popular", 2) shouldBeEqualTo popularPageTwo
            sut.getPage("trending", 1) shouldBeEqualTo trendingPageOne
            sut.getPage("trending", 2).shouldBeNull()
        }
    }

    @Test
    fun givenExistingCompositeKey_whenUpserted_thenRowIsReplaced() {
        runBlocking {
            sut.upsertPage(entry("popular", 1, "old", 100L))
            val replacement = entry("popular", 1, "new", 200L)

            sut.upsertPage(replacement)

            sut.getPage("popular", 1) shouldBeEqualTo replacement
        }
    }

    @Test
    fun givenMultipleSections_whenOneSectionCleared_thenOtherSectionRemains() {
        runBlocking {
            sut.upsertPage(entry("popular", 1, "one", 100L))
            sut.upsertPage(entry("popular", 2, "two", 200L))
            val trending = entry("trending", 1, "three", 300L)
            sut.upsertPage(trending)

            sut.clearSection("popular")

            sut.getPage("popular", 1).shouldBeNull()
            sut.getPage("popular", 2).shouldBeNull()
            sut.getPage("trending", 1) shouldBeEqualTo trending
        }
    }

    @Test
    fun givenEntriesAroundCutoff_whenOldEntriesCleared_thenStrictCutoffIsPreserved() {
        runBlocking {
            sut.upsertPage(entry("popular", 1, "older", 99L))
            val atCutoff = entry("popular", 2, "cutoff", 100L)
            val newer = entry("popular", 3, "newer", 101L)
            sut.upsertPage(atCutoff)
            sut.upsertPage(newer)

            sut.clearEntriesOlderThan(100L)

            sut.getPage("popular", 1).shouldBeNull()
            sut.getPage("popular", 2) shouldBeEqualTo atCutoff
            sut.getPage("popular", 3) shouldBeEqualTo newer
        }
    }

    @Test
    fun givenCachedEntries_whenClearAll_thenEveryPageIsRemoved() {
        runBlocking {
            sut.upsertPage(entry("popular", 1, "one", 100L))
            sut.upsertPage(entry("trending", 2, "two", 200L))

            sut.clearAll()

            sut.getPage("popular", 1).shouldBeNull()
            sut.getPage("trending", 2).shouldBeNull()
        }
    }

    private fun entry(
        section: String,
        page: Int,
        moviesJson: String,
        cachedAt: Long,
    ) = BrowseCacheEntity(section, page, moviesJson, cachedAt)
}
