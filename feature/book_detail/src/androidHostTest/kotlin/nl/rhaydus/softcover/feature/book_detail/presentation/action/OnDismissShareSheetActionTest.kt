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

class OnDismissShareSheetActionTest {
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
            stateFlow.value = openSheetState().copy(chosenShareSize = ShareCardSize.FULL_TEXT)

            // ----- Act -----
            OnDismissShareSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.chosenShareSize shouldBe ShareCardSize.STORY
        }

        @Test
        fun `closes the share sheet session and clears the chosen variant`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState()

            // ----- Act -----
            OnDismissShareSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
            stateFlow.value.chosenShareVariant.shouldBeNull()
        }

        @Test
        fun `closes a session that is already closing`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState(closing = true)

            // ----- Act -----
            OnDismissShareSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `clears the request so the collector does not reopen the sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState().copy(shareSheetRequested = true)

            // ----- Act -----
            OnDismissShareSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheetRequested shouldBe false
        }

        @Test
        fun `resets the capture progress`() = runTest {
            // ----- Arrange -----
            stateFlow.value = openSheetState(capture = ShareCaptureState(progress = ShareCaptureProgress.Sharing))

            // ----- Act -----
            OnDismissShareSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareCapture shouldBe ShareCaptureState()
        }

        @Test
        fun `leaves an already closed session closed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(shareSheet = SheetSession.Closed)

            // ----- Act -----
            OnDismissShareSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.shareSheet shouldBe SheetSession.Closed
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
            OnDismissShareSheetAction().execute(
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
