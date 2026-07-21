package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.usecase.SaveBookVerdictUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.model.VerdictSheetContext
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class OnSaveVerdictActionTest {
    private lateinit var saveBookVerdictUseCase: SaveBookVerdictUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        saveBookVerdictUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState(verdictSheetContext = VerdictSheetContext.EDIT))
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(): BookDetailDependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
        every {
            mock.saveBookVerdictUseCase
        } returns saveBookVerdictUseCase
    }

    private fun stubBook(id: Int = 42): Book = mockk<Book>().also { mock ->
        every {
            mock.id
        } returns id
    }

    private fun stubReview(): ReviewDocument = ReviewDocument(
        paragraphs = listOf(ReviewParagraph(runs = listOf(ReviewRun(text = "A great read")))),
    )

    @Nested
    inner class Execute {
        @Test
        fun `sets verdictSheetContext to null before the use case resolves`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val review = stubReview()
            dependencies = stubDependencies()

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 4.0,
                    review = review,
                    hasSpoilers = false,
                )
            } returns Result.success(Unit)

            val action = OnSaveVerdictAction(
                book = book,
                rating = 4.0,
                review = review,
                hasSpoilers = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheetContext shouldBe null
        }

        @Test
        fun `invokes use case with the book, rating, review and hasSpoilers provided to the action constructor`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val review = stubReview()
            dependencies = stubDependencies()

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 3.5,
                    review = review,
                    hasSpoilers = true,
                )
            } returns Result.success(Unit)

            val action = OnSaveVerdictAction(
                book = book,
                rating = 3.5,
                review = review,
                hasSpoilers = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 3.5,
                    review = review,
                    hasSpoilers = true,
                )
            }
        }

        @Test
        fun `does not add book id to failedMutationBookIds when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val review = stubReview()
            dependencies = stubDependencies()

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = null,
                    review = review,
                    hasSpoilers = false,
                )
            } returns Result.success(Unit)

            val action = OnSaveVerdictAction(
                book = book,
                rating = null,
                review = review,
                hasSpoilers = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe false
        }

        @Test
        fun `adds book id to failedMutationBookIds when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val review = stubReview()
            dependencies = stubDependencies()

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = null,
                    review = review,
                    hasSpoilers = false,
                )
            } returns Result.failure(RuntimeException("api error"))

            val action = OnSaveVerdictAction(
                book = book,
                rating = null,
                review = review,
                hasSpoilers = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
        }

        @Test
        fun `adds failing book id to an existing failedMutationBookIds set without losing prior entries`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val review = stubReview()
            stateFlow.value = BookDetailUiState(failedMutationBookIds = setOf(1, 2))
            dependencies = stubDependencies()

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = null,
                    review = review,
                    hasSpoilers = false,
                )
            } returns Result.failure(RuntimeException("api error"))

            val action = OnSaveVerdictAction(
                book = book,
                rating = null,
                review = review,
                hasSpoilers = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds shouldBe setOf(1, 2, 42)
        }
    }
}
