package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnDismissProgressSheetActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(buildBookDetailUiState().copy())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true)
    }

    @Nested
    inner class Execute {
        @Test
        fun `closes the progress sheet session`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                progressSheet = SheetSession.Open(model = ProgressSheetUiModel.previews.values.first()),
            )

            // ----- Act -----
            OnDismissProgressSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `closes a session that is already closing`() = runTest {
            // ----- Arrange -----
            val closingModel = ProgressSheetUiModel.previews.values.first().requestingClose()
            stateFlow.value = buildBookDetailUiState().copy(progressSheet = SheetSession.Open(model = closingModel))

            // ----- Act -----
            OnDismissProgressSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `clears the request so the collector does not reopen the sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                progressSheet = SheetSession.Open(model = ProgressSheetUiModel.previews.values.first()),
                progressSheetRequested = true,
            )

            // ----- Act -----
            OnDismissProgressSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheetRequested shouldBe false
        }

        @Test
        fun `discards the time draft so the next open reseeds from the book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                progressSheet = SheetSession.Open(model = ProgressSheetUiModel.previews.values.first()),
                progressTimeDraft = ProgressTimeDraft(
                    forSeconds = 3725,
                    hours = "1",
                    minutes = "2",
                    seconds = "5",
                ),
            )

            // ----- Act -----
            OnDismissProgressSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft.shouldBeNull()
        }

        @Test
        fun `keeps the book in state`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = PreviewData.baseBook,
                progressSheet = SheetSession.Open(model = ProgressSheetUiModel.previews.values.first()),
            )

            // ----- Act -----
            OnDismissProgressSheetAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.book shouldBe PreviewData.baseBook
        }
    }
}
