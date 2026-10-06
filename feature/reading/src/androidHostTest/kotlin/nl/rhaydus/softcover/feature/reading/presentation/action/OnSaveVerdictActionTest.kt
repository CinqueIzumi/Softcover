package nl.rhaydus.softcover.feature.reading.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.SaveBookVerdictUseCase
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnSaveVerdictActionTest {
    private lateinit var saveBookVerdictUseCase: SaveBookVerdictUseCase
    private lateinit var dependencies: ReadingScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        saveBookVerdictUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        dependencies = mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.saveBookVerdictUseCase
            } returns saveBookVerdictUseCase
        }
    }

    private fun stubBook(id: Int = 42): Book = mockk<Book>().also { mock ->
        every {
            mock.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `saves the verdict arguments and flags no failure when the use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook()

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 3.0,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = true,
                )
            } returns Result.success(Unit)

            val action = OnSaveVerdictAction(
                book = book,
                rating = 3.0,
                review = RichTextUiModel.EMPTY,
                hasSpoilers = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 3.0,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = true,
                )
            }
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe false
        }

        @Test
        fun `flags the book as failed when the use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)

            coEvery {
                saveBookVerdictUseCase(
                    book = book,
                    rating = 4.5,
                    review = ReviewDocument.EMPTY,
                    hasSpoilers = false,
                )
            } returns Result.failure(RuntimeException("api error"))

            val action = OnSaveVerdictAction(
                book = book,
                rating = 4.5,
                review = RichTextUiModel.EMPTY,
                hasSpoilers = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.failedMutationBookIds.contains(42) shouldBe true
        }
    }
}
