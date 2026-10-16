package nl.rhaydus.softcover.feature.widget.progress

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.NO_USER_ID
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LoadUpdateProgressActionTest {
    private val eventChannel = Channel<UpdateProgressEvent>(Channel.BUFFERED)
    private lateinit var dependencies: UpdateProgressDependencies
    private lateinit var stateFlow: MutableStateFlow<UpdateProgressUiState>
    private lateinit var scope: ActionScope<UpdateProgressUiState, UpdateProgressEvent, UpdateProgressLocalVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(UpdateProgressUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(UpdateProgressLocalVariables()),
            eventChannel = eventChannel,
        )
    }

    private fun stubBook(id: Int): Book = mockk<Book>().also { book ->
        every {
            book.id
        } returns id
    }

    @Nested
    inner class Execute {
        @Test
        fun `finishes without loading when nobody is signed in`() = runTest {
            // ----- Arrange -----
            every {
                dependencies.getUserIdAsFlow()
            } returns flowOf(NO_USER_ID)
            every {
                dependencies.getCurrentlyReadingUserBooks()
            } returns flowOf(listOf(stubBook(id = 7)))

            // ----- Act -----
            LoadUpdateProgressAction(bookId = 7).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
            stateFlow.value.bookToUpdate.shouldBeNull()
        }

        @Test
        fun `finishes when the book is not on the reading shelf`() = runTest {
            // ----- Arrange -----
            every {
                dependencies.getUserIdAsFlow()
            } returns flowOf(1)
            every {
                dependencies.getCurrentlyReadingUserBooks()
            } returns flowOf(listOf(stubBook(id = 8)))

            // ----- Act -----
            LoadUpdateProgressAction(bookId = 7).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe UpdateProgressEvent.Finished
            stateFlow.value.bookToUpdate.shouldBeNull()
        }

        @Test
        fun `loads the matching shelf book into the sheet without finishing`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 7)
            every {
                dependencies.getUserIdAsFlow()
            } returns flowOf(1)
            every {
                dependencies.getCurrentlyReadingUserBooks()
            } returns flowOf(listOf(stubBook(id = 8), book))

            // ----- Act -----
            LoadUpdateProgressAction(bookId = 7).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate shouldBe book
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }

        @Test
        fun `keeps the already loaded book when run again`() = runTest {
            // ----- Arrange -----
            val loaded = stubBook(id = 7)
            stateFlow.value = UpdateProgressUiState(bookToUpdate = loaded)
            every {
                dependencies.getUserIdAsFlow()
            } returns flowOf(NO_USER_ID)

            // ----- Act -----
            LoadUpdateProgressAction(bookId = 7).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bookToUpdate shouldBe loaded
            eventChannel.tryReceive().getOrNull().shouldBeNull()
        }
    }
}
