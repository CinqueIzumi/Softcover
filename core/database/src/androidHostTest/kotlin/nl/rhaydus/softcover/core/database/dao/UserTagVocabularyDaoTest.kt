package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import app.cash.turbine.test
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.UserTagVocabularyEntity

class UserTagVocabularyDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: UserTagVocabularyDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.userTagVocabularyDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class UpsertAllAndObserve {
        @Test
        fun `upsertAll inserts new rows and observe emits them for that user`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val entities = listOf(
                UserTagVocabularyEntity(
                    userId = 1,
                    category = "genre",
                    name = "sci-fi",
                    usageCount = 2,
                ),
                UserTagVocabularyEntity(
                    userId = 1,
                    category = "mood",
                    name = "cozy",
                    usageCount = 1,
                ),
            )

            // ----- Act -----
            dao.upsertAll(entities)

            // ----- Assert -----
            dao.observe(userId = 1).test {
                awaitItem() shouldContainExactlyInAnyOrder entities
            }
        }

        // NOTE: the @Upsert conflict-replace path (an in-place update when a row with the same
        // composite PK already exists) is intentionally NOT covered here. Room's upsert fallback relies
        // on the driver's uniqueness-conflict error message to decide to UPDATE, and the
        // sqlite-bundled-jvm host-test binary surfaces that exception with a null message, so the
        // fallback cannot run under unit tests (confirmed still failing on Room 2.8.4 / androidx.sqlite
        // 2.7.0, not just the pinned 2.7.2 / 2.6.2). Left out until it can be exercised where it
        // actually runs (an on-device / instrumented test against the native bundled driver).

        @Test
        fun `observe only emits rows for the given user, not other users' rows`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val userOneEntity = UserTagVocabularyEntity(
                userId = 1,
                category = "genre",
                name = "sci-fi",
            )
            val userTwoEntity = UserTagVocabularyEntity(
                userId = 2,
                category = "genre",
                name = "fantasy",
            )

            // ----- Act -----
            dao.upsertAll(listOf(userOneEntity, userTwoEntity))

            // ----- Assert -----
            dao.observe(userId = 1).test {
                awaitItem() shouldBe listOf(userOneEntity)
            }
        }
    }

    @Nested
    inner class ClearForUser {
        @Test
        fun `clearForUser removes only that user's rows`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val userOneEntity = UserTagVocabularyEntity(
                userId = 1,
                category = "genre",
                name = "sci-fi",
            )
            val userTwoEntity = UserTagVocabularyEntity(
                userId = 2,
                category = "genre",
                name = "fantasy",
            )
            dao.upsertAll(listOf(userOneEntity, userTwoEntity))

            // ----- Act -----
            dao.clearForUser(userId = 1)

            // ----- Assert -----
            dao.observe(userId = 1).test {
                awaitItem() shouldBe emptyList()
            }
            dao.observe(userId = 2).test {
                awaitItem() shouldBe listOf(userTwoEntity)
            }
        }
    }

    @Nested
    inner class InsertIfAbsent {
        @Test
        fun `inserts a brand-new key`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val entity = UserTagVocabularyEntity(
                userId = 1,
                category = "genre",
                name = "sci-fi",
                usageCount = 1,
            )

            // ----- Act -----
            dao.insertIfAbsent(listOf(entity))

            // ----- Assert -----
            dao.observe(userId = 1).test {
                awaitItem() shouldBe listOf(entity)
            }
        }

        @Test
        fun `leaves the existing usageCount unchanged when the key already exists`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val existing = UserTagVocabularyEntity(
                userId = 1,
                category = "genre",
                name = "sci-fi",
                usageCount = 12,
            )
            dao.upsertAll(listOf(existing))

            val conflicting = UserTagVocabularyEntity(
                userId = 1,
                category = "genre",
                name = "sci-fi",
                usageCount = 1,
            )

            // ----- Act -----
            dao.insertIfAbsent(listOf(conflicting))

            // ----- Assert -----
            dao.observe(userId = 1).test {
                awaitItem() shouldBe listOf(existing)
            }
        }
    }

    @Nested
    inner class ReplaceForUser {
        @Test
        fun `replaces only the given user's rows and leaves other users' rows intact`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val userOneEntity = UserTagVocabularyEntity(
                userId = 1,
                category = "genre",
                name = "sci-fi",
            )
            val userTwoEntity = UserTagVocabularyEntity(
                userId = 2,
                category = "genre",
                name = "fantasy",
            )
            dao.upsertAll(listOf(userOneEntity, userTwoEntity))

            val newRows = listOf(
                UserTagVocabularyEntity(
                    userId = 1,
                    category = "mood",
                    name = "cozy",
                    usageCount = 5,
                ),
            )

            // ----- Act -----
            dao.replaceForUser(
                userId = 1,
                entities = newRows,
            )

            // ----- Assert -----
            dao.observe(userId = 1).test {
                awaitItem() shouldBe newRows
            }
            dao.observe(userId = 2).test {
                awaitItem() shouldBe listOf(userTwoEntity)
            }
        }
    }
}
