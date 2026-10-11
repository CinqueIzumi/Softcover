package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareSheetFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnShareCaptureEventActionTest {
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

    private fun heldSheet(
        progress: ShareCaptureProgress = ShareCaptureProgress.Idle,
    ): ShareSheetUiModel = shareSheetFor(
        card = BookShareCardUiModel(
            coverUrl = null,
            title = "Piranesi",
            author = "Susanna Clarke",
            statsLine = null,
            description = null,
        ),
        bookTitle = "Piranesi",
        variant = ShareCardVariant.BOOK,
        hasUpdateCard = false,
        progress = progress,
    )

    private fun openSheetState(
        capture: ShareCaptureState = ShareCaptureState(),
        closing: Boolean = false,
    ): BookDetailUiState {
        val sheet = heldSheet(progress = capture.progress)

        return buildBookDetailUiState().copy(
            chosenShareVariant = ShareCardVariant.BOOK,
            shareCapture = capture,
            shareSheet = SheetSession.Open(model = if (closing) sheet.requestingClose() else sheet),
            shareSheetRequested = true,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `records sharing progress and rebuilds the held sheet as busy`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState()

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.ShareStarted).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState(progress = ShareCaptureProgress.Sharing)
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.scaffold.busy shouldBe true
        }

        @Test
        fun `returns to idle and clears busy when the capture finishes`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState(capture = ShareCaptureState(progress = ShareCaptureProgress.Saving))

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.CaptureFinished).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture.progress shouldBe ShareCaptureProgress.Idle
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.scaffold.busy shouldBe false
        }

        @Test
        fun `keeps a closing session closing while the model is updated`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState(closing = true)

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.SaveStarted).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheet.isClosing shouldBe true
            stateFlow.value.shareSheet.modelOrNull.shouldNotBeNull().sheet.scaffold.busy shouldBe true
        }

        @Test
        fun `clears the share request once the card was shared`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState()

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Shared).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheetRequested shouldBe false
        }

        @Test
        fun `does nothing to a closed session when the card was shared`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(shareSheet = SheetSession.Closed)

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Shared).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `leaves the capture state and the session untouched when the sheet reports it was dismissed`() = runTest {
            // ----- Arrange -----
            val held = openSheetState(capture = ShareCaptureState(progress = ShareCaptureProgress.Saving))
            stateFlow.value = held

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Dismissed).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe held.shareCapture
            stateFlow.value.shareSheet shouldBe held.shareSheet
        }

        @Test
        fun `records the capture without opening a sheet when none is held`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState()

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.SaveStarted).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture.progress shouldBe ShareCaptureProgress.Saving
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }
    }
}
