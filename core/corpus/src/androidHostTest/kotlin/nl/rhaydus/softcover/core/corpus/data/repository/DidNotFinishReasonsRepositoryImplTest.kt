package nl.rhaydus.softcover.core.corpus.data.repository

import app.cash.turbine.test
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.corpus.data.datasource.DidNotFinishReasonsLocalDataSource
import nl.rhaydus.softcover.core.database.model.DidNotFinishReasonsEntity
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class DidNotFinishReasonsRepositoryImplTest {
    private class FakeDataSource : DidNotFinishReasonsLocalDataSource {
        val rows = MutableStateFlow<Map<Int, DidNotFinishReasonsEntity>>(emptyMap())

        override fun observeAll(): Flow<List<DidNotFinishReasonsEntity>> = rows.map { it.values.toList() }

        override fun observe(bookId: Int): Flow<DidNotFinishReasonsEntity?> = rows.map { it[bookId] }

        override suspend fun get(bookId: Int): DidNotFinishReasonsEntity? = rows.value[bookId]

        override suspend fun upsert(entity: DidNotFinishReasonsEntity) {
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
    private lateinit var repository: DidNotFinishReasonsRepositoryImpl

    @BeforeEach
    fun setUp() {
        dataSource = FakeDataSource()

        repository = DidNotFinishReasonsRepositoryImpl(
            localDataSource = dataSource,
            clock = FixedClock(millis = 123_456L),
        )
    }

    @Nested
    inner class ObserveAll {
        @Test
        fun `keys the stored reasons by book id`() = runTest {
            // ----- Arrange -----
            repository.save(
                reasons = DidNotFinishReasons(
                    bookId = 1,
                    reasons = setOf(DidNotFinishReason.TooSlow),
                ),
            )

            repository.save(
                reasons = DidNotFinishReasons(
                    bookId = 2,
                    ownWords = "Dull",
                ),
            )

            // ----- Act & Assert -----
            repository.observeAll().test {
                awaitItem() shouldBe mapOf(
                    1 to DidNotFinishReasons(
                        bookId = 1,
                        reasons = setOf(DidNotFinishReason.TooSlow),
                    ),
                    2 to DidNotFinishReasons(
                        bookId = 2,
                        ownWords = "Dull",
                    ),
                )
            }
        }
    }

    @Nested
    inner class Observe {
        @Test
        fun `emits empty reasons for the book while nothing is stored`() = runTest {
            // ----- Arrange -----
            // ----- Act & Assert -----
            repository.observe(bookId = 9).test {
                awaitItem() shouldBe DidNotFinishReasons(bookId = 9)
            }
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `own words are trimmed before they are stored`() = runTest {
            // ----- Arrange -----
            val reasons = DidNotFinishReasons(
                bookId = 9,
                reasons = setOf(DidNotFinishReason.LostInterest),
                ownWords = "  Lost the plot \n",
            )

            // ----- Act -----
            repository.save(reasons = reasons)

            // ----- Assert -----
            dataSource.get(bookId = 9) shouldBe DidNotFinishReasonsEntity(
                bookId = 9,
                notTheRightTime = false,
                notMyKindOfWriting = false,
                tooSlow = false,
                lostInterest = true,
                unwantedContent = false,
                ownWords = "Lost the plot",
                updatedAt = 123_456L,
            )
        }

        @Test
        fun `blank own words with reasons store no own words`() = runTest {
            // ----- Arrange -----
            val reasons = DidNotFinishReasons(
                bookId = 9,
                reasons = setOf(DidNotFinishReason.TooSlow),
                ownWords = "   ",
            )

            // ----- Act -----
            repository.save(reasons = reasons)

            // ----- Assert -----
            dataSource.get(bookId = 9)?.ownWords.shouldBeNull()

            repository.get(bookId = 9) shouldBe DidNotFinishReasons(
                bookId = 9,
                reasons = setOf(DidNotFinishReason.TooSlow),
            )
        }

        @Test
        fun `own words alone are stored`() = runTest {
            // ----- Arrange -----
            val reasons = DidNotFinishReasons(
                bookId = 9,
                ownWords = "Not for me",
            )

            // ----- Act -----
            repository.save(reasons = reasons)

            // ----- Assert -----
            repository.get(bookId = 9) shouldBe reasons
        }

        @Test
        fun `no reasons and no own words stores nothing`() = runTest {
            // ----- Arrange -----
            val reasons = DidNotFinishReasons(bookId = 9)

            // ----- Act -----
            repository.save(reasons = reasons)

            // ----- Assert -----
            dataSource.get(bookId = 9).shouldBeNull()
        }

        @Test
        fun `no reasons and blank own words deletes the stored row`() = runTest {
            // ----- Arrange -----
            repository.save(
                reasons = DidNotFinishReasons(
                    bookId = 9,
                    reasons = setOf(DidNotFinishReason.TooSlow),
                ),
            )

            // ----- Act -----
            repository.save(
                reasons = DidNotFinishReasons(
                    bookId = 9,
                    ownWords = " \t ",
                ),
            )

            // ----- Assert -----
            dataSource.get(bookId = 9).shouldBeNull()
        }
    }
}
