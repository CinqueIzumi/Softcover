package nl.rhaydus.softcover.core.corpus.data.repository

import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.corpus.data.datasource.BookTraitsLocalDataSource
import nl.rhaydus.softcover.core.database.model.BookTraitsEntity
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.Pace
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class BookTraitsRepositoryImplTest {
    private class FakeDataSource : BookTraitsLocalDataSource {
        val rows = MutableStateFlow<Map<Int, BookTraitsEntity>>(emptyMap())

        override fun observe(bookId: Int): Flow<BookTraitsEntity?> = rows.map { it[bookId] }

        override suspend fun get(bookId: Int): BookTraitsEntity? = rows.value[bookId]

        override suspend fun upsert(entity: BookTraitsEntity) {
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
    private lateinit var repository: BookTraitsRepositoryImpl

    @BeforeEach
    fun setUp() {
        dataSource = FakeDataSource()
        repository = BookTraitsRepositoryImpl(
            localDataSource = dataSource,
            clock = FixedClock(millis = 123_456L),
        )
    }

    @Nested
    inner class Observe {
        @Test
        fun `emits empty traits for the book when no row exists`() = runTest {
            // ----- Arrange -----
            // ----- Act & Assert -----
            repository.observe(bookId = 9).test {
                awaitItem() shouldBe BookTraits(bookId = 9)
            }
        }

        @Test
        fun `re-emits after a save`() = runTest {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 9,
                pace = Pace.FAST,
            )

            // ----- Act & Assert -----
            repository.observe(bookId = 9).test {
                awaitItem() shouldBe BookTraits(bookId = 9)
                repository.save(traits = traits)
                awaitItem() shouldBe traits
            }
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `stores non-empty traits so a later get returns them stamped with the clock time`() = runTest {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 9,
                pace = Pace.SLOW,
                plotCharacterBalance = 20,
            )

            // ----- Act -----
            repository.save(traits = traits)

            // ----- Assert -----
            repository.get(bookId = 9) shouldBe traits
            dataSource.rows.value[9].shouldNotBeNull().updatedAt shouldBe 123_456L
        }

        @Test
        fun `saving empty traits removes the existing row`() = runTest {
            // ----- Arrange -----
            repository.save(
                traits = BookTraits(
                    bookId = 9,
                    pace = Pace.SLOW,
                ),
            )

            // ----- Act -----
            repository.save(traits = BookTraits(bookId = 9))

            // ----- Assert -----
            repository.get(bookId = 9) shouldBe BookTraits(bookId = 9)
            dataSource.rows.value.containsKey(9) shouldBe false
        }
    }
}
