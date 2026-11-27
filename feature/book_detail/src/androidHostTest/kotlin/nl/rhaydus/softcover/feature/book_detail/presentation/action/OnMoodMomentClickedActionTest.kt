package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.progresssheet.presentation.mood.MoodMomentSheetSlice
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailMoodMomentSheetHost
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnMoodMomentClickedActionTest {
    private lateinit var slice: MoodMomentSheetSlice
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        slice = mockk(relaxed = true)

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.moodMomentSheetSlice
            } returns slice
        }

        scope = ActionScope(
            stateFlow = MutableStateFlow(buildBookDetailUiState(bookId = 42)),
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `opens the moment with the numeric id for the screen's book`() = runTest {
            // ----- Arrange -----
            val action = OnMoodMomentClickedAction(id = "7")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                slice.openToEdit(
                    bookId = 42,
                    momentId = 7L,
                    host = BookDetailMoodMomentSheetHost,
                    scope = scope,
                )
            }
        }

        @Test
        fun `opens nothing when the id is not a number`() = runTest {
            // ----- Arrange -----
            val action = OnMoodMomentClickedAction(id = "not-a-number")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                slice.openToEdit(
                    bookId = any(),
                    momentId = any(),
                    host = BookDetailMoodMomentSheetHost,
                    scope = scope,
                )
            }
        }
    }
}
