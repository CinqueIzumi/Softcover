package nl.rhaydus.softcover.feature.widget.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.event.UpdateProgressEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressDependencies
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressLocalVariables
import nl.rhaydus.softcover.feature.widget.presentation.state.UpdateProgressUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressSheetCollectorTest {
    private lateinit var dependencies: UpdateProgressDependencies
    private lateinit var stateFlow: MutableStateFlow<UpdateProgressUiState>
    private lateinit var scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(UpdateProgressUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(UpdateProgressLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `opens the sheet on the mapped model when a book is set`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val job = launch {
                ProgressSheetCollector().onLaunch(
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
        fun `requests the sheet to close when the book is cleared`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                ProgressSheetCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            stateFlow.value = stateFlow.value.copy(bookToUpdate = PreviewData.baseBook)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(bookToUpdate = null)

            // ----- Assert -----
            stateFlow.value.progressSheet.isClosing shouldBe true
            stateFlow.value.progressSheet.modelOrNull.shouldNotBeNull()
            job.cancel()
        }
    }
}
