package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.verdict.VerdictSheetContext
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
        variant = CoverVariant.ReadingRowThumb,
    )

    @Nested
    inner class OnLaunch {
        @Test
        fun `verdictSheet stays null while there is no prompt book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VerdictSheetCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictCover = cover())

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe null
            job.cancel()
        }

        @Test
        fun `builds the FINISHED model when the prompt book is set even without a cover`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val collector = VerdictSheetCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = book)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe book.toVerdictSheetUiModel(
                context = VerdictSheetContext.FINISHED,
                review = null,
                cover = null,
            )
            job.cancel()
        }

        @Test
        fun `updates the model when the cover arrives`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val cover = cover()
            val collector = VerdictSheetCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = book)

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

        @Test
        fun `clears the model when the prompt book is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VerdictSheetCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = PreviewData.baseBook)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = null)

            // ----- Assert -----
            stateFlow.value.verdictSheet shouldBe null
            job.cancel()
        }

        @Test
        fun `never offers a delete secondary action`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = VerdictSheetCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(verdictPromptBook = PreviewData.baseBook)

            // ----- Assert -----
            stateFlow.value.verdictSheet?.scaffold?.footer?.secondary shouldBe null
            job.cancel()
        }
    }
}
