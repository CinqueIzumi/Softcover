package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnShowUpdateProgressSheetClickActionTest {
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
        fun `requests the progress sheet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = PreviewData.baseBook)

            // ----- Act -----
            OnShowUpdateProgressSheetClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheetRequested shouldBe true
        }

        @Test
        fun `leaves the session to the collector`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = PreviewData.baseBook)

            // ----- Act -----
            OnShowUpdateProgressSheetClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe SheetSession.Closed
        }

        @Test
        fun `keeps the stored tab`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = PreviewData.baseBook,
                selectedProgressSheetTab = ProgressSheetTab.PERCENTAGE,
            )

            // ----- Act -----
            OnShowUpdateProgressSheetClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectedProgressSheetTab shouldBe ProgressSheetTab.PERCENTAGE
        }

        @Test
        fun `resets a leftover backdate to just now`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = PreviewData.baseBook,
                readAt = LocalDateTime(
                    2020,
                    1,
                    2,
                    9,
                    15,
                ),
            )

            // ----- Act -----
            OnShowUpdateProgressSheetClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.readAt.shouldBeNull()
        }

        @Test
        fun `resets a leftover backdate even when there is no book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = null,
                readAt = LocalDateTime(
                    2020,
                    1,
                    2,
                    9,
                    15,
                ),
            )

            // ----- Act -----
            OnShowUpdateProgressSheetClickAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.readAt.shouldBeNull()
        }
    }
}
