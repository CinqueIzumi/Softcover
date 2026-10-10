package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull

import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
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

class OnDismissShareSheetActionTest {
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
        fun `hides the share sheet and clears the chosen variant`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState()

            // ----- Act -----
            OnDismissShareSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isShareSheetVisible shouldBe false
            stateFlow.value.chosenShareVariant shouldBe null
        }

        @Test
        fun `resets the capture progress and close request, and the held sheet's busy state`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState(
                capture = ShareCaptureState(
                    progress = ShareCaptureProgress.Sharing,
                    closeRequested = true,
                ),
            )

            // ----- Act -----
            OnDismissShareSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState()
            val scaffold = stateFlow.value.shareSheet.shouldNotBeNull().sheet.scaffold
            scaffold.busy shouldBe false
            scaffold.closeRequested shouldBe false
        }
    }
}
