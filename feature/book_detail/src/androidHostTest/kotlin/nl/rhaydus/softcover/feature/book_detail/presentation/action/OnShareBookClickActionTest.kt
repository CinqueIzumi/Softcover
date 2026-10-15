package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import nl.rhaydus.softcover.core.component.share.ShareCardFormat
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardSize
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareCardVariant
import nl.rhaydus.softcover.feature.book_detail.presentation.state.ShareSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.shareSheetFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnShareBookClickActionTest {
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
            cover = CoverUiModel(
                source = null,
                coverlessTitle = "Piranesi",
                variant = CoverVariant.Floating,
            ),
            title = "Piranesi",
            author = "Susanna Clarke",
            statsLine = null,
            description = null,
            format = ShareCardFormat.Story,
        ),
        bookTitle = "Piranesi",
        variant = ShareCardVariant.BOOK,
        hasUpdateCard = false,
        size = ShareCardSize.STORY,
        hasExpandableText = false,
        tagPicker = null,
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
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `resets the chosen size to story`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = PreviewData.baseBook,
                chosenShareSize = ShareCardSize.FULL_TEXT,
            )

            // ----- Act -----
            OnShareBookClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chosenShareSize shouldBe ShareCardSize.STORY
        }

        @Test
        fun `requests the share sheet and clears the chosen variant`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = PreviewData.baseBook,
                chosenShareVariant = ShareCardVariant.BOOK,
            )

            // ----- Act -----
            OnShareBookClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheetRequested shouldBe true
            stateFlow.value.chosenShareVariant.shouldBeNull()
        }

        @Test
        fun `leaves the session to the collector`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = PreviewData.baseBook)

            // ----- Act -----
            OnShareBookClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `resets the capture progress and leaves a closing session for the collector to reopen`() = runTest {
            // ----- Arrange -----
            val busyState = openSheetState(
                capture = ShareCaptureState(progress = ShareCaptureProgress.Sharing),
                closing = true,
            )
            stateFlow.value = busyState.copy(book = PreviewData.baseBook)

            // ----- Act -----
            OnShareBookClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState()
            stateFlow.value.shareSheetRequested shouldBe true
            stateFlow.value.shareSheet.isClosing shouldBe true
        }

        @Test
        fun `resets the capture progress even when there is no book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = null,
                shareCapture = ShareCaptureState(progress = ShareCaptureProgress.Saving),
            )

            // ----- Act -----
            OnShareBookClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState()
        }

        @Test
        fun `clears the share tag selections and collapses the picker`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = PreviewData.baseBook,
                selectedBookShareTags = persistentSetOf("Cozy"),
                deselectedUpdateShareTags = persistentSetOf("epic"),
                shareTagPickerExpanded = true,
            )

            // ----- Act -----
            OnShareBookClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedBookShareTags shouldBe persistentSetOf<String>()
            stateFlow.value.deselectedUpdateShareTags shouldBe persistentSetOf<String>()
            stateFlow.value.shareTagPickerExpanded shouldBe false
        }
    }
}
