package nl.rhaydus.softcover.core.personal.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.data.database.model.HighlightEntity
import nl.rhaydus.softcover.core.personal.data.datasource.HighlightLocalDataSource
import nl.rhaydus.softcover.core.personal.domain.model.Highlight
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class HighlightRepositoryImplTest {

    private lateinit var localDataSource: HighlightLocalDataSource
    private lateinit var repository: HighlightRepositoryImpl

    @BeforeEach
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        repository = HighlightRepositoryImpl(localDataSource = localDataSource)
    }

    private fun buildEntity(
        id: Long = 1L,
        bookId: Int = 10,
        quote: String = "A quote",
        page: Int? = 5,
        note: String? = null,
        createdAt: String = "2024-06-01T08:00:00Z",
    ) = HighlightEntity(
        id = id,
        bookId = bookId,
        quote = quote,
        page = page,
        note = note,
        createdAt = createdAt,
    )

    private fun buildHighlight(
        id: Long = 1L,
        bookId: Int = 10,
        quote: String = "A quote",
        page: Int? = 5,
        note: String? = null,
        createdAt: Instant = Instant.parse("2024-06-01T08:00:00Z"),
    ) = Highlight(
        id = id,
        bookId = bookId,
        quote = quote,
        page = page,
        note = note,
        createdAt = createdAt,
    )

    @Nested
    inner class ObserveByBookId {

        @Test
        fun `emits empty list when data source emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeByBookId(bookId = 10)
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            repository.observeByBookId(bookId = 10).test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }

        @Test
        fun `maps entities to domain models`() = runTest {
            // ----- Arrange -----
            val entities = listOf(
                buildEntity(
                    id = 1L,
                    quote = "First",
                ),
                buildEntity(
                    id = 2L,
                    quote = "Second",
                ),
            )

            every {
                localDataSource.observeByBookId(bookId = 10)
            } returns flowOf(entities)

            // ----- Act & Assert -----
            repository.observeByBookId(bookId = 10).test {
                val items = awaitItem()
                items.size shouldBe 2
                items[0].quote shouldBe "First"
                items[1].quote shouldBe "Second"
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ObserveAll {

        @Test
        fun `maps all entities to domain models`() = runTest {
            // ----- Arrange -----
            val entities = listOf(
                buildEntity(
                    id = 1L,
                    bookId = 10,
                ),
                buildEntity(
                    id = 2L,
                    bookId = 20,
                ),
            )

            every {
                localDataSource.observeAll()
            } returns flowOf(entities)

            // ----- Act & Assert -----
            repository.observeAll().test {
                val items = awaitItem()
                items.size shouldBe 2
                items[0].bookId shouldBe 10
                items[1].bookId shouldBe 20
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ObserveById {

        @Test
        fun `emits null when data source emits null`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeById(id = 99L)
            } returns flowOf(null)

            // ----- Act & Assert -----
            repository.observeById(id = 99L).test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }

        @Test
        fun `emits mapped domain model when data source emits entity`() = runTest {
            // ----- Arrange -----
            val entity = buildEntity(
                id = 99L,
                quote = "Specific quote",
            )

            every {
                localDataSource.observeById(id = 99L)
            } returns flowOf(entity)

            // ----- Act & Assert -----
            repository.observeById(id = 99L).test {
                val item = awaitItem()
                item?.id shouldBe 99L
                item?.quote shouldBe "Specific quote"
                awaitComplete()
            }
        }
    }

    @Nested
    inner class Add {

        @Test
        fun `inserts entity and returns generated id`() = runTest {
            // ----- Arrange -----
            val entitySlot = slot<HighlightEntity>()

            coEvery {
                localDataSource.insert(capture(entitySlot))
            } returns 77L

            // ----- Act -----
            val result = repository.add(
                bookId = 10,
                quote = "New quote",
                page = 42,
                note = "Side note",
            )

            // ----- Assert -----
            result shouldBe 77L
            entitySlot.captured.bookId shouldBe 10
            entitySlot.captured.quote shouldBe "New quote"
            entitySlot.captured.page shouldBe 42
            entitySlot.captured.note shouldBe "Side note"
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `delegates update to local data source with converted entity`() = runTest {
            // ----- Arrange -----
            val highlight = buildHighlight(
                id = 5L,
                quote = "Updated quote",
            )
            val entitySlot = slot<HighlightEntity>()

            coEvery {
                localDataSource.update(capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.update(highlight = highlight)

            // ----- Assert -----
            entitySlot.captured.id shouldBe 5L
            entitySlot.captured.quote shouldBe "Updated quote"
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delegates delete to local data source`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            repository.delete(id = 11L)

            // ----- Assert -----
            coVerify {
                localDataSource.delete(id = 11L)
            }
        }
    }
}
