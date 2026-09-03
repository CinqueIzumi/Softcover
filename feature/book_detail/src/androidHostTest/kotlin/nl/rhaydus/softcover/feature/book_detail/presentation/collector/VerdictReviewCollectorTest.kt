package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class VerdictReviewCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    private fun stubBook(userBook: UserBook?): Book = mockk {
        every {
            this@mockk.userBook
        } returns userBook
    }

    private fun stubUserBook(reviewDocument: ReviewDocument?): UserBook = mockk {
        every {
            this@mockk.reviewDocument
        } returns reviewDocument
    }

    private fun buildReviewDocument(text: String) = ReviewDocument(
        paragraphs = listOf(ReviewParagraph(
            runs = listOf(ReviewRun(text = text)),
        ),),
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `sets verdictReview to the mapped rich text when the book has a review document`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val reviewDocument = buildReviewDocument(text = "A gorgeous read.")
            val userBook = stubUserBook(reviewDocument = reviewDocument)
            val book = stubBook(userBook = userBook)

            val collector = VerdictReviewCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictReview shouldBe reviewDocument.toRichTextUiModel()
            job.cancel()
        }

        @Test
        fun `verdictReview is null when the book has no review document`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val userBook = stubUserBook(reviewDocument = null)
            val book = stubBook(userBook = userBook)

            val collector = VerdictReviewCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictReview shouldBe null
            job.cancel()
        }

        @Test
        fun `verdictReview is null when the book has no userBook`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(userBook = null)

            val collector = VerdictReviewCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictReview shouldBe null
            job.cancel()
        }

        @Test
        fun `verdictReview is null when book is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VerdictReviewCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.verdictReview shouldBe null
            job.cancel()
        }

        @Test
        fun `verdictReview updates when the review document changes on a later book emission`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstDocument = buildReviewDocument(text = "First impression.")
            val secondDocument = buildReviewDocument(text = "Revised after a reread.")
            val bookA = stubBook(userBook = stubUserBook(reviewDocument = firstDocument))
            val bookB = stubBook(userBook = stubUserBook(reviewDocument = secondDocument))

            val collector = VerdictReviewCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(book = bookA)
            stateFlow.value.verdictReview shouldBe firstDocument.toRichTextUiModel()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = bookB)

            // ----- Assert -----
            stateFlow.value.verdictReview shouldBe secondDocument.toRichTextUiModel()
            job.cancel()
        }
    }
}
