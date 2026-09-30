package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookReviewUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDeleteReviewActionTest {
    private lateinit var updateBookReviewUseCase: UpdateBookReviewUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        updateBookReviewUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState(verdictSheetContext = VerdictSheetContext.EDIT))
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.updateBookReviewUseCase
            } returns updateBookReviewUseCase
        }
    }

    private fun stubBook(id: Int = 42): Book = mockk<Book>().also { mock ->
        every {
            mock.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `clears the review by updating it to an empty document without spoilers`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            coEvery {
                updateBookReviewUseCase(
                    book = book,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = false,
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnDeleteReviewAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookReviewUseCase(
                    book = book,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = false,
                )
            }
        }

        @Test
        fun `does not add book id to failedMutationBookIds when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)

            coEvery {
                updateBookReviewUseCase(
                    book = book,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = false,
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnDeleteReviewAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds shouldBe emptySet()
        }

        @Test
        fun `adds book id to failedMutationBookIds without losing prior entries when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = BookDetailUiState(failedMutationBookIds = setOf(1, 2))

            coEvery {
                updateBookReviewUseCase(
                    book = book,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = false,
                )
            } returns Result.failure(RuntimeException("api error"))

            // ----- Act -----
            OnDeleteReviewAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds shouldBe setOf(1, 2, 42)
        }

        @Test
        fun `leaves verdictSheetContext untouched on success`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            coEvery {
                updateBookReviewUseCase(
                    book = book,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = false,
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnDeleteReviewAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheetContext shouldBe VerdictSheetContext.EDIT
        }

        @Test
        fun `leaves verdictSheetContext untouched on failure`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            coEvery {
                updateBookReviewUseCase(
                    book = book,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = false,
                )
            } returns Result.failure(RuntimeException("api error"))

            // ----- Act -----
            OnDeleteReviewAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheetContext shouldBe VerdictSheetContext.EDIT
        }
    }
}
