package nl.rhaydus.softcover.core.corpus.data.repository

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.corpus.data.datasource.MoodMomentsLocalDataSource
import nl.rhaydus.softcover.core.database.model.MoodMomentEntity
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class MoodMomentsRepositoryImplTest {
    private class FakeDataSource : MoodMomentsLocalDataSource {
        val rows = MutableStateFlow<List<MoodMomentEntity>>(emptyList())

        override fun observeAll(): Flow<List<MoodMomentEntity>> = rows

        override fun observe(bookId: Int): Flow<List<MoodMomentEntity>> = rows.map { all -> all.filter { it.bookId == bookId } }

        override suspend fun insert(entity: MoodMomentEntity): Long {
            val id = rows.value.size + 1L
            rows.value = rows.value + entity.copy(id = id)

            return id
        }

        override suspend fun update(entity: MoodMomentEntity) {
            rows.value = rows.value.map { if (it.id == entity.id) entity else it }
        }

        override suspend fun delete(id: Long) {
            rows.value = rows.value.filter { it.id != id }
        }
    }

    private class FixedClock(private val millis: Long) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(millis)
    }

    private lateinit var dataSource: FakeDataSource
    private lateinit var repository: MoodMomentsRepositoryImpl

    @BeforeEach
    fun setUp() {
        dataSource = FakeDataSource()

        repository = MoodMomentsRepositoryImpl(
            localDataSource = dataSource,
            clock = FixedClock(millis = 5_000L),
        )
    }

    private fun entity(
        id: Long,
        bookId: Int = 1,
        percent: Int? = null,
        page: Int? = null,
        seconds: Int? = null,
        recordedAt: Long = 0L,
        word: String = "curious",
    ) = MoodMomentEntity(
        id = id,
        bookId = bookId,
        word = word,
        isOwn = false,
        percent = percent,
        page = page,
        seconds = seconds,
        recordedAt = recordedAt,
    )

    @Nested
    inner class Observe {
        @Test
        fun `moments sort by percent then page then seconds then recorded time with missing values last`() = runTest {
            // ----- Arrange -----
            dataSource.rows.value = listOf(
                entity(id = 1),
                entity(
                    id = 2,
                    percent = 50,
                    page = 9,
                ),
                entity(
                    id = 3,
                    percent = 50,
                    page = 2,
                    seconds = 90,
                ),
                entity(
                    id = 4,
                    percent = 50,
                    page = 2,
                    seconds = 10,
                ),
                entity(
                    id = 5,
                    percent = 10,
                ),
                entity(
                    id = 6,
                    percent = 50,
                    page = 2,
                    seconds = 10,
                    recordedAt = -1L,
                ),
                entity(
                    id = 7,
                    percent = 50,
                ),
            )

            // ----- Act & Assert -----
            repository.observe(bookId = 1).test {
                awaitItem().map { it.id } shouldBe listOf(5L, 6L, 4L, 3L, 2L, 7L, 1L)
            }
        }

        @Test
        fun `all moments are grouped per book, sorted, and unreadable words are skipped`() = runTest {
            // ----- Arrange -----
            dataSource.rows.value = listOf(
                entity(
                    id = 1,
                    bookId = 1,
                    percent = 80,
                ),
                entity(
                    id = 2,
                    bookId = 1,
                    percent = 20,
                ),
                entity(
                    id = 3,
                    bookId = 2,
                    word = "retired",
                ),
            )

            // ----- Act & Assert -----
            repository.observeAll().test {
                awaitItem().mapValues { (_, moments) -> moments.map { it.id } } shouldBe mapOf(1 to listOf(2L, 1L))
            }
        }
    }

    @Nested
    inner class Add {
        @Test
        fun `stamps the moment with the clock and clamps strength`() = runTest {
            // ----- Arrange -----
            // ----- Act -----
            repository.add(
                bookId = 3,
                word = MoodWord.Preset(mood = Mood.Gripped),
                strength = 500,
                percent = 40,
                page = null,
                seconds = null,
            )

            // ----- Assert -----
            dataSource.rows.value shouldBe listOf(
                MoodMomentEntity(
                    id = 1,
                    bookId = 3,
                    word = "gripped",
                    isOwn = false,
                    strength = 100,
                    percent = 40,
                    recordedAt = 5_000L,
                ),
            )
        }

        @Test
        fun `an own word is trimmed before it is stored`() = runTest {
            // ----- Arrange -----
            // ----- Act -----
            repository.add(
                bookId = 3,
                word = MoodWord.Own(text = "  Giddy "),
                strength = null,
                percent = null,
                page = null,
                seconds = null,
            )

            // ----- Assert -----
            dataSource.rows.value.map { it.word to it.isOwn } shouldBe listOf("Giddy" to true)
        }

        @Test
        fun `a blank own word is rejected and nothing is stored`() = runTest {
            // ----- Arrange -----
            // ----- Act & Assert -----
            shouldThrow<IllegalArgumentException> {
                repository.add(
                    bookId = 3,
                    word = MoodWord.Own(text = "   "),
                    strength = null,
                    percent = null,
                    page = null,
                    seconds = null,
                )
            }

            dataSource.rows.value shouldBe emptyList()
        }

        @Test
        fun `a preset outside the moment list is rejected and nothing is stored`() = runTest {
            // ----- Arrange -----
            // ----- Act & Assert -----
            shouldThrow<IllegalArgumentException> {
                repository.add(
                    bookId = 3,
                    word = MoodWord.Preset(mood = Mood.Cosy),
                    strength = null,
                    percent = null,
                    page = null,
                    seconds = null,
                )
            }

            dataSource.rows.value shouldBe emptyList()
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `keeps the original recorded time and validates the word`() = runTest {
            // ----- Arrange -----
            dataSource.rows.value = listOf(
                entity(
                    id = 1,
                    recordedAt = 111L,
                ),
            )

            val moment = MoodMoment(
                id = 1,
                bookId = 1,
                word = MoodWord.Own(text = " Giddy "),
                strength = -4,
                percent = 5,
                page = null,
                seconds = null,
                recordedAt = 111L,
            )

            // ----- Act -----
            repository.update(moment = moment)

            // ----- Assert -----
            dataSource.rows.value shouldBe listOf(
                MoodMomentEntity(
                    id = 1,
                    bookId = 1,
                    word = "Giddy",
                    isOwn = true,
                    strength = 0,
                    percent = 5,
                    recordedAt = 111L,
                ),
            )
        }

        @Test
        fun `a blank own word is rejected and the stored moment is untouched`() = runTest {
            // ----- Arrange -----
            dataSource.rows.value = listOf(entity(id = 1))

            val moment = MoodMoment(
                id = 1,
                bookId = 1,
                word = MoodWord.Own(text = ""),
                strength = null,
                percent = null,
                page = null,
                seconds = null,
                recordedAt = 0L,
            )

            // ----- Act & Assert -----
            shouldThrow<IllegalArgumentException> { repository.update(moment = moment) }
            dataSource.rows.value shouldBe listOf(entity(id = 1))
        }
    }
}
