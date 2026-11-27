package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.corpus.domain.usecase.GetDidNotFinishReasonsUseCase
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildDidNotFinishReasonDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnOpenDidNotFinishReasonSheetActionTest {
    private lateinit var getDidNotFinishReasonsUseCase: GetDidNotFinishReasonsUseCase
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        getDidNotFinishReasonsUseCase = mockk()

        stateFlow = MutableStateFlow(buildBookDetailUiState(bookId = 42))

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
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
                mock.getDidNotFinishReasonsUseCase
            } returns getDidNotFinishReasonsUseCase

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
        fun `seeds the draft from the stored reasons and own words`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(book = buildQuoteBook())

            coEvery {
                getDidNotFinishReasonsUseCase(bookId = 42)
            } returns Result.success(
                DidNotFinishReasons(
                    bookId = 42,
                    reasons = setOf(DidNotFinishReason.TooSlow, DidNotFinishReason.LostInterest),
                    ownWords = "The middle dragged",
                ),
            )

            // ----- Act -----
            OnOpenDidNotFinishReasonSheetAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonDraft shouldBe buildDidNotFinishReasonDraft(
                reasons = setOf(DidNotFinishReason.TooSlow, DidNotFinishReason.LostInterest),
                ownWords = "The middle dragged",
            )
        }

        @Test
        fun `leaves no draft and shows a message when the reasons cannot be read`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(book = buildQuoteBook())

            coEvery {
                getDidNotFinishReasonsUseCase(bookId = 42)
            } returns Result.failure(RuntimeException("read failed"))

            // ----- Act -----
            OnOpenDidNotFinishReasonSheetAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonDraft.shouldBeNull()

            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't load your reasons. Try again.")
            }
        }

        @Test
        fun `seeds no draft while the book has not loaded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 42)

            // ----- Act -----
            OnOpenDidNotFinishReasonSheetAction().execute(
                dependencies = stubDependencies(this),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.didNotFinishReasonDraft.shouldBeNull()
        }
    }
}
