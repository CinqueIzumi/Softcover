package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EndSessionPauseSheetCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

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

    private fun TestScope.launchCollector() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            EndSessionPauseSheetCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `keeps the sheet closed while no pause is requested`() = runTest {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            val sheet = stateFlow.value.endSessionPauseSheet

            // ----- Assert -----
            sheet shouldBe SheetSession.Closed
        }

        @Test
        fun `opens the sheet when a pause is requested`() = runTest {
            // ----- Arrange -----
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(endSessionPauseRequested = true)

            // ----- Assert -----
            stateFlow.value.endSessionPauseSheet.isOpen shouldBe true
        }

        @Test
        fun `asks the sheet to close when the request is cleared`() = runTest {
            // ----- Arrange -----
            launchCollector()
            stateFlow.value = stateFlow.value.copy(endSessionPauseRequested = true)

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(endSessionPauseRequested = false)

            // ----- Assert -----
            stateFlow.value.endSessionPauseSheet.isClosing shouldBe true
        }

        @Test
        fun `a late requested emission after the request was cleared does not reopen the sheet`() = runTest {
            // ----- Arrange -----
            val staleState = stateFlow.value.copy(endSessionPauseRequested = true)
            var openAfterStaleEmission: Boolean? = null
            val lateEmittingState = object : MutableStateFlow<BookDetailUiState> by stateFlow {
                override suspend fun collect(collector: FlowCollector<BookDetailUiState>): Nothing {
                    collector.emit(staleState)
                    openAfterStaleEmission = stateFlow.value.endSessionPauseSheet.isOpen
                    stateFlow.collect(collector)
                }
            }
            val lateScope = ActionScope(
                stateFlow = lateEmittingState,
                localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
                eventChannel = Channel<BookDetailEvent>(Channel.BUFFERED),
            )

            // ----- Act -----
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                EndSessionPauseSheetCollector().onLaunch(
                    scope = lateScope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            openAfterStaleEmission shouldBe false
            stateFlow.value.endSessionPauseSheet.isOpen shouldBe false
        }
    }
}
