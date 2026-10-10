package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel

import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
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
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    private fun openSheetState(
        capture: ShareCaptureState = ShareCaptureState(),
    ): BookDetailUiState = BookDetailUiState(
        isShareSheetVisible = true,
        chosenShareVariant = ShareCardVariant.BOOK,
        shareCapture = capture,
        shareSheet = shareSheetFor(
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
            progress = capture.progress,
            closeRequested = capture.closeRequested,
        ),
    )

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
            stateFlow.value.shareSheet.shouldNotBeNull().sheet.scaffold.busy shouldBe true
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
            stateFlow.value.shareSheet.shouldNotBeNull().sheet.scaffold.busy shouldBe false
        }

        @Test
        fun `asks the held sheet to close once the card was shared`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState()

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Shared).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture.closeRequested shouldBe true
            stateFlow.value.shareSheet.shouldNotBeNull().sheet.scaffold.closeRequested shouldBe true
        }

        @Test
        fun `leaves the capture state and the sheet visible when the sheet reports it was dismissed`() = runTest {
            // ----- Arrange -----
            val capture = ShareCaptureState(progress = ShareCaptureProgress.Saving)
            stateFlow.value = openSheetState(capture = capture)

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.Dismissed).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe capture
            stateFlow.value.isShareSheetVisible shouldBe true
        }

        @Test
        fun `records the capture without a sheet when none is held`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState()

            // ----- Act -----
            OnShareCaptureEventAction(captureEvent = ShareCardSheetEvent.SaveStarted).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture.progress shouldBe ShareCaptureProgress.Saving
            stateFlow.value.shareSheet.shouldBeNull()
        }
    }
}
