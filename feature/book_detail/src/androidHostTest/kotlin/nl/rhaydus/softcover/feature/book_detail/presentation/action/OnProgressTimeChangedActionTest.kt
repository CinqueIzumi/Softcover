package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressTimeUnit
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnProgressTimeChangedActionTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun bookAt(currentSeconds: Int?): Book = PreviewData.baseBook.copy(
        userBookRead = UserBookRead(
            id = 1,
            currentPage = null,
            currentSeconds = currentSeconds,
            progress = 0f,
            startedAt = null,
            finishedAt = null,
        ),
    )

    private fun audiobookAt(currentSeconds: Int): Book {
        val audioEdition = PreviewData.baseEdition.copy(
            readingFormat = ReadingFormat.Audio,
            audioSeconds = 7200,
        )

        return bookAt(currentSeconds = currentSeconds).copy(
            editions = listOf(audioEdition),
            defaultEdition = audioEdition,
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `does nothing when there is no book`() = runTest {
            // ----- Arrange -----
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.HOURS,
                text = "3",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft shouldBe null
        }

        @Test
        fun `seeds the draft from the book position and applies the edited hours`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = bookAt(currentSeconds = 3725))
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.HOURS,
                text = "7",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft shouldBe ProgressTimeDraft(
                forSeconds = 3725,
                hours = "7",
                minutes = "2",
                seconds = "5",
            )
        }

        @Test
        fun `clamps minutes above 59 down to 59`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = bookAt(currentSeconds = 0))
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.MINUTES,
                text = "99",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft?.minutes shouldBe "59"
        }

        @Test
        fun `clamps seconds above 59 down to 59`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = bookAt(currentSeconds = 0))
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.SECONDS,
                text = "75",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft?.seconds shouldBe "59"
        }

        @Test
        fun `keeps an empty text so the field can be cleared`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = bookAt(currentSeconds = 3725))
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.MINUTES,
                text = "",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft?.minutes shouldBe ""
        }

        @Test
        fun `ignores text that is not a number`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = bookAt(currentSeconds = 3725))
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.HOURS,
                text = "abc",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft?.hours shouldBe "1"
        }

        @Test
        fun `keeps earlier edits to other units when the stored draft matches the book position`() = runTest {
            // ----- Arrange -----
            val stored = ProgressTimeDraft(
                forSeconds = 3725,
                hours = "9",
                minutes = "8",
                seconds = "7",
            )
            stateFlow.value = BookDetailUiState(
                book = bookAt(currentSeconds = 3725),
                progressTimeDraft = stored,
            )
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.SECONDS,
                text = "30",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft shouldBe stored.copy(seconds = "30")
        }

        @Test
        fun `discards a stale draft when the book position has moved on`() = runTest {
            // ----- Arrange -----
            val stale = ProgressTimeDraft(
                forSeconds = 100,
                hours = "9",
                minutes = "8",
                seconds = "7",
            )
            stateFlow.value = BookDetailUiState(
                book = bookAt(currentSeconds = 3725),
                progressTimeDraft = stale,
            )
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.SECONDS,
                text = "30",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft shouldBe ProgressTimeDraft(
                forSeconds = 3725,
                hours = "1",
                minutes = "2",
                seconds = "30",
            )
        }

        @Test
        fun `treats a book without a recorded position as starting from zero`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = bookAt(currentSeconds = null))
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.HOURS,
                text = "2",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft shouldBe ProgressTimeDraft(
                forSeconds = 0,
                hours = "2",
                minutes = "0",
                seconds = "0",
            )
        }

        @Test
        fun `leaves a paged progress sheet unchanged`() = runTest {
            // ----- Arrange -----
            val paged = bookAt(currentSeconds = 3725)
            val sheet = paged.toProgressSheetUiModel(selectedTab = ProgressSheetTab.PAGE)
            stateFlow.value = BookDetailUiState(
                book = paged,
                progressSheet = sheet,
            )
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.MINUTES,
                text = "45",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe sheet
        }

        @Test
        fun `keeps progressSheet null when no sheet has been derived yet`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = audiobookAt(currentSeconds = 3725))
            val action = OnProgressTimeChangedAction(
                unit = ProgressTimeUnit.MINUTES,
                text = "45",
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressSheet shouldBe null
        }
    }
}
