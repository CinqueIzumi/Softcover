package nl.rhaydus.softcover.feature.book_detail.presentation.action

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.SaveBookVerdictUseCase
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.uibinding.richtext.toReviewDocument
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.core.uibinding.verdict.draftReview
import nl.rhaydus.softcover.core.uibinding.verdict.reduce
import nl.rhaydus.softcover.core.uibinding.verdict.toVerdictSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveVerdictActionTest {
    private lateinit var saveBookVerdictUseCase: SaveBookVerdictUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        saveBookVerdictUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState().copy(verdictSheetContext = VerdictSheetContext.EDIT))
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = eventChannel,
        )
    }

    private fun stubDependencies(): BookDetailDependencies =
        mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.saveBookVerdictUseCase
            } returns saveBookVerdictUseCase
        }

    private fun stubBook(id: Int = 42): Book = mockk<Book>().also { mock ->
        every {
            mock.id
        } returns id

        every {
            mock.title
        } returns "Piranesi"

        every {
            mock.userBook
        } returns null
    }

    private fun draftSheet(
        book: Book,
        rating: Double?,
        reviewText: String,
        hasSpoilers: Boolean,
    ): VerdictSheetUiModel = book
        .toVerdictSheetUiModel(
            context = VerdictSheetContext.EDIT,
            review = null,
            cover = null,
        )
        .reduce(VerdictDraftEvent.RatingChanged(rating = rating))
        .reduce(
            VerdictDraftEvent.ReviewEdited(
                value = TextFieldValue(
                    text = reviewText,
                    selection = TextRange(reviewText.length),
                ),
            ),
        )
        .reduce(VerdictDraftEvent.SpoilersToggled(checked = hasSpoilers))

    @Nested
    inner class Execute {
        @Test
        fun `saves the book with the rating, review and spoiler flag held by the open sheet draft`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val sheet = draftSheet(
                book = book,
                rating = 3.5,
                reviewText = "A great read",
                hasSpoilers = true,
            )
            stateFlow.value = stateFlow.value.copy(verdictSheet = sheet)
            dependencies = stubDependencies()

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 3.5,
                    review = sheet.draftReview().toReviewDocument(),
                    hasSpoilers = true,
                )
            } returns Result.success(Unit)

            val action = OnSaveVerdictAction(book = book)

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
                    review = sheet.draftReview().toReviewDocument(),
                    hasSpoilers = true,
                )
            }
        }

        @Test
        fun `saves an empty review document when the sheet draft review is blank`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val sheet = draftSheet(
                book = book,
                rating = 4.0,
                reviewText = "   ",
                hasSpoilers = false,
            )
            stateFlow.value = stateFlow.value.copy(verdictSheet = sheet)
            dependencies = stubDependencies()

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 4.0,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = false,
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnSaveVerdictAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 4.0,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = false,
                )
            }
        }

        @Test
        fun `does nothing when no verdict sheet is open`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies()
            val action = OnSaveVerdictAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                saveBookVerdictUseCase(
                    any(),
                    any(),
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `does not add book id to failedMutationBookIds when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val sheet = draftSheet(
                book = book,
                rating = null,
                reviewText = "A great read",
                hasSpoilers = false,
            )
            stateFlow.value = stateFlow.value.copy(verdictSheet = sheet)
            dependencies = stubDependencies()

            coEvery {
                saveBookVerdictUseCase(
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns Result.success(Unit)

            val action = OnSaveVerdictAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe false
            val sentEvent = eventChannel.tryReceive().getOrNull()
            sentEvent.shouldBeInstanceOf<BookDetailEvent.MutationSucceededEvent>()
        }

        @Test
        fun `adds failing book id to an existing failedMutationBookIds set without losing prior entries`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            val sheet = draftSheet(
                book = book,
                rating = null,
                reviewText = "A great read",
                hasSpoilers = false,
            )
            stateFlow.value = buildBookDetailUiState().copy(
                failedMutationBookIds = persistentSetOf(1, 2),
                verdictSheet = sheet,
            )
            dependencies = stubDependencies()

            coEvery {
                saveBookVerdictUseCase(
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns Result.failure(RuntimeException("api error"))

            val action = OnSaveVerdictAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds shouldBe setOf(1, 2, 42)
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
