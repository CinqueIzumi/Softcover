package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnProgressMinutesSteppedActionTest {
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

    private fun bookAt(currentSeconds: Int?): Book = PreviewData.baseBook.copy(
        userBook = PreviewData.baseBook.userBook?.copy(
            status = BookStatus.Reading,
            reads = listOf(
                UserBookRead(
                id = 1,
                currentPage = null,
                currentSeconds = currentSeconds,
                progress = 0f,
                startedAt = null,
                finishedAt = null,
                ),
            ),
        ),
    )

    private fun draftWithMinutes(minutes: String) = ProgressTimeDraft(
        forSeconds = 3725,
        hours = "1",
        minutes = minutes,
        seconds = "5",
    )

    @Nested
    inner class Execute {
        @Test
        fun `does nothing when there is no book`() = runTest {
            // ----- Arrange -----
            val action = OnProgressMinutesSteppedAction(delta = 1)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft shouldBe null
        }

        @Test
        fun `steps minutes up by one from the seeded book position`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(bookToUpdate = bookAt(currentSeconds = 3725))
            val action = OnProgressMinutesSteppedAction(delta = 1)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft shouldBe draftWithMinutes("3")
        }

        @Test
        fun `does not step below zero`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = bookAt(currentSeconds = 3725),
                progressTimeDraft = draftWithMinutes("0"),
            )
            val action = OnProgressMinutesSteppedAction(delta = -1)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft?.minutes shouldBe "0"
        }

        @Test
        fun `does not step above 59`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = bookAt(currentSeconds = 3725),
                progressTimeDraft = draftWithMinutes("59"),
            )
            val action = OnProgressMinutesSteppedAction(delta = 1)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft?.minutes shouldBe "59"
        }

        @Test
        fun `treats a cleared minutes field as zero when stepping up`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = bookAt(currentSeconds = 3725),
                progressTimeDraft = draftWithMinutes(""),
            )
            val action = OnProgressMinutesSteppedAction(delta = 1)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft?.minutes shouldBe "1"
        }

        @Test
        fun `steps from a fresh seed when the stored draft is stale`() = runTest {
            // ----- Arrange -----
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = bookAt(currentSeconds = 3725),
                progressTimeDraft = draftWithMinutes("40").copy(forSeconds = 100),
            )
            val action = OnProgressMinutesSteppedAction(delta = 1)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.progressTimeDraft shouldBe draftWithMinutes("3")
        }
    }
}
