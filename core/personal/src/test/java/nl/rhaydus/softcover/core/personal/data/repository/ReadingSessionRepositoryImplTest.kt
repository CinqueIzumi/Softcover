package nl.rhaydus.softcover.core.personal.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.model.ReadingSessionEntity
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.personal.data.datasource.ReadingSessionLocalDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class ReadingSessionRepositoryImplTest {

    private lateinit var localDataSource: ReadingSessionLocalDataSource
    private lateinit var repository: ReadingSessionRepositoryImpl

    @BeforeEach
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        repository = ReadingSessionRepositoryImpl(localDataSource = localDataSource)
    }

    private fun buildEntity(
        id: Long = 1L,
        bookId: Int = 10,
        startedAt: String = "2024-05-01T09:00:00Z",
        endedAt: String? = null,
        startPage: Int? = 100,
        endPage: Int? = null,
        startSeconds: Int? = null,
        endSeconds: Int? = null,
        pausedSeconds: Int = 0,
        lastPausedAt: String? = null,
    ) = ReadingSessionEntity(
        id = id,
        bookId = bookId,
        startedAt = startedAt,
        endedAt = endedAt,
        startPage = startPage,
        endPage = endPage,
        startSeconds = startSeconds,
        endSeconds = endSeconds,
        pausedSeconds = pausedSeconds,
        lastPausedAt = lastPausedAt,
    )

    private fun buildSession(
        id: Long = 1L,
        bookId: Int = 10,
        startedAt: Instant = Instant.parse("2024-05-01T09:00:00Z"),
        endedAt: Instant? = null,
        startPage: Int? = 100,
        endPage: Int? = null,
        startSeconds: Int? = null,
        endSeconds: Int? = null,
    ) = ReadingSession(
        id = id,
        bookId = bookId,
        startedAt = startedAt,
        endedAt = endedAt,
        startPage = startPage,
        endPage = endPage,
        startSeconds = startSeconds,
        endSeconds = endSeconds,
    )

    @Nested
    inner class ObserveByBookId {

        @Test
        fun `maps entities to domain models`() = runTest {
            // ----- Arrange -----
            val entities = listOf(buildEntity(id = 1L), buildEntity(id = 2L))

            every {
                localDataSource.observeByBookId(bookId = 10)
            } returns flowOf(entities)

            // ----- Act & Assert -----
            repository.observeByBookId(bookId = 10).test {
                val items = awaitItem()
                items.size shouldBe 2
                items[0].id shouldBe 1L
                items[1].id shouldBe 2L
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ObserveAll {

        @Test
        fun `maps all entities to domain models`() = runTest {
            // ----- Arrange -----
            val entities = listOf(buildEntity(bookId = 10), buildEntity(bookId = 20))

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
    inner class ObserveActive {

        @Test
        fun `emits null when no active session`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeActive()
            } returns flowOf(null)

            // ----- Act & Assert -----
            repository.observeActive().test {
                awaitItem() shouldBe null
                awaitComplete()
            }
        }

        @Test
        fun `emits mapped domain session when active session exists`() = runTest {
            // ----- Arrange -----
            val entity = buildEntity(id = 5L, endedAt = null)

            every {
                localDataSource.observeActive()
            } returns flowOf(entity)

            // ----- Act & Assert -----
            repository.observeActive().test {
                val item = awaitItem()
                item?.id shouldBe 5L
                item?.endedAt shouldBe null
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ObserveById {

        @Test
        fun `emits null when session not found`() = runTest {
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
    }

    @Nested
    inner class Start {

        @Test
        fun `inserts entity with null endedAt and returns generated id`() = runTest {
            // ----- Arrange -----
            val entitySlot = slot<ReadingSessionEntity>()

            coEvery {
                localDataSource.insert(capture(entitySlot))
            } returns 55L

            // ----- Act -----
            val result = repository.start(
                bookId = 10,
                startPage = 200,
                startSeconds = null,
            )

            // ----- Assert -----
            result shouldBe 55L
            entitySlot.captured.bookId shouldBe 10
            entitySlot.captured.endedAt shouldBe null
            entitySlot.captured.startPage shouldBe 200
        }
    }

    @Nested
    inner class Stop {

        @Test
        fun `sets endedAt on existing session when stopping`() = runTest {
            // ----- Arrange -----
            val existingEntity = buildEntity(id = 3L, endedAt = null)
            val updatedEntitySlot = slot<ReadingSessionEntity>()

            every {
                localDataSource.observeById(id = 3L)
            } returns flowOf(existingEntity)

            coEvery {
                localDataSource.update(capture(updatedEntitySlot))
            } returns Unit

            // ----- Act -----
            repository.stop(
                id = 3L,
                endPage = 150,
                endSeconds = null,
            )

            // ----- Assert -----
            updatedEntitySlot.captured.endedAt shouldNotBe null
            updatedEntitySlot.captured.endPage shouldBe 150
        }

        @Test
        fun `does nothing when session not found`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeById(id = 999L)
            } returns flowOf(null)

            // ----- Act -----
            repository.stop(
                id = 999L,
                endPage = null,
                endSeconds = null,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                localDataSource.update(any())
            }
        }

        @Test
        fun `folds open pause into pausedSeconds and clears lastPausedAt`() = runTest {
            // ----- Arrange -----
            val knownPausedAt = Instant.now().minusSeconds(60).toString()
            val existingEntity = buildEntity(
                id = 4L,
                pausedSeconds = 100,
                lastPausedAt = knownPausedAt,
            )

            val updatedEntitySlot = slot<ReadingSessionEntity>()

            every {
                localDataSource.observeById(id = 4L)
            } returns flowOf(existingEntity)

            coEvery {
                localDataSource.update(capture(updatedEntitySlot))
            } returns Unit

            // ----- Act -----
            repository.stop(id = 4L, endPage = null, endSeconds = null)

            // ----- Assert -----
            val captured = updatedEntitySlot.captured

            captured.lastPausedAt shouldBe null
            captured.endedAt shouldNotBe null
            // accumulated = 100 + ~60 s open pause; must be at least 160 s
            (captured.pausedSeconds >= 160) shouldBe true
        }
    }

    @Nested
    inner class Pause {

        @Test
        fun `sets lastPausedAt when session is active and not yet paused`() = runTest {
            // ----- Arrange -----
            val existingEntity = buildEntity(id = 5L, endedAt = null, lastPausedAt = null)
            val updatedEntitySlot = slot<ReadingSessionEntity>()

            every {
                localDataSource.observeById(id = 5L)
            } returns flowOf(existingEntity)

            coEvery {
                localDataSource.update(capture(updatedEntitySlot))
            } returns Unit

            // ----- Act -----
            repository.pause(id = 5L)

            // ----- Assert -----
            updatedEntitySlot.captured.lastPausedAt shouldNotBe null
        }

        @Test
        fun `does nothing when session is already paused`() = runTest {
            // ----- Arrange -----
            val existingEntity = buildEntity(
                id = 5L,
                endedAt = null,
                lastPausedAt = "2024-05-01T09:10:00Z",
            )

            every {
                localDataSource.observeById(id = 5L)
            } returns flowOf(existingEntity)

            // ----- Act -----
            repository.pause(id = 5L)

            // ----- Assert -----
            coVerify(exactly = 0) { localDataSource.update(any()) }
        }

        @Test
        fun `does nothing when session is already ended`() = runTest {
            // ----- Arrange -----
            val existingEntity = buildEntity(
                id = 5L,
                endedAt = "2024-05-01T10:00:00Z",
                lastPausedAt = null,
            )

            every {
                localDataSource.observeById(id = 5L)
            } returns flowOf(existingEntity)

            // ----- Act -----
            repository.pause(id = 5L)

            // ----- Assert -----
            coVerify(exactly = 0) { localDataSource.update(any()) }
        }

        @Test
        fun `does nothing when session not found`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeById(id = 99L)
            } returns flowOf(null)

            // ----- Act -----
            repository.pause(id = 99L)

            // ----- Assert -----
            coVerify(exactly = 0) { localDataSource.update(any()) }
        }
    }

    @Nested
    inner class Resume {

        @Test
        fun `accumulates pausedSeconds and clears lastPausedAt`() = runTest {
            // ----- Arrange -----
            val knownPausedAt = Instant.now().minusSeconds(60).toString()
            val existingEntity = buildEntity(
                id = 6L,
                pausedSeconds = 50,
                lastPausedAt = knownPausedAt,
            )

            val updatedEntitySlot = slot<ReadingSessionEntity>()

            every {
                localDataSource.observeById(id = 6L)
            } returns flowOf(existingEntity)

            coEvery {
                localDataSource.update(capture(updatedEntitySlot))
            } returns Unit

            // ----- Act -----
            repository.resume(id = 6L)

            // ----- Assert -----
            val captured = updatedEntitySlot.captured

            captured.lastPausedAt shouldBe null
            // accumulated = 50 + ~60 s; must be at least 110 s
            (captured.pausedSeconds >= 110) shouldBe true
        }

        @Test
        fun `does nothing when session is not paused`() = runTest {
            // ----- Arrange -----
            val existingEntity = buildEntity(id = 6L, lastPausedAt = null)

            every {
                localDataSource.observeById(id = 6L)
            } returns flowOf(existingEntity)

            // ----- Act -----
            repository.resume(id = 6L)

            // ----- Assert -----
            coVerify(exactly = 0) { localDataSource.update(any()) }
        }

        @Test
        fun `does nothing when session not found`() = runTest {
            // ----- Arrange -----
            every {
                localDataSource.observeById(id = 99L)
            } returns flowOf(null)

            // ----- Act -----
            repository.resume(id = 99L)

            // ----- Assert -----
            coVerify(exactly = 0) { localDataSource.update(any()) }
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `delegates update to local data source with converted entity`() = runTest {
            // ----- Arrange -----
            val session = buildSession(id = 7L, endPage = 300)
            val entitySlot = slot<ReadingSessionEntity>()

            coEvery {
                localDataSource.update(capture(entitySlot))
            } returns Unit

            // ----- Act -----
            repository.update(session = session)

            // ----- Assert -----
            entitySlot.captured.id shouldBe 7L
            entitySlot.captured.endPage shouldBe 300
        }
    }

    @Nested
    inner class Delete {

        @Test
        fun `delegates delete to local data source`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            repository.delete(id = 8L)

            // ----- Assert -----
            coVerify {
                localDataSource.delete(id = 8L)
            }
        }
    }
}
