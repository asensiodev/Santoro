package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.santoro.core.sync.SyncMockUtils
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Transaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FirestoreMovieDataSourceImplTest {
    private val firestore: FirebaseFirestore = mockk()
    private val usersCollection: CollectionReference = mockk()
    private val userDocument: DocumentReference = mockk()
    private val moviesCollection: CollectionReference = mockk()
    private val movieDocument: DocumentReference = mockk()
    private val transaction: Transaction = mockk()
    private val remoteDocument: DocumentSnapshot = mockk()

    private lateinit var sut: FirestoreMovieDataSourceImpl

    @BeforeEach
    fun setUp() {
        sut = FirestoreMovieDataSourceImpl(firestore)

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document(any()) } returns userDocument
        every { userDocument.collection("movies") } returns moviesCollection
        every { moviesCollection.document(any()) } returns movieDocument
        every { transaction.get(movieDocument) } returns remoteDocument
        every { transaction.set(movieDocument, any<Map<String, Any?>>()) } returns transaction
        every { firestore.runTransaction<Unit>(any()) } answers {
            val function = firstArg<Transaction.Function<Unit>>()
            Tasks.forResult(function.apply(transaction))
        }
    }

    @Test
    fun `GIVEN valid entity WHEN uploadMovie THEN returns success`() =
        runTest {
            val entity = SyncMockUtils.createSyncEntity(movieId = 1, isWatched = true)
            every { remoteDocument.getLong("updatedAt") } returns null

            val result = sut.uploadMovie(uid = "uid123", entity = entity)

            result.isSuccess.shouldBeTrue()
            verify(exactly = 1) { transaction.set(movieDocument, any<Map<String, Any?>>()) }
        }

    @Test
    fun `GIVEN remote movie is newer WHEN uploadMovie THEN does not overwrite it`() =
        runTest {
            val entity = SyncMockUtils.createSyncEntity(movieId = 1, updatedAt = 1000L)
            every { remoteDocument.getLong("updatedAt") } returns 2000L

            val result = sut.uploadMovie(uid = "uid123", entity = entity)

            result.isSuccess.shouldBeTrue()
            verify(exactly = 0) { transaction.set(any(), any<Map<String, Any?>>()) }
        }

    @Test
    fun `GIVEN local movie is newer WHEN uploadMovie THEN overwrites remote movie`() =
        runTest {
            val entity = SyncMockUtils.createSyncEntity(movieId = 1, updatedAt = 2000L)
            every { remoteDocument.getLong("updatedAt") } returns 1000L

            val result = sut.uploadMovie(uid = "uid123", entity = entity)

            result.isSuccess.shouldBeTrue()
            verify(exactly = 1) { transaction.set(movieDocument, any<Map<String, Any?>>()) }
        }

    @Test
    fun `GIVEN valid entities WHEN uploadMovies THEN uploads every movie`() =
        runTest {
            val entities =
                listOf(
                    SyncMockUtils.createSyncEntity(movieId = 1, isWatched = true),
                    SyncMockUtils.createSyncEntity(movieId = 2, isInWatchlist = true),
                )
            every { remoteDocument.getLong("updatedAt") } returns null

            val result = sut.uploadMovies(uid = "uid123", entities = entities)

            result.isSuccess.shouldBeTrue()
            verify(exactly = 2) { transaction.set(any<DocumentReference>(), any<Map<String, Any?>>()) }
        }

    @Test
    fun `GIVEN Firestore throws WHEN uploadMovie THEN returns failure`() =
        runTest {
            val entity = SyncMockUtils.createSyncEntity(movieId = 1)
            every {
                firestore.runTransaction<Unit>(any())
            } returns Tasks.forException(Exception("Firestore error"))

            val result = sut.uploadMovie(uid = "uid123", entity = entity)

            result.isFailure.shouldBeTrue()
        }

    @Test
    fun `GIVEN upload is cancelled WHEN uploadMovie THEN cancellation propagates`() =
        runTest {
            val entity = SyncMockUtils.createSyncEntity(movieId = 1)
            every {
                firestore.runTransaction<Unit>(any())
            } returns Tasks.forException(CancellationException())

            val exception =
                try {
                    sut.uploadMovie(uid = "uid123", entity = entity)
                    null
                } catch (exception: CancellationException) {
                    exception
                }

            (exception is CancellationException) shouldBeEqualTo true
        }

    @Test
    fun `GIVEN remote documents WHEN downloadUserMovies THEN returns mapped entities`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val doc: DocumentSnapshot = mockk()

            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns listOf(doc)
            every { doc.getLong("movieId") } returns 42L
            every { doc.getString("title") } returns "Movie Title"
            every { doc.getString("posterPath") } returns null
            every { doc.getString("genres") } returns ""
            every { doc.getLong("runtime") } returns null
            every { doc.getBoolean("isWatched") } returns true
            every { doc.getBoolean("isInWatchlist") } returns false
            every { doc.getLong("watchedAt") } returns 999L
            every { doc.getLong("updatedAt") } returns 2000L

            val result = sut.downloadUserMovies(uid = "uid123")

            result.isSuccess.shouldBeTrue()
            val entities = result.getOrThrow()
            entities.size shouldBeEqualTo 1
            entities[0].movieId shouldBeEqualTo 42
            entities[0].isWatched.shouldBeTrue()
            entities[0].updatedAt shouldBeEqualTo 2000L
        }

    @Test
    fun `GIVEN Firestore throws WHEN downloadUserMovies THEN returns failure`() =
        runTest {
            every {
                moviesCollection.get()
            } returns Tasks.forException(Exception("Network error"))

            val result = sut.downloadUserMovies(uid = "uid123")

            result.isFailure.shouldBeTrue()
        }

    @Test
    fun `GIVEN download is cancelled WHEN downloadUserMovies THEN cancellation propagates`() =
        runTest {
            every { moviesCollection.get() } returns Tasks.forException(CancellationException())

            val exception =
                try {
                    sut.downloadUserMovies(uid = "uid123")
                    null
                } catch (exception: CancellationException) {
                    exception
                }

            (exception is CancellationException) shouldBeEqualTo true
        }

    @Test
    fun `GIVEN document without movieId WHEN downloadUserMovies THEN skips invalid document`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val invalidDoc: DocumentSnapshot = mockk()

            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns listOf(invalidDoc)
            every { invalidDoc.getLong("movieId") } returns null

            val result = sut.downloadUserMovies(uid = "uid123")

            result.isSuccess.shouldBeTrue()
            result.getOrThrow().size shouldBeEqualTo 0
        }

    @Test
    fun `GIVEN document with null title WHEN downloadUserMovies THEN skips invalid document`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val invalidDoc: DocumentSnapshot = mockk()

            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns listOf(invalidDoc)
            every { invalidDoc.getLong("movieId") } returns 1L
            every { invalidDoc.getString("title") } returns null

            val result = sut.downloadUserMovies(uid = "uid123")

            result.isSuccess.shouldBeTrue()
            result.getOrThrow().size shouldBeEqualTo 0
        }

    @Test
    fun `GIVEN document with empty title WHEN downloadUserMovies THEN skips invalid document`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val invalidDoc: DocumentSnapshot = mockk()

            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns listOf(invalidDoc)
            every { invalidDoc.getLong("movieId") } returns 1L
            every { invalidDoc.getString("title") } returns ""

            val result = sut.downloadUserMovies(uid = "uid123")

            result.isSuccess.shouldBeTrue()
            result.getOrThrow().size shouldBeEqualTo 0
        }

    @Test
    fun `GIVEN empty collection WHEN downloadUserMovies THEN returns empty list`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()

            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns emptyList()

            val result = sut.downloadUserMovies(uid = "uid123")

            result.isSuccess.shouldBeTrue()
            result.getOrThrow().size shouldBeEqualTo 0
        }
}
