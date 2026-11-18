package nl.rhaydus.softcover.core.corpus.data.repository

import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.corpus.data.datasource.BookCopyLocalDataSource
import nl.rhaydus.softcover.core.database.model.BookCopyEntity
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class BookCopyRepositoryImplTest {
    private class FakeDataSource : BookCopyLocalDataSource {
        val rows = MutableStateFlow<Map<Int, BookCopyEntity>>(emptyMap())

        override fun observe(bookId: Int): Flow<BookCopyEntity?> = rows.map { it[bookId] }

        override suspend fun get(bookId: Int): BookCopyEntity? = rows.value[bookId]

        override suspend fun upsert(entity: BookCopyEntity) {
            rows.value = rows.value + (entity.bookId to entity)
        }

        override suspend fun delete(bookId: Int) {
            rows.value = rows.value - bookId
        }
    }

    private class FixedClock(private val millis: Long) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(millis)
    }

    private lateinit var dataSource: FakeDataSource
    private lateinit var repository: BookCopyRepositoryImpl

    @BeforeEach
    fun setUp() {
        dataSource = FakeDataSource()

        repository = BookCopyRepositoryImpl(
            localDataSource = dataSource,
            clock = FixedClock(millis = 123_456L),
        )
    }

    @Nested
    inner class Observe {
        @Test
        fun `emits an empty copy for the book while nothing is stored`() = runTest {
            // ----- Arrange -----
            // ----- Act & Assert -----
            repository.observe(bookId = 9).test {
                awaitItem() shouldBe BookCopy(bookId = 9)
            }
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `an empty copy deletes the stored row`() = runTest {
            // ----- Arrange -----
            repository.save(
                copy = BookCopy(
                    bookId = 9,
                    source = AcquisitionSource.Preset(kind = AcquisitionKind.GIFT),
                ),
            )

            // ----- Act -----
            repository.save(copy = BookCopy(bookId = 9))

            // ----- Assert -----
            dataSource.get(bookId = 9).shouldBeNull()
        }

        @Test
        fun `other with whitespace-only text deletes the stored row`() = runTest {
            // ----- Arrange -----
            repository.save(
                copy = BookCopy(
                    bookId = 9,
                    source = AcquisitionSource.Preset(kind = AcquisitionKind.GIFT),
                ),
            )

            // ----- Act -----
            repository.save(
                copy = BookCopy(
                    bookId = 9,
                    source = AcquisitionSource.Other(text = "  \t"),
                ),
            )

            // ----- Assert -----
            dataSource.get(bookId = 9).shouldBeNull()
        }

        @Test
        fun `a preset is stored with the clock time and read back`() = runTest {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 9,
                source = AcquisitionSource.Preset(kind = AcquisitionKind.LIBRARY),
            )

            // ----- Act -----
            repository.save(copy = copy)

            // ----- Assert -----
            dataSource.get(bookId = 9) shouldBe BookCopyEntity(
                bookId = 9,
                source = "library",
                customSource = null,
                updatedAt = 123_456L,
            )

            repository.get(bookId = 9) shouldBe copy
        }
    }
}
