package nl.rhaydus.softcover.feature.personal.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.personal.data.datasource.PersonalReviewLocalDataSource
import nl.rhaydus.softcover.feature.personal.data.model.PersonalReviewEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PersonalReviewRepositoryImplTest {

    private lateinit var localDataSource: PersonalReviewLocalDataSource
    private lateinit var repository: PersonalReviewRepositoryImpl

    @BeforeEach
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        repository = PersonalReviewRepositoryImpl(localDataSource = localDataSource)
    }

    private fun buildEntity(
        bookId: Int = 1,
        body: String = "Review body",
        hasSpoilers: Boolean = false,
        isDraft: Boolean = false,
        updatedAt: String = "2024-01-15T10:30:00Z",
    ) = PersonalReviewEntity(
        bookId = bookId,
        body = body,
        hasSpoilers = hasSpoilers,
        isDraft = isDraft,
        updatedAt = updatedAt,
    )

    @Nested
    inner class Observe {

        @Test
        fun `emits null when data source emits null`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observe(bookId = 1)
            } returns flowOf(null)

            // ----- Act & Assert -----
            repository.observe(bookId = 1).test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }

        @Test
        fun `emits mapped domain model when data source emits entity`() = runTest {
            // ----- Arrange -----
            val entity = buildEntity(body = "My review")

            every {
                localDataSource.observe(bookId = 1)
            } returns flowOf(entity)

            // ----- Act & Assert -----
            repository.observe(bookId = 1).test {
                val item = awaitItem()
                item?.bookId shouldBe 1
                item?.body shouldBe "My review"
                item?.isDraft shouldBe false
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ObserveAll {

        @Test
        fun `emits empty list when data source emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeAll()
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            repository.observeAll().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }

        @Test
        fun `maps all entities to domain models`() = runTest {
            // ----- Arrange -----
            val entities = listOf(
                buildEntity(bookId = 1),
                buildEntity(bookId = 2),
            )

            every {
                localDataSource.observeAll()
            } returns flowOf(entities)

            // ----- Act & Assert -----
            repository.observeAll().test {
                val items = awaitItem()
                items.size shouldBe 2
                items[0].bookId shouldBe 1
                items[1].bookId shouldBe 2
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SaveDraft {

        @Test
        fun `upserts entity with isDraft true`() = runTest {
            // ----- Arrange -----
            val entitySlot = slot<PersonalReviewEntity>()

            coEvery {
                localDataSource.upsert(capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.saveDraft(
                bookId = 1,
                body = "Draft text",
                hasSpoilers = true,
            )

            // ----- Assert -----
            entitySlot.captured.bookId shouldBe 1
            entitySlot.captured.body shouldBe "Draft text"
            entitySlot.captured.hasSpoilers shouldBe true
            entitySlot.captured.isDraft shouldBe true
        }
    }

    @Nested
    inner class Publish {

        @Test
        fun `upserts entity with isDraft false`() = runTest {
            // ----- Arrange -----
            val entitySlot = slot<PersonalReviewEntity>()

            coEvery {
                localDataSource.upsert(capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.publish(
                bookId = 2,
                body = "Published review",
                hasSpoilers = false,
            )

            // ----- Assert -----
            entitySlot.captured.bookId shouldBe 2
            entitySlot.captured.body shouldBe "Published review"
            entitySlot.captured.hasSpoilers shouldBe false
            entitySlot.captured.isDraft shouldBe false
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delegates delete to local data source`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            repository.delete(bookId = 42)

            // ----- Assert -----
            coVerify {
                localDataSource.delete(bookId = 42)
            }
        }
    }
}
