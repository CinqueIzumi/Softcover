package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressSheetCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    private val pastReadAt = LocalDateTime(
        2020,
        1,
        2,
        9,
        15,
    )

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet on the mapped model once it is requested with a book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                progressSheetRequested = true,
            )

            // ----- Assert -----
            val expected = book.toProgressSheetUiModel(selectedTab = stateFlow.value.selectedProgressSheetTab)
            val sheet = stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull()
            sheet.copy(whenRead = expected.whenRead) shouldBe expected
            stateFlow.value.progressSheet.isClosing shouldBe false
            job.cancel()
        }

        @Test
        fun `a book alone does not open the sheet`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = PreviewData.baseBook)

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `a request without a book does not open the sheet`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(progressSheetRequested = true)

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe SheetSession.Closed
            job.cancel()
        }

        @Test
        fun `clearing the request requests close on the open session`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                book = PreviewData.baseBook,
                progressSheetRequested = true,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(progressSheetRequested = false)

            // ----- Assert -----
            stateFlow.value.progressSheet.isClosing shouldBe true
            stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull()
            job.cancel()
        }

        @Test
        fun `book going null while requested requests close on the open session`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                book = PreviewData.baseBook,
                progressSheetRequested = true,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = null)

            // ----- Assert -----
            stateFlow.value.progressSheet.isClosing shouldBe true
            job.cancel()
        }

        @Test
        fun `a request reopens the sheet with a fresh model while a close is in flight`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val closingModel = ProgressSheetUiModel.previews.values.first().requestingClose()
            stateFlow.value = buildBookDetailUiState().copy(progressSheet = SheetSession.Open(model = closingModel))
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                book = book,
                progressSheetRequested = true,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.isClosing shouldBe false
            val expected = book.toProgressSheetUiModel(selectedTab = stateFlow.value.selectedProgressSheetTab)
            val sheet = stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull()
            sheet.copy(whenRead = expected.whenRead) shouldBe expected
            job.cancel()
        }

        @Test
        fun `a model update keeps the open sheet open`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                book = PreviewData.baseBook,
                progressSheetRequested = true,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedProgressSheetTab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            stateFlow.value.progressSheet.isClosing shouldBe false
            job.cancel()
        }

        @Test
        fun `a tab change alone re-emits the model with selectedTab matching the stored tab`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                book = book,
                progressSheetRequested = true,
            )
            stateFlow.value.progressSheet.modelOrNull?.tabs?.selected shouldBe ProgressSheetTab.PAGE

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedProgressSheetTab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            stateFlow.value.progressSheet.modelOrNull?.tabs?.selected shouldBe stateFlow.value.selectedProgressSheetTab
            job.cancel()
        }

        @Test
        fun `a time draft edit alone re-emits the model with the drafted text`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val audioEdition = PreviewData.baseEdition.copy(
                readingFormat = ReadingFormat.Audio,
                audioSeconds = 7200,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(audioEdition),
                defaultEdition = audioEdition,
                userBookRead = null,
            )
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                book = book,
                progressSheetRequested = true,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(
                progressTimeDraft = ProgressTimeDraft(
                    forSeconds = 0,
                    hours = "4",
                    minutes = "30",
                    seconds = "15",
                ),
            )

            // ----- Assert -----
            val medium = stateFlow.value.progressSheet.modelOrNull?.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.hours.text shouldBe "4"
            medium.minutes.text shouldBe "30"
            medium.seconds.text shouldBe "15"
            job.cancel()
        }

        @Test
        fun `clearing the time draft reseeds the fields from the book position`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val audioEdition = PreviewData.baseEdition.copy(
                readingFormat = ReadingFormat.Audio,
                audioSeconds = 7200,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(audioEdition),
                defaultEdition = audioEdition,
                userBookRead = null,
            )
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                book = book,
                progressSheetRequested = true,
                progressTimeDraft = ProgressTimeDraft(
                    forSeconds = 0,
                    hours = "4",
                    minutes = "30",
                    seconds = "15",
                ),
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(progressTimeDraft = null)

            // ----- Assert -----
            val medium = stateFlow.value.progressSheet.modelOrNull?.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.hours.text shouldBe "0"
            medium.minutes.text shouldBe "0"
            medium.seconds.text shouldBe "0"
            job.cancel()
        }

        @Test
        fun `a backdate shows the picked time on the whenRead row and selects its chip`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                book = PreviewData.baseBook,
                progressSheetRequested = true,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readAt = pastReadAt)

            // ----- Assert -----
            val whenRead = stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull().whenRead
            whenRead.timeLabel shouldBe "09:15"
            whenRead.chip.selected shouldBe true
            whenRead.chip.label shouldEndWith ", 09:15"
            job.cancel()
        }

        @Test
        fun `resetting the backdate returns the whenRead row to just now`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                book = PreviewData.baseBook,
                progressSheetRequested = true,
                readAt = pastReadAt,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readAt = null)

            // ----- Assert -----
            val whenRead = stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull().whenRead
            whenRead.chip.selected shouldBe false
            whenRead.chip.label shouldBe "Just now"
            job.cancel()
        }
    }
}
