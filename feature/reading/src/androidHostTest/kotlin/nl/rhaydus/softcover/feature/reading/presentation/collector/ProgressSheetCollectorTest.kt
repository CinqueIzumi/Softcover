package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetUiModel
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

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
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

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
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

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
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

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
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            stateFlow.value = stateFlow.value.copy(
                bookToUpdate = book,
                progressSheetTab = ProgressSheetTab.PAGE,
            )
            stateFlow.value.progressSheet?.selectedTab shouldBe ProgressSheetTab.PAGE

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(progressSheetTab = ProgressSheetTab.TIME)

            // ----- Assert -----
            stateFlow.value.progressSheet?.selectedTab shouldBe stateFlow.value.progressSheetTab
            job.cancel()
        }
    }
}
