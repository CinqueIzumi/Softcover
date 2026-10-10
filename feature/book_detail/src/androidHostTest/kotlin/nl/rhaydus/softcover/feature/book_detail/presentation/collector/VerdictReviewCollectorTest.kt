package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.richtext.RichTextVariant
import nl.rhaydus.softcover.core.component.verdict.VerdictBlockUiModel
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            VerdictReviewCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    private fun stubBook(userBook: UserBook?): Book = mockk {
        every {
            this@mockk.userBook
        } returns userBook
    }

    private fun stubUserBook(
        reviewDocument: ReviewDocument?,
        rating: Double? = null,
        reviewHasSpoilers: Boolean = false,
    ): UserBook = mockk {
        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.reviewHasSpoilers
        } returns reviewHasSpoilers

        every {
            this@mockk.reviewDocument
        } returns reviewDocument
    }

    private fun buildReviewDocument(text: String) = ReviewDocument(
        paragraphs = listOf(
            ReviewParagraph(
                runs = listOf(ReviewRun(text = text)),
            ),
        ),
    )

    @Nested
    inner class Empty {
        @Test
        fun `is Empty when the user book has neither a rating nor a review`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(userBook = stubUserBook(reviewDocument = null))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictBlock shouldBe VerdictBlockUiModel.Empty
        }

        @Test
        fun `is Empty when the rating is zero and the review is blank`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                userBook = stubUserBook(
                    reviewDocument = buildReviewDocument(text = "   "),
                    rating = 0.0,
                ),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictBlock shouldBe VerdictBlockUiModel.Empty
        }
    }

    @Nested
    inner class Filled {
        @Test
        fun `carries the rating, review and spoiler flag`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val reviewDocument = buildReviewDocument(text = "A gorgeous read.")
            val book = stubBook(
                userBook = stubUserBook(
                    reviewDocument = reviewDocument,
                    rating = 4.5,
                    reviewHasSpoilers = true,
                ),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictBlock shouldBe VerdictBlockUiModel.Filled(
                rating = 4.5,
                review = reviewDocument.toRichTextUiModel(variant = RichTextVariant.Verdict),
                hasSpoilers = true,
            )
        }

        @Test
        fun `has no review when only a rating is given`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                userBook = stubUserBook(
                    reviewDocument = null,
                    rating = 3.0,
                ),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictBlock shouldBe VerdictBlockUiModel.Filled(
                rating = 3.0,
                review = null,
                hasSpoilers = false,
            )
        }

        @Test
        fun `has no rating when only a review is given`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val reviewDocument = buildReviewDocument(text = "Words only.")
            val book = stubBook(userBook = stubUserBook(reviewDocument = reviewDocument))
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictBlock shouldBe VerdictBlockUiModel.Filled(
                rating = null,
                review = reviewDocument.toRichTextUiModel(variant = RichTextVariant.Verdict),
                hasSpoilers = false,
            )
        }

        @Test
        fun `treats a zero rating as unrated when a review is present`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val reviewDocument = buildReviewDocument(text = "Unrated but reviewed.")
            val book = stubBook(
                userBook = stubUserBook(
                    reviewDocument = reviewDocument,
                    rating = 0.0,
                ),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictBlock shouldBe VerdictBlockUiModel.Filled(
                rating = null,
                review = reviewDocument.toRichTextUiModel(variant = RichTextVariant.Verdict),
                hasSpoilers = false,
            )
        }

        @Test
        fun `drops a blank review and keeps the rating`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(
                userBook = stubUserBook(
                    reviewDocument = buildReviewDocument(text = " "),
                    rating = 2.5,
                ),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = book)

            // ----- Assert -----
            stateFlow.value.verdictBlock shouldBe VerdictBlockUiModel.Filled(
                rating = 2.5,
                review = null,
                hasSpoilers = false,
            )
        }
    }

    @Nested
    inner class NoUserData {
        @Test
        fun `is null when the book has no user book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = stubBook(userBook = null))

            // ----- Assert -----
            stateFlow.value.verdictBlock.shouldBeNull()
        }

        @Test
        fun `clears when the book is removed`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(
                    userBook = stubUserBook(
                        reviewDocument = null,
                        rating = 3.0,
                    ),
                ),
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.verdictBlock.shouldBeNull()
        }
    }
}
