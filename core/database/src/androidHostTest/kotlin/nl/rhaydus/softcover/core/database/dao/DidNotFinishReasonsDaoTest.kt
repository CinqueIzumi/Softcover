package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.BookMoodEntity
import nl.rhaydus.softcover.core.database.model.DidNotFinishReasonsEntity
import nl.rhaydus.softcover.core.database.model.MoodMomentEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DidNotFinishReasonsDaoTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: DidNotFinishReasonsDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )

        dao = database.didNotFinishReasonsDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class DeleteAllLocalData {
        @Test
        fun `clearing local data empties the did-not-finish reasons`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.upsert(
                entity = DidNotFinishReasonsEntity(
                    bookId = 1,
                    notTheRightTime = true,
                    notMyKindOfWriting = false,
                    tooSlow = false,
                    lostInterest = true,
                    unwantedContent = false,
                    ownWords = "dragged",
                    updatedAt = 10L,
                ),
            )

            // ----- Act -----
            database.bookDao().deleteAllLocalData()

            // ----- Assert -----
            dao.get(bookId = 1).shouldBeNull()
        }

        @Test
        fun `clearing local data empties the book moods and mood moments`() = runTest(testDispatcher) {
            // ----- Arrange -----
            database.bookMoodsDao().insertAll(
                listOf(
                    BookMoodEntity(
                        bookId = 1,
                        word = "cozy",
                        isOwn = false,
                    ),
                ),
            )

            database.moodMomentsDao().insert(
                MoodMomentEntity(
                    bookId = 1,
                    word = "cozy",
                    isOwn = false,
                    recordedAt = 5L,
                ),
            )

            // ----- Act -----
            database.bookDao().deleteAllLocalData()

            // ----- Assert -----
            database.bookMoodsDao().observeAll().first().shouldBeEmpty()
            database.moodMomentsDao().observeAll().first().shouldBeEmpty()
        }
    }
}
