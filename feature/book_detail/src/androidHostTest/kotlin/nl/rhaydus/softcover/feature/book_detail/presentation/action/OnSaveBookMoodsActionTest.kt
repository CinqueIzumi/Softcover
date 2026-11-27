package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.corpus.domain.usecase.SaveBookMoodsUseCase
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItFeltDraft
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveBookMoodsActionTest {
    private lateinit var saveBookMoodsUseCase: SaveBookMoodsUseCase
    private val saved = mutableListOf<Pair<Int, List<BookMood>>>()
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        saved.clear()
        saveBookMoodsUseCase = mockk()

        coEvery {
            saveBookMoodsUseCase(
                bookId = any(),
                moods = any(),
            )
        } answers {
            saved += firstArg<Int>() to secondArg<List<BookMood>>()
            Result.success(Unit)
        }

        stateFlow = MutableStateFlow(buildBookDetailUiState(bookId = 42))
        eventChannel = Channel(Channel.BUFFERED)

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = eventChannel,
        )

        mockkObject(SnackBarManager)

        every {
            SnackBarManager.showSnackbar(title = any())
        } returns Unit
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.saveBookMoodsUseCase
            } returns saveBookMoodsUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `saves the chosen moods with a typed own word folded in and closes the sheet`() = runTest {
            // ----- Arrange -----
            val tense = BookMood(
                word = MoodWord.Preset(Mood.Tense),
                strength = 80,
            )

            stateFlow.value = stateFlow.value.copy(
                howItFeltDraft = HowItFeltDraft(
                    moods = persistentListOf(tense),
                    ownWordText = " Wistful ",
                ),
            )

            // ----- Act -----
            OnSaveBookMoodsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            saved shouldBe listOf(42 to listOf(tense, BookMood(word = MoodWord.Own("Wistful"))))
            stateFlow.value.howItFeltDraft.shouldBeNull()
        }

        @Test
        fun `shows a message and sends MutationFailedEvent when the save fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                saveBookMoodsUseCase(
                    bookId = any(),
                    moods = any(),
                )
            } returns Result.failure(RuntimeException("save failed"))

            stateFlow.value = stateFlow.value.copy(howItFeltDraft = HowItFeltDraft())

            // ----- Act -----
            OnSaveBookMoodsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't save how it felt. Try again.")
            }

            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
        }

        @Test
        fun `does nothing when there is no draft`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42)

            // ----- Act -----
            OnSaveBookMoodsAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            saved.shouldBeEmpty()
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
