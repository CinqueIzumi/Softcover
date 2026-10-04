package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.progress.ProgressSheetMedium
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
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
        fun `sets progressSheet to the mapped model when bookToUpdate arrives`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value.progressSheet shouldBe book.toProgressSheetUiModel(
                selectedTab = stateFlow.value.progressSheetTab,
            )
            job.cancel()
        }

        @Test
        fun `progressSheet stays null while bookToUpdate is null`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = ProgressSheetCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(bookToUpdate = null)

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe null
            job.cancel()
        }

        @Test
        fun `progressSheet clears back to null when bookToUpdate becomes null again`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value.progressSheet shouldBe book.toProgressSheetUiModel(
                selectedTab = stateFlow.value.progressSheetTab,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(bookToUpdate = null)

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe null
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
            stateFlow.value.progressSheet?.selectedTab shouldBe ProgressSheetTab.PAGE

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(progressSheetTab = ProgressSheetTab.PERCENTAGE)

            // ----- Assert -----
            stateFlow.value.progressSheet?.selectedTab shouldBe stateFlow.value.progressSheetTab
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
            val medium = stateFlow.value.progressSheet?.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()
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
            val medium = stateFlow.value.progressSheet?.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.hours.text shouldBe "0"
            medium.minutes.text shouldBe "0"
            medium.seconds.text shouldBe "0"
            job.cancel()
        }

        @Test
        fun `does not reseed the fields while a draft stays present and only its text changes`() = runTest(UnconfinedTestDispatcher()) {
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

            val firstDraft = ProgressTimeDraft(
                forSeconds = 0,
                hours = "1",
                minutes = "1",
                seconds = "1",
            )
            val secondDraft = firstDraft.copy(minutes = "45")
            stateFlow.value = stateFlow.value.copy(
                bookToUpdate = book,
                progressTimeDraft = firstDraft,
            )

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(progressTimeDraft = secondDraft)

            // ----- Assert -----
            val medium = stateFlow.value.progressSheet?.medium.shouldBeInstanceOf<ProgressSheetMedium.Timed>()
            medium.minutes.text shouldBe "1"
            job.cancel()
        }
    }
}
