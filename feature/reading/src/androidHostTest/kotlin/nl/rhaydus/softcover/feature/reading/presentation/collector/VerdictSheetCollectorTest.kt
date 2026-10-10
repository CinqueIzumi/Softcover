package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
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

class VerdictSheetCollectorTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<ReadingScreenDependencies>(relaxed = true)
    }

    private fun cover(): CoverUiModel = CoverUiModel(
        source = null,
        coverlessTitle = PreviewData.baseBook.title,
        variant = CoverVariant.Tile,
    )

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            VerdictSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    private fun bookReviewing(reviewDocument: ReviewDocument?): Book {
        val userBook = mockk<UserBook> {
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

        return mockk {
            every {
                this@mockk.title
            } returns "Piranesi"

            every {
                this@mockk.userBook
            } returns userBook
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `builds the FINISHED model when the prompt book is set even without a cover`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = book)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.FINISHED,
                review = null,
                cover = null,
            )
        }

        @Test
        fun `updates the model when the cover arrives`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val cover = cover()
            launchCollector()
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = book)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictCover = cover)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.FINISHED,
                review = null,
                cover = cover,
            )
        }

        @Test
        fun `clears the model when the prompt book is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            launchCollector()
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = PreviewData.baseBook)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = null)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe null
        }

        @Test
        fun `carries the book's review into the model`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val reviewDocument = ReviewDocument(
                paragraphs = listOf(
                    ReviewParagraph(
                        runs = listOf(
                            ReviewRun(text = "Loved it."),
                        ),
                    ),
                ),
            )
            val book = bookReviewing(reviewDocument = reviewDocument)
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = book)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.FINISHED,
                review = reviewDocument.toRichTextUiModel(),
                cover = null,
            )
        }

        @Test
        fun `keeps the edited draft when the cover arrives and rebuilds the model`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val cover = cover()
            launchCollector()
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = book)
            val edited = stateFlow.value.verdictSheet.shouldNotBeNull()
                .reduce(event = VerdictDraftEvent.RatingChanged(rating = 4.0))
                .reduce(event = VerdictDraftEvent.SpoilersToggled(checked = true))
            stateFlow.value = stateFlow.value.copy(verdictSheet = edited)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictCover = cover)

            // ----- Assert -----
            val sheet = stateFlow.value.verdictSheet.shouldNotBeNull()
            sheet.rating shouldBe 4.0
            sheet.spoilers.checked shouldBe true
            sheet.scaffold shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.FINISHED,
                review = null,
                cover = cover,
            ).scaffold
        }

        @Test
        fun `does not carry a draft over to a new prompt after the sheet was cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            launchCollector()
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = book)
            stateFlow.value = stateFlow.value.copy(
                verdictSheet = stateFlow.value.verdictSheet.shouldNotBeNull()
                    .reduce(event = VerdictDraftEvent.RatingChanged(rating = 4.0)),
            )
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = null)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = book)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.FINISHED,
                review = null,
                cover = null,
            )
        }
    }
}
