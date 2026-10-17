package nl.rhaydus.softcover.feature.scan.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
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

    private fun openSession(
        closing: Boolean = false,
        pending: ResolvedScan? = null,
    ): SheetSession<UnknownIsbnSheetUiModel, ResolvedScan> {
        val model = UnknownIsbnSheetUiModel(
            isbn = "9780451524935",
            scaffold = unknownIsbnSheetScaffoldFor(
                isAdding = false,
                isImportPending = false,
            ),
        )

        return SheetSession.Open(
            model = if (closing) model.requestingClose() else model,
            pending = pending,
        )
    }

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
        fun `closes the session and sends no event when nothing was resolved`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(closing = true),
            )
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet shouldBe SheetSession.Closed
            stateFlow.value.unknownIsbn.shouldBeNull()
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `closes a sheet the user swiped away without a close request`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(unknownIsbnSheet = openSession())
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet shouldBe SheetSession.Closed
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `leaves state unchanged and sends no event while a book is being added`() = runTest {
            // ----- Arrange -----
            val addingState = ScanUiState(
                unknownIsbnSheet = openSession(),
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
        fun `hands back the pending resolved scan as BookResolvedEvent and closes the session`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            stateFlow.value = ScanUiState(
                unknownIsbnSheet = openSession(
                    closing = true,
                    pending = ResolvedScan(
                        book = book,
                        editionId = 7,
                    ),
                ),
            )
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.unknownIsbnSheet shouldBe SheetSession.Closed
            val event = eventChannel.tryReceive().getOrNull()
            event.shouldBeInstanceOf<ScanEvent.BookResolvedEvent>()
            event.book shouldBe book
            event.editionId shouldBe 7
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `keeps the pending resolved scan and sends no event while a book is being added`() = runTest {
            // ----- Arrange -----
            val resolving = ScanUiState(
                unknownIsbnSheet = openSession(
                    closing = true,
                    pending = ResolvedScan(
                        book = PreviewData.baseBook,
                        editionId = null,
                    ),
                ),
                isAddingBook = true,
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
    

        @Test
        fun `resets the pending flag when the sheet is dismissed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ScanUiState(
                unknownIsbn = "9780451524935",
                unknownIsbnSheet = openSession(),
                isImportPending = true,
            )
            val action = OnAddUnknownIsbnDismissedAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isImportPending shouldBe false
        }
}
}
