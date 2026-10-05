package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
import nl.rhaydus.softcover.core.uibinding.verdict.toVerdictSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VerdictSheetCollectorTest {
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
            VerdictSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    private fun stubBook(
        title: String = "Piranesi",
        reviewDocument: ReviewDocument? = null,
        hasUserBook: Boolean = reviewDocument != null,
    ): Book {
        val stubbedUserBook = if (hasUserBook) {
            mockk<UserBook> {
                every {
                    this@mockk.reviewDocument
                } returns reviewDocument

                every {
                    this@mockk.rating
                } returns null

                every {
                    this@mockk.reviewHasSpoilers
                } returns false
            }
        } else {
            null
        }

        return mockk {
            every {
                this@mockk.title
            } returns title

            every {
                this@mockk.userBook
            } returns stubbedUserBook
        }
    }

    private fun buildReviewDocument(text: String) = ReviewDocument(
        paragraphs = listOf(
            ReviewParagraph(
                runs = listOf(ReviewRun(text = text)),
            ),
        ),
    )

    private fun buildCover() = CoverUiModel(
        source = null,
        coverlessTitle = "Piranesi",
        variant = CoverVariant.Jacket,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `verdictSheet is null when the context is set but the book is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = null,
                verdictSheetContext = VerdictSheetContext.FINISHED,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe null
        }

        @Test
        fun `verdictSheet is built from the book context review and cover`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val reviewDocument = buildReviewDocument(text = "A gorgeous read.")
            val book = stubBook(reviewDocument = reviewDocument)
            val cover = buildCover()
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                verdictCover = cover,
                verdictSheetContext = VerdictSheetContext.EDIT,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.EDIT,
                review = reviewDocument.toRichTextUiModel(),
                cover = cover,
            )
        }

        @Test
        fun `verdictSheet closes when the context is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(),
                verdictSheetContext = VerdictSheetContext.FINISHED,
            )
            stateFlow.value.verdictSheet shouldNotBe null

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictSheetContext = null)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe null
        }

        @Test
        fun `verdictSheet picks up a cover that resolves after the sheet opened`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook()
            val cover = buildCover()
            launchCollector()
            stateFlow.value = stateFlow.value.copy(
                book = book,
                verdictSheetContext = VerdictSheetContext.FINISHED,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictCover = cover)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.FINISHED,
                review = null,
                cover = cover,
            )
        }
    }
}
