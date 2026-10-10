package nl.rhaydus.softcover.feature.reading.presentation.action

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.SaveBookVerdictUseCase
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.core.uibinding.verdict.reduce
import nl.rhaydus.softcover.core.uibinding.verdict.toVerdictSheetUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveVerdictActionTest {
    private lateinit var saveBookVerdictUseCase: SaveBookVerdictUseCase
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        saveBookVerdictUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.saveBookVerdictUseCase
            } returns saveBookVerdictUseCase
        }
    }

    private fun stubBook(id: Int = 42): Book = mockk<Book>().also { mock ->
        every {
            mock.id
        } returns id
    }

    private val blankDraftReview = ReviewDocument.EMPTY

    private fun sheet() = PreviewData.baseBook.toVerdictSheetUiModel(
        context = VerdictSheetContext.FINISHED,
        review = null,
        cover = null,
    )

    @Nested
    inner class Execute {
        @Test
        fun `saves the draft rating and spoilers with an empty review and flags no failure on success`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val draft = sheet()
                .reduce(event = VerdictDraftEvent.RatingChanged(rating = 3.0))
                .reduce(event = VerdictDraftEvent.SpoilersToggled(checked = true))
            stateFlow.value = ReadingScreenUiState(verdictSheet = draft)

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 3.0,
                    review = blankDraftReview,
                    hasSpoilers = true,
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
                    rating = 3.0,
                    review = blankDraftReview,
                    hasSpoilers = true,
                )
            }
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe false
        }

        @Test
        fun `saves the review text typed into the draft`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            val draft = sheet().reduce(
                event = VerdictDraftEvent.ReviewEdited(
                    value = TextFieldValue(
                        text = "Loved it.",
                        selection = TextRange(9),
                    ),
                ),
            )
            stateFlow.value = ReadingScreenUiState(verdictSheet = draft)
            val savedReview = slot<ReviewDocument>()

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = any(),
                    review = capture(savedReview),
                    hasSpoilers = any(),
                )
            } returns Result.success(Unit)

            // ----- Act -----
            OnSaveVerdictAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            savedReview.captured.paragraphs.single().runs.joinToString("") { it.text } shouldBe "Loved it."
        }

        @Test
        fun `saves a null rating when the draft has none`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = ReadingScreenUiState(
                verdictSheet = sheet().reduce(event = VerdictDraftEvent.RatingChanged(rating = null)),
            )

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = null,
                    review = blankDraftReview,
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
                    rating = null,
                    review = blankDraftReview,
                    hasSpoilers = false,
                )
            }
        }

        @Test
        fun `flags the book as failed when the use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            stateFlow.value = ReadingScreenUiState(
                verdictSheet = sheet().reduce(event = VerdictDraftEvent.RatingChanged(rating = 4.5)),
            )

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 4.5,
                    review = blankDraftReview,
                    hasSpoilers = false,
                )
            } returns Result.failure(RuntimeException("api error"))

            // ----- Act -----
            OnSaveVerdictAction(book = book).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
        }

        @Test
        fun `does nothing when no verdict sheet is open`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            stateFlow.value = ReadingScreenUiState(verdictSheet = null)

            // ----- Act -----
            OnSaveVerdictAction(book = book).execute(
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
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe false
        }
    }
}
