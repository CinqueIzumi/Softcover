package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetState
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnShowProgressSheetClickActionTest {
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets the book to update and requests the sheet`() = runTest {
            // ----- Arrange -----
            val book = PreviewData.baseBook
            val action = OnShowProgressSheetClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate shouldBe book
            stateFlow.value.progressSheet.requested shouldBe true
        }

        @Test
        fun `resets a leftover backdate to just now`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                progressSheet = ProgressSheetState(
                    readAt = LocalDateTime(
                        2020,
                        1,
                        2,
                        9,
                        15,
                    ),
                ),
            )
            val action = OnShowProgressSheetClickAction(book = PreviewData.baseBook)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.readAt shouldBe null
        }

        @Test
        fun `keeps the stored tab`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                progressSheet = ProgressSheetState(tab = ProgressSheetTab.PERCENTAGE),
            )
            val action = OnShowProgressSheetClickAction(book = PreviewData.baseBook)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet.tab shouldBe ProgressSheetTab.PERCENTAGE
        }
    }
}
