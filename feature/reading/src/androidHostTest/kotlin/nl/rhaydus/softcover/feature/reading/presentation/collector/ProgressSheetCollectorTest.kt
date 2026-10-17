package nl.rhaydus.softcover.feature.reading.presentation.collector

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
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressSheetCollectorTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    private val pastReadAt = LocalDateTime(
        2020,
        1,
        2,
        9,
        15,
    )

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

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet on the mapped model when bookToUpdate arrives`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value = stateFlow.value.copy(bookToUpdate = book)

            // ----- Assert -----
            val expected = book.toProgressSheetUiModel(selectedTab = stateFlow.value.progressSheetTab)
            val sheet = stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull()
            sheet.copy(whenRead = expected.whenRead) shouldBe expected
            job.cancel()
        }

        @Test
        fun `book going null closes an Open session`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(bookToUpdate = book)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(bookToUpdate = null)

            // ----- Assert -----
            stateFlow.value.progressSheet.isClosing shouldBe true
            stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull()
            job.cancel()
        }

        @Test
        fun `reopens with a fresh model when bookToUpdate arrives while a close is in flight`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val closingModel = ProgressSheetUiModel.previews.values.first().requestingClose()
            stateFlow.value = ReadingScreenUiState(progressSheet = SheetSession.Open(model = closingModel))
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(bookToUpdate = book)

            // ----- Assert -----
            stateFlow.value.progressSheet.isClosing shouldBe false
            val expected = book.toProgressSheetUiModel(selectedTab = stateFlow.value.progressSheetTab)
            val sheet = stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull()
            sheet.copy(whenRead = expected.whenRead) shouldBe expected
            job.cancel()
        }

        @Test
        fun `leaves a closed session closed while there is no bookToUpdate`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(progressSheetTab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe SheetSession.Closed
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

            stateFlow.value = stateFlow.value.copy(bookToUpdate = PreviewData.baseBook)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(progressSheetTab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            stateFlow.value.progressSheet.isClosing shouldBe false
            job.cancel()
        }

        @Test
        fun `a tab change alone re-emits the model with the stored progressSheetTab`() = runTest(UnconfinedTestDispatcher()) {
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
                bookToUpdate = book,
                progressSheetTab = ProgressSheetTab.PAGE,
            )
            stateFlow.value.progressSheet.modelOrNull?.tabs?.selected shouldBe ProgressSheetTab.PAGE

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(progressSheetTab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            stateFlow.value.progressSheet.modelOrNull?.tabs?.selected shouldBe stateFlow.value.progressSheetTab
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
                userBook = PreviewData.baseBook.userBook?.copy(reads = emptyList()),
            )
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(bookToUpdate = book)

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
                userBook = PreviewData.baseBook.userBook?.copy(reads = emptyList()),
            )
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                bookToUpdate = book,
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

            stateFlow.value = stateFlow.value.copy(bookToUpdate = PreviewData.baseBook)

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
                bookToUpdate = PreviewData.baseBook,
                readAt = pastReadAt,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readAt = null)

            // ----- Assert -----
            val whenRead = stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull().whenRead
            whenRead.chip.selected shouldBe false
            job.cancel()
        }

        @Test
        fun `a backdate alone leaves the time fields and tabs as they were`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val audioEdition = PreviewData.baseEdition.copy(
                readingFormat = ReadingFormat.Audio,
                audioSeconds = 7200,
            )
            val book = PreviewData.baseBook.copy(
                editions = listOf(audioEdition),
                defaultEdition = audioEdition,
                userBook = PreviewData.baseBook.userBook?.copy(reads = emptyList()),
            )
            val draft = ProgressTimeDraft(
                forSeconds = 0,
                hours = "2",
                minutes = "20",
                seconds = "5",
            )
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            stateFlow.value = stateFlow.value.copy(
                bookToUpdate = book,
                progressTimeDraft = draft,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(readAt = pastReadAt)

            // ----- Assert -----
            val medium = stateFlow.value.progressSheet.modelOrNull?.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.hours.text shouldBe "2"
            medium.minutes.text shouldBe "20"
            medium.seconds.text shouldBe "5"
            job.cancel()
        }
    }
}
