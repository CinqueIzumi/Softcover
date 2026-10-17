package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.ListBookEntity
import nl.rhaydus.softcover.core.database.model.ListBookPositionRow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDaoListBookPositionsTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: BookDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.bookDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    private suspend fun insertListBook(
        listBookId: Int,
        listId: Int = 1,
        position: Int?,
    ) {
        dao.insertListBook(
            listBook = ListBookEntity(
                listBookId = listBookId,
                listId = listId,
                bookId = listBookId,
                editionId = listBookId,
                position = position,
            ),
        )
    }

    @Nested
    inner class GetListBookPositions {
        @Test
        fun `returns rows matching by id or by position window in the list only`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertListBook(
                listBookId = 1,
                position = 0,
            )
            insertListBook(
                listBookId = 2,
                position = 5,
            )
            insertListBook(
                listBookId = 3,
                position = 2,
            )
            insertListBook(
                listBookId = 4,
                position = 9,
            )
            insertListBook(
                listBookId = 5,
                listId = 2,
                position = 2,
            )

            // ----- Act -----
            val rows = dao.getListBookPositions(
                listId = 1,
                listBookIds = listOf(1),
                positions = listOf(2),
            )

            // ----- Assert -----
            rows shouldContainExactlyInAnyOrder listOf(
                ListBookPositionRow(
                    listBookId = 1,
                    position = 0,
                ),
                ListBookPositionRow(
                    listBookId = 3,
                    position = 2,
                ),
            )
        }
    }

    @Nested
    inner class RestoreListBookPositions {
        @Test
        fun `writes snapshotted positions back, including clearing a position to null`() = runTest(testDispatcher) {
            // ----- Arrange -----
            insertListBook(
                listBookId = 1,
                position = 7,
            )
            insertListBook(
                listBookId = 2,
                position = 8,
            )

            // ----- Act -----
            dao.restoreListBookPositions(
                rows = listOf(
                    ListBookPositionRow(
                        listBookId = 1,
                        position = 3,
                    ),
                    ListBookPositionRow(
                        listBookId = 2,
                        position = null,
                    ),
                ),
            )

            // ----- Assert -----
            val rows = dao.getListBookPositions(
                listId = 1,
                listBookIds = listOf(1, 2),
                positions = emptyList(),
            )

            rows shouldContainExactlyInAnyOrder listOf(
                ListBookPositionRow(
                    listBookId = 1,
                    position = 3,
                ),
                ListBookPositionRow(
                    listBookId = 2,
                    position = null,
                ),
            )
            rows.size shouldBe 2
        }
    }
}
