package nl.rhaydus.softcover.core.corpus.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.corpus.domain.repository.BookMoodsRepository
import nl.rhaydus.softcover.core.corpus.domain.repository.MoodMomentsRepository
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveOwnMoodWordsUseCaseTest {
    private class FakeBookMoods(private val all: Map<Int, List<BookMood>>) : BookMoodsRepository {
        override fun observeAll(): Flow<Map<Int, List<BookMood>>> = flowOf(all)

        override fun observe(bookId: Int): Flow<List<BookMood>> = flowOf(all[bookId].orEmpty())

        override suspend fun save(
            bookId: Int,
            moods: List<BookMood>,
        ) = Unit
    }

    private class FakeMoments(private val all: Map<Int, List<MoodMoment>>) : MoodMomentsRepository {
        override fun observeAll(): Flow<Map<Int, List<MoodMoment>>> = flowOf(all)

        override fun observe(bookId: Int): Flow<List<MoodMoment>> = flowOf(all[bookId].orEmpty())

        override suspend fun add(
            bookId: Int,
            word: MoodWord,
            strength: Int?,
            percent: Int?,
            page: Int?,
            seconds: Int?,
        ) = Unit

        override suspend fun update(moment: MoodMoment) = Unit

        override suspend fun delete(id: Long) = Unit
    }

    private fun moment(word: MoodWord) = MoodMoment(
        id = 1,
        bookId = 1,
        word = word,
        strength = null,
        percent = null,
        page = null,
        seconds = null,
        recordedAt = 0L,
    )

    private fun useCase(
        bookMoods: Map<Int, List<BookMood>> = emptyMap(),
        moments: Map<Int, List<MoodMoment>> = emptyMap(),
    ) = ObserveOwnMoodWordsUseCase(
        bookMoodsRepository = FakeBookMoods(all = bookMoods),
        moodMomentsRepository = FakeMoments(all = moments),
    )

    @Nested
    inner class Invoke {
        @Test
        fun `collects own words from book moods and moments and ignores presets`() = runTest {
            // ----- Arrange -----
            val useCase = useCase(
                bookMoods = mapOf(
                    1 to listOf(
                        BookMood(word = MoodWord.Preset(mood = Mood.Sad)),
                        BookMood(word = MoodWord.Own(text = "Wistful")),
                    ),
                ),
                moments = mapOf(2 to listOf(moment(word = MoodWord.Own(text = "Giddy")))),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe listOf("Wistful", "Giddy")
                awaitComplete()
            }
        }

        @Test
        fun `case-insensitive duplicates across sources keep the first spelling`() = runTest {
            // ----- Arrange -----
            val useCase = useCase(
                bookMoods = mapOf(1 to listOf(BookMood(word = MoodWord.Own(text = "Wistful")))),
                moments = mapOf(
                    1 to listOf(
                        moment(word = MoodWord.Own(text = "WISTFUL")),
                        moment(word = MoodWord.Own(text = "wistful ")),
                    ),
                ),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe listOf("Wistful")
                awaitComplete()
            }
        }

        @Test
        fun `blank own words are left out`() = runTest {
            // ----- Arrange -----
            val useCase = useCase(
                bookMoods = mapOf(1 to listOf(BookMood(word = MoodWord.Own(text = "  ")))),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }
}
