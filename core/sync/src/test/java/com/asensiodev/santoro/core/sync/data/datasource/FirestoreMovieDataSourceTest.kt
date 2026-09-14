package com.asensiodev.santoro.core.sync.data.datasource

import com.asensiodev.santoro.core.sync.SyncMockUtils
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.WriteBatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FirestoreMovieDataSourceTest {
    private val firestore: FirebaseFirestore = mockk()
    private val usersCollection: CollectionReference = mockk()
    private val userDocument: DocumentReference = mockk()
    private val moviesCollection: CollectionReference = mockk()
    private val movieDocument: DocumentReference = mockk()
    private val transaction: Transaction = mockk()
    private val remoteDocument: DocumentSnapshot = mockk()

    private lateinit var sut: FirestoreMovieDataSource

    @BeforeEach
    fun setUp() {
        sut = FirestoreMovieDataSource(firestore)

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
    fun `GIVEN out of range and noncanonical movie IDs WHEN downloading THEN skips only out of range IDs`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val zero = movieDocument(movieId = 0L)
            val overflow = movieDocument(movieId = Int.MAX_VALUE.toLong() + 1L)
            val noncanonical = movieDocument(movieId = 42L)
            val valid = movieDocument(movieId = 7L)
            every { noncanonical.id } returns "not-42"
            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns listOf(zero, overflow, noncanonical, valid)

            val result = sut.downloadUserMovies("uid123")

            result.getOrThrow().map { it.movieId } shouldBeEqualTo listOf(42, 7)
        }

    @Test
    fun `GIVEN invalid required fields and valid sibling WHEN downloading THEN skips malformed documents`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val missingMovieId = movieDocument(movieId = 1L)
            val missingTitle = movieDocument(movieId = 2L)
            val blankTitle = movieDocument(movieId = 3L)
            val wrongMovieIdType = movieDocument(movieId = 4L)
            val wrongTitleType = movieDocument(movieId = 5L)
            val valid = movieDocument(movieId = 6L)
            every { missingMovieId.getLong("movieId") } returns null
            every { missingTitle.getString("title") } returns null
            every { blankTitle.getString("title") } returns " "
            every { wrongMovieIdType.getLong("movieId") } throws IllegalArgumentException("wrong type")
            every { wrongTitleType.getString("title") } throws IllegalArgumentException("wrong type")
            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns
                listOf(
                    missingMovieId,
                    missingTitle,
                    blankTitle,
                    wrongMovieIdType,
                    wrongTitleType,
                    valid,
                )

            val result = sut.downloadUserMovies("uid123")

            result.getOrThrow().map { it.movieId } shouldBeEqualTo listOf(6)
        }

    @Test
    fun `GIVEN wrong typed optional field and valid sibling WHEN downloading THEN skips malformed document`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val valid = movieDocument(movieId = 3L)
            val malformed = movieDocument(movieId = 4L)
            every { malformed.getBoolean("isWatched") } throws
                IllegalArgumentException("wrong type")
            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns listOf(valid, malformed)

            val result = sut.downloadUserMovies("uid123")

            result.getOrThrow().map { it.movieId } shouldBeEqualTo listOf(3)
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
    fun `GIVEN document decoding is cancelled WHEN downloadUserMovies THEN cancellation propagates`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val document = movieDocument(movieId = 1L)
            every { document.getString("posterPath") } throws CancellationException()
            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns listOf(document)

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
    fun `GIVEN empty collection WHEN downloadUserMovies THEN returns empty list`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()

            every { moviesCollection.get() } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns emptyList()

            val result = sut.downloadUserMovies(uid = "uid123")

            result.isSuccess.shouldBeTrue()
            result.getOrThrow().size shouldBeEqualTo 0
        }

    @Test
    fun `GIVEN no movie documents WHEN deleteUserData THEN deletes only parent document`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            every { moviesCollection.get(Source.SERVER) } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns emptyList()
            every { userDocument.delete() } returns Tasks.forResult(null)

            sut.deleteUserData("uid123") shouldBeEqualTo Result.success(Unit)

            verify(exactly = 0) { firestore.batch() }
            verify(exactly = 1) { userDocument.delete() }
        }

    @Test
    fun `GIVEN one movie document WHEN deleteUserData THEN commits deletion before parent`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val documentSnapshot: DocumentSnapshot = mockk()
            val documentReference: DocumentReference = mockk()
            val batch: WriteBatch = mockk()
            every { moviesCollection.get(Source.SERVER) } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns listOf(documentSnapshot)
            every { documentSnapshot.reference } returns documentReference
            every { firestore.batch() } returns batch
            every { batch.delete(documentReference) } returns batch
            every { batch.commit() } returns Tasks.forResult(null)
            every { userDocument.delete() } returns Tasks.forResult(null)

            sut.deleteUserData("uid123") shouldBeEqualTo Result.success(Unit)

            verify(exactly = 1) { batch.delete(documentReference) }
            verify(exactly = 1) { batch.commit() }
            verify(exactly = 1) { userDocument.delete() }
            verifyOrder {
                batch.commit()
                userDocument.delete()
            }
        }

    @Test
    fun `GIVEN 501 movie documents WHEN deleteUserData THEN commits two bounded batches`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val documentReferences = List(501) { mockk<DocumentReference>() }
            val documentSnapshots =
                documentReferences.map { reference ->
                    mockk<DocumentSnapshot> {
                        every { this@mockk.reference } returns reference
                    }
                }
            val firstBatch: WriteBatch = mockk()
            val secondBatch: WriteBatch = mockk()
            every { moviesCollection.get(Source.SERVER) } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns documentSnapshots
            every { firestore.batch() } returnsMany listOf(firstBatch, secondBatch)
            every { firstBatch.delete(any()) } returns firstBatch
            every { secondBatch.delete(any()) } returns secondBatch
            every { firstBatch.commit() } returns Tasks.forResult(null)
            every { secondBatch.commit() } returns Tasks.forResult(null)
            every { userDocument.delete() } returns Tasks.forResult(null)

            sut.deleteUserData("uid123") shouldBeEqualTo Result.success(Unit)

            verify(exactly = 500) { firstBatch.delete(any()) }
            verify(exactly = 1) { secondBatch.delete(any()) }
            verify(exactly = 1) { firstBatch.commit() }
            verify(exactly = 1) { secondBatch.commit() }
            verify(exactly = 1) { userDocument.delete() }
            verifyOrder {
                firstBatch.commit()
                secondBatch.commit()
                userDocument.delete()
            }
        }

    @Test
    fun `GIVEN a failed movie batch WHEN deleteUserData THEN stops before later batches and parent`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val documentSnapshots =
                List(1001) {
                    mockk<DocumentSnapshot> {
                        every { reference } returns mockk()
                    }
                }
            val firstBatch: WriteBatch = mockk()
            val failedBatch: WriteBatch = mockk()
            val uncommittedBatch: WriteBatch = mockk()
            val failure = IllegalStateException("batch failed")
            every { moviesCollection.get(Source.SERVER) } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns documentSnapshots
            every { firestore.batch() } returnsMany listOf(firstBatch, failedBatch, uncommittedBatch)
            every { firstBatch.delete(any()) } returns firstBatch
            every { failedBatch.delete(any()) } returns failedBatch
            every { firstBatch.commit() } returns Tasks.forResult(null)
            every { failedBatch.commit() } returns Tasks.forException(failure)

            sut.deleteUserData("uid123").exceptionOrNull() shouldBeEqualTo failure

            verify(exactly = 1) { firstBatch.commit() }
            verify(exactly = 1) { failedBatch.commit() }
            verify(exactly = 0) { uncommittedBatch.commit() }
            verify(exactly = 0) { userDocument.delete() }
        }

    @Test
    fun `GIVEN server movie query failure WHEN deleteUserData THEN returns failure without deleting parent`() =
        runTest {
            val failure = IllegalStateException("query failed")
            every { moviesCollection.get(Source.SERVER) } returns Tasks.forException(failure)

            sut.deleteUserData("uid123").exceptionOrNull() shouldBeEqualTo failure

            verify(exactly = 0) { firestore.batch() }
            verify(exactly = 0) { userDocument.delete() }
        }

    @Test
    fun `GIVEN parent deletion failure WHEN deleteUserData THEN returns failure`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val failure = IllegalStateException("parent deletion failed")
            every { moviesCollection.get(Source.SERVER) } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns emptyList()
            every { userDocument.delete() } returns Tasks.forException(failure)

            sut.deleteUserData("uid123").exceptionOrNull() shouldBeEqualTo failure
        }

    @Test
    fun `GIVEN batch cancellation WHEN deleteUserData THEN cancellation propagates without deleting parent`() =
        runTest {
            val querySnapshot: QuerySnapshot = mockk()
            val documentSnapshot: DocumentSnapshot = mockk()
            val batch: WriteBatch = mockk()
            val cancellation = CancellationException("cancelled")
            every { moviesCollection.get(Source.SERVER) } returns Tasks.forResult(querySnapshot)
            every { querySnapshot.documents } returns listOf(documentSnapshot)
            every { documentSnapshot.reference } returns movieDocument
            every { firestore.batch() } returns batch
            every { batch.delete(movieDocument) } returns batch
            every { batch.commit() } returns Tasks.forException(cancellation)

            val thrown =
                try {
                    sut.deleteUserData("uid123")
                    null
                } catch (exception: CancellationException) {
                    exception
                }

            thrown shouldBeEqualTo cancellation
            verify(exactly = 0) { userDocument.delete() }
        }

    private fun movieDocument(movieId: Long): DocumentSnapshot =
        mockk {
            every { getLong("movieId") } returns movieId
            every { getString("title") } returns "Movie"
            every { getString("posterPath") } returns null
            every { getString("genres") } returns ""
            every { getLong("runtime") } returns null
            every { getBoolean("isWatched") } returns false
            every { getBoolean("isInWatchlist") } returns false
            every { getLong("watchedAt") } returns null
            every { getLong("updatedAt") } returns 1L
        }
}
