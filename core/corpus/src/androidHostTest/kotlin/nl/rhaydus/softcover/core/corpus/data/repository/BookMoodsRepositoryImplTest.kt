package nl.rhaydus.softcover.core.corpus.data.repository

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.corpus.data.datasource.BookMoodsLocalDataSource
import nl.rhaydus.softcover.core.database.model.BookMoodEntity
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookMoodsRepositoryImplTest {
    private class FakeDataSource : BookMoodsLocalDataSource {
        val rows = MutableStateFlow<List<BookMoodEntity>>(emptyList())

        override fun observeAll(): Flow<List<BookMoodEntity>> = rows

        override fun observe(bookId: Int): Flow<List<BookMoodEntity>> = rows.map { all -> all.filter { it.bookId == bookId } }

        override suspend fun replaceForBook(
            bookId: Int,
            entities: List<BookMoodEntity>,
        ) {
            rows.value = rows.value.filter { it.bookId != bookId } + entities
        }
    }

    private lateinit var dataSource: FakeDataSource
    private lateinit var repository: BookMoodsRepositoryImpl

    @BeforeEach
    fun setUp() {
        dataSource = FakeDataSource()
        repository = BookMoodsRepositoryImpl(localDataSource = dataSource)
    }

    @Nested
    inner class Observe {
        @Test
        fun `groups moods by book and skips books whose rows are all unreadable`() = runTest {
            // ----- Arrange -----
            dataSource.rows.value = listOf(
                BookMoodEntity(
                    bookId = 1,
                    word = "sad",
                    isOwn = false,
                ),
                BookMoodEntity(
                    bookId = 1,
                    word = "Wistful",
                    isOwn = true,
                ),
                BookMoodEntity(
                    bookId = 2,
                    word = "retired",
                    isOwn = false,
                ),
            )

            // ----- Act & Assert -----
            repository.observeAll().test {
                awaitItem() shouldBe mapOf(
                    1 to listOf(
                        BookMood(word = MoodWord.Preset(mood = Mood.Sad)),
                        BookMood(word = MoodWord.Own(text = "Wistful")),
                    ),
                )
            }
        }

        @Test
        fun `a single book drops rows with an unknown preset key`() = runTest {
            // ----- Arrange -----
            dataSource.rows.value = listOf(
                BookMoodEntity(
                    bookId = 1,
                    word = "retired",
                    isOwn = false,
                ),
                BookMoodEntity(
                    bookId = 1,
                    word = "dark",
                    isOwn = false,
                ),
            )

            // ----- Act & Assert -----
            repository.observe(bookId = 1).test {
                awaitItem() shouldBe listOf(BookMood(word = MoodWord.Preset(mood = Mood.Dark)))
            }
        }
    }

    @Nested
    inner class Save {
        @Test
        fun `replaces the set stored for the book and leaves other books alone`() = runTest {
            // ----- Arrange -----
            dataSource.rows.value = listOf(
                BookMoodEntity(
                    bookId = 1,
                    word = "sad",
                    isOwn = false,
                ),
                BookMoodEntity(
                    bookId = 2,
                    word = "dark",
                    isOwn = false,
                ),
            )

            // ----- Act -----
            repository.save(
                bookId = 1,
                moods = listOf(BookMood(word = MoodWord.Preset(mood = Mood.Cosy))),
            )

            // ----- Assert -----
            dataSource.rows.value shouldBe listOf(
                BookMoodEntity(
                    bookId = 2,
                    word = "dark",
                    isOwn = false,
                ),
                BookMoodEntity(
                    bookId = 1,
                    word = "cosy",
                    isOwn = false,
                ),
            )
        }

        @Test
        fun `own words are trimmed, blanks dropped and case-insensitive duplicates keep the first spelling`() = runTest {
            // ----- Arrange -----
            val moods = listOf(
                BookMood(word = MoodWord.Own(text = "  Wistful ")),
                BookMood(word = MoodWord.Own(text = "   ")),
                BookMood(word = MoodWord.Own(text = "wISTFUL")),
                BookMood(word = MoodWord.Own(text = "Giddy")),
            )

            // ----- Act -----
            repository.save(
                bookId = 1,
                moods = moods,
            )

            // ----- Assert -----
            dataSource.rows.value.map { it.word } shouldBe listOf("Wistful", "Giddy")
        }

        @Test
        fun `the same preset twice is stored once`() = runTest {
            // ----- Arrange -----
            val moods = listOf(
                BookMood(
                    word = MoodWord.Preset(mood = Mood.Sad),
                    strength = 10,
                ),
                BookMood(
                    word = MoodWord.Preset(mood = Mood.Sad),
                    strength = 90,
                ),
            )

            // ----- Act -----
            repository.save(
                bookId = 1,
                moods = moods,
            )

            // ----- Assert -----
            dataSource.rows.value shouldBe listOf(
                BookMoodEntity(
                    bookId = 1,
                    word = "sad",
                    isOwn = false,
                    strength = 10,
                ),
            )
        }

        @Test
        fun `an own word equal to a preset key is kept apart from the preset`() = runTest {
            // ----- Arrange -----
            val moods = listOf(
                BookMood(word = MoodWord.Preset(mood = Mood.Sad)),
                BookMood(word = MoodWord.Own(text = "Sad")),
            )

            // ----- Act -----
            repository.save(
                bookId = 1,
                moods = moods,
            )

            // ----- Assert -----
            dataSource.rows.value.map { it.isOwn } shouldBe listOf(false, true)
        }

        @Test
        fun `strength is clamped into zero to one hundred`() = runTest {
            // ----- Arrange -----
            val moods = listOf(
                BookMood(
                    word = MoodWord.Preset(mood = Mood.Sad),
                    strength = 400,
                ),
                BookMood(
                    word = MoodWord.Preset(mood = Mood.Dark),
                    strength = -3,
                ),
            )

            // ----- Act -----
            repository.save(
                bookId = 1,
                moods = moods,
            )

            // ----- Assert -----
            dataSource.rows.value.map { it.strength } shouldBe listOf(100, 0)
        }

        @Test
        fun `a preset outside the How it felt list is rejected and nothing is written`() = runTest {
            // ----- Arrange -----
            dataSource.rows.value = listOf(
                BookMoodEntity(
                    bookId = 1,
                    word = "sad",
                    isOwn = false,
                ),
            )

            val moods = listOf(
                BookMood(word = MoodWord.Preset(mood = Mood.Cosy)),
                BookMood(word = MoodWord.Preset(mood = Mood.Gripped)),
            )

            // ----- Act & Assert -----
            shouldThrow<IllegalArgumentException> {
                repository.save(
                    bookId = 1,
                    moods = moods,
                )
            }

            dataSource.rows.value shouldBe listOf(
                BookMoodEntity(
                    bookId = 1,
                    word = "sad",
                    isOwn = false,
                ),
            )
        }
    }
}
