package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.scan.presentation.event.ScanEvent
import nl.rhaydus.softcover.feature.scan.presentation.screenmodel.ScanDependencies
import nl.rhaydus.softcover.feature.scan.presentation.state.LocalScanVariables
import nl.rhaydus.softcover.feature.scan.presentation.state.ResolvedScan
import nl.rhaydus.softcover.feature.scan.presentation.state.ScanUiState
import nl.rhaydus.softcover.feature.scan.presentation.state.UnknownIsbnSheetUiModel
import nl.rhaydus.softcover.feature.scan.presentation.state.unknownIsbnSheetScaffoldFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnAddUnknownIsbnDismissedActionTest {
    private lateinit var dependencies: ScanDependencies
    private lateinit var stateFlow: MutableStateFlow<ScanUiState>
    private lateinit var eventChannel: Channel<ScanEvent>
    private lateinit var scope: ActionScope<ScanUiState, ScanEvent, LocalScanVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ScanUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalScanVariables()),
            eventChannel = eventChannel,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `clears the unknown isbn sheet and the pending close request so the sheet can open again`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbnSheet = UnknownIsbnSheetUiModel(
                    isbn = "9780451524935",
                    scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
                ),
                isUnknownIsbnCloseRequested = true,
            )
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet.shouldBeNull()
            stateFlow.value.isUnknownIsbnCloseRequested shouldBe false
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `leaves state unchanged and sends no event while a book is being added`() = runTest {
            // ----- Arrange -----
            val addingState = ScanUiState(
                unknownIsbnSheet = UnknownIsbnSheetUiModel(
                    isbn = "9780451524935",
                    scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
                ),
                isAddingBook = true,
            )
            stateFlow.value = addingState
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe addingState
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `sends BookResolvedEvent for the resolved scan and clears it`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            stateFlow.value = ScanUiState(
                unknownIsbnSheet = UnknownIsbnSheetUiModel(
                    isbn = "9780451524935",
                    scaffold = unknownIsbnSheetScaffoldFor(isAdding = false),
                ),
                isUnknownIsbnCloseRequested = true,
                resolvedUnknownIsbn = ResolvedScan(
                    book = book,
                    editionId = 7,
                ),
            )
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.resolvedUnknownIsbn.shouldBeNull()
            stateFlow.value.unknownIsbnSheet.shouldBeNull()
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<ScanEvent.BookResolvedEvent>()
            event.book shouldBe book
            event.editionId shouldBe 7
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `keeps the resolved scan and sends no event while a book is being added`() = runTest {
            // ----- Arrange -----
            val resolving = ScanUiState(
                isAddingBook = true,
                resolvedUnknownIsbn = ResolvedScan(
                    book = PreviewData.baseBook,
                    editionId = null,
                ),
            )
            stateFlow.value = resolving
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe resolving
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
