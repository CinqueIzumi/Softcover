package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.domain.model.Book
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

    private fun stubBook(title: String = "Piranesi"): Book = mockk {
        every {
            this@mockk.title
        } returns title

        every {
            this@mockk.userBook
        } returns null
    }

    private fun buildCover() = CoverUiModel(
        source = null,
        coverlessTitle = "Piranesi",
        variant = CoverVariant.SheetHeaderJacket,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `verdictSheet is null when the context is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VerdictSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(),
                verdictSheetContext = null,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe null
            job.cancel()
        }

        @Test
        fun `verdictSheet is null when the context is set but the book is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VerdictSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = null,
                verdictSheetContext = VerdictSheetContext.FINISHED,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe null
            job.cancel()
        }

        @Test
        fun `verdictSheet is built from the book context review and cover`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook()
            val cover = buildCover()
            val review = RichTextUiModel.previews.first()
            val collector = VerdictSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                verdictReview = review,
                verdictCover = cover,
                verdictSheetContext = VerdictSheetContext.EDIT,
            )

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.EDIT,
                review = review,
                cover = cover,
            )
            job.cancel()
        }

        @Test
        fun `verdictSheet closes when the context is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VerdictSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(
                book = stubBook(),
                verdictSheetContext = VerdictSheetContext.FINISHED,
            )
            stateFlow.value.verdictSheet shouldNotBe null

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictSheetContext = null)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe null
            job.cancel()
        }

        @Test
        fun `verdictSheet follows the context when it switches`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook()
            val collector = VerdictSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(
                book = book,
                verdictSheetContext = VerdictSheetContext.FINISHED,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictSheetContext = VerdictSheetContext.EDIT)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.EDIT,
                review = null,
                cover = null,
            )
            job.cancel()
        }

        @Test
        fun `verdictSheet picks up a cover that resolves after the sheet opened`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook()
            val cover = buildCover()
            val collector = VerdictSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
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
            job.cancel()
        }
    }
}
