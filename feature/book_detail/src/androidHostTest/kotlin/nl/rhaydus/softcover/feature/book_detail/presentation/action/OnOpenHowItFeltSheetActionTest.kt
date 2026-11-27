package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveBookMoodsUseCase
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItFeltDraft
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnOpenHowItFeltSheetActionTest {
    private lateinit var observeBookMoodsUseCase: ObserveBookMoodsUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        observeBookMoodsUseCase = mockk()

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeBookMoodsUseCase
            } returns observeBookMoodsUseCase
        }

        stateFlow = MutableStateFlow(buildBookDetailUiState(bookId = 42))

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `seeds the draft from the book's stored moods`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(book = buildQuoteBook())

            val stored = listOf(
                BookMood(
                    word = MoodWord.Preset(Mood.Cosy),
                    strength = 30,
                ),
                BookMood(word = MoodWord.Own("Wistful")),
            )

            every {
                observeBookMoodsUseCase(bookId = 42)
            } returns flowOf(stored)

            // ----- Act -----
            OnOpenHowItFeltSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.howItFeltDraft shouldBe HowItFeltDraft(moods = persistentListOf(*stored.toTypedArray()))
        }

        @Test
        fun `seeds no draft while the book has not loaded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42)

            // ----- Act -----
            OnOpenHowItFeltSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.howItFeltDraft.shouldBeNull()
        }
    }
}
